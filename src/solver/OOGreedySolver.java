package solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.ShortMessage.*;

/**
 * This is the core of the MekBassMidiMaker program as it is responsible for
 * crafting a midi sequence that the MekBass can actually play.<br>
 * Output can be changed by setting the String configuration (generally
 * speaking, more strings can play more sounds).
 *
 * @author Elliot Wilde
 * @author Andrew Palmer
 * @author Roma Klapaukh
 * @author James McVay
 */
public class OOGreedySolver implements Solver {

	private static MekString[] strings;

	public OOGreedySolver(MekString[] str) {
		strings = str;
	}

	public OOGreedySolver() {
		this(new MekString[] { new MekString(43, 56), new MekString(38, 51), new MekString(33, 46),
				new MekString(28, 41) });
	}

	/**
	 * Takes a Sequence, and splits it up into a pre-defined number of tracks,
	 * dropping notes that do not fit within the constraints of the tracks,
	 * specified previously by the user.
	 *
	 * @param seq
	 *            The Sequence to be bodged
	 * @return The New Sequence
	 */
	public Sequence solve(Sequence seq) {

		// This is where everything will be written. The last track will be the
		// stuff that isn't used.
		Sequence newTrack;

		try {
			newTrack = new solver.tests.Sequence(seq.getDivisionType(), seq.getResolution(), strings.length + 1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			throw new RuntimeException("It all went to hell, I'm sorry");
		}

		// This is where all the notes and data are now.
		Track tr = seq.getTracks()[0];
		float tickScaling = (float)seq.getMicrosecondLength()/10000;
		tickScaling = tickScaling/(float)seq.getTickLength();
		// This is the current allocation of the string
		// -1 denotes that the string is currently free
		int[] currentNote = new int[strings.length + 1];
		int[] lastNote = new int[strings.length + 1];
		long[] freeSince = new long[strings.length + 1];
		for (int i = 0; i < strings.length; ++i) {
			currentNote[i] = -1;
			freeSince[i] = -1;
			lastNote[i] = strings[i].lowNote;
		}
		
		//These are for the dump string
		currentNote[strings.length] = -1;
		lastNote[strings.length] = -1;
		
		// Assume that events come in time order, but that multiple events can
		// happen at the same instant
		List<MidiEvent> currentMoment = new ArrayList<MidiEvent>();
		for (int i = 0; i < tr.size(); i++) {
			// Get the event
			MidiEvent e = tr.get(i);

			// Get when it happens
			long now = e.getTick();

			// Get what happens
			//MidiMessage msg = e.getMessage();

			// Clear our list of current events and add a new one
			currentMoment.clear();
			currentMoment.add(e);

			// get everything else that happens at this moment
			while (i < tr.size() - 1 && tr.get(i + 1).getTick() == now) {
				i++;
				currentMoment.add(tr.get(i));
			}

			// Now process all the events that happen NOW
			// We do this in the following order
			// 1. All Control Messages
			// 2. All Note OFFs that have an open Note ON event
			// 3. All Note ON events
			// 4. All Note OFF events that now have an open Note ON event
			// 4.5. All unmatched note OFF events are dropped
			// That should be everything

			// 1. All control messages
			Iterator<MidiEvent> msgs = currentMoment.iterator();

			loopOne: while (msgs.hasNext()) {
				MidiEvent currentEvent = msgs.next();
				MidiMessage current = currentEvent.getMessage();
				if (current instanceof ShortMessage) {
					ShortMessage m = (ShortMessage) current;
					switch (m.getCommand()) {
					case NOTE_ON:
					case NOTE_OFF:
						continue loopOne;
					case PITCH_BEND:
						// These ideally would only go to only the affected
						// tracks, but that is too hard for us now.
					default:
						// these get copied ONLY to the solved tracks
						for (int j = 0; j < strings.length; j++) {
							MidiEvent newCurrentEvent = new MidiEvent((MidiMessage) currentEvent.getMessage().clone(),
									currentEvent.getTick());
							newTrack.getTracks()[j].add(newCurrentEvent);
						}
					}
				} else {
					// It must be a meta message. These we copy to all tracks so
					// that nothing bad happens. We do this even if it's end of
					// track because why not?
					MetaMessage m = (MetaMessage) current;
					if (m.getType() == 0x2f) {
						msgs.remove();
						continue loopOne;
					}
					for (int j = 0; j < strings.length + 1; j++) {
						MidiEvent newCurrentEvent = new MidiEvent((MidiMessage) currentEvent.getMessage().clone(),
								currentEvent.getTick());
						newTrack.getTracks()[j].add(newCurrentEvent);
					}
				}
				msgs.remove();
			}

			// 2. Note Off events with a matching previous Note ON
			msgs = currentMoment.iterator();

			loopTwo: while (msgs.hasNext()) {
				MidiEvent currentEvent = msgs.next();
				MidiMessage current = currentEvent.getMessage();
				if (!(current instanceof ShortMessage)) {
					throw new RuntimeException("Only short messages should be left");
				}

				ShortMessage m = (ShortMessage) current;
				switch (m.getCommand()) {
				case NOTE_ON:
					if (m.getData2() == 0) {
						// This is a secret note OFF
						int note = m.getData1();
						for (int string = 0; string < strings.length + 1; string++) {
							if (currentNote[string] == note) {
								lastNote[string] = currentNote[string];
								currentNote[string] = -1;
								freeSince[string] = now;
								newTrack.getTracks()[string].add(currentEvent);
								msgs.remove();
								continue loopTwo;
							}
						}
					}
					break;
				case NOTE_OFF:
					// This is actually a Note OFF
					int note = m.getData1();
					for (int string = 0; string < strings.length + 1; string++) {
						if (currentNote[string] == note) {
							lastNote[string] = currentNote[string];
							currentNote[string] = -1;
							freeSince[string] = now;
							newTrack.getTracks()[string].add(currentEvent);
							msgs.remove();
							continue loopTwo;
						}
					}
					break;
				default:
					throw new RuntimeException("I should have been already processed");
				}

			}

			// 3. All Note ON events
			msgs = currentMoment.iterator();

			loopThree: while (msgs.hasNext()) {
				MidiEvent currentEvent = msgs.next();
				MidiMessage current = currentEvent.getMessage();
				if (!(current instanceof ShortMessage)) {
					throw new RuntimeException("Only short messages should be left");
				}

				ShortMessage m = (ShortMessage) current;
				switch (m.getCommand()) {
				case NOTE_ON:
					int velocity = m.getData2();
					if (velocity == 0) {
						// This is a secret note off
						// don't process it here
						continue loopThree;
					}
					int note = m.getData1();
					// Go through all strings until you find one that is free
					// and can take the note.
					int bestIdx = -1;
					long bestDist = Integer.MAX_VALUE;
					for (int string = 0; string < strings.length; string++) {
						// Is it physically playable on this string?
						if (currentNote[string] == -1 && strings[string].playable(note)){//note >= strings[string].lowNote && note <= strings[string].highNote) {
							if((now - freeSince[string]) >= strings[string].differenceTick(note,lastNote[string],tickScaling) || lastNote[string] == note){
							long dist = strings[string].differenceTime(note, lastNote[string]);
							if (dist < bestDist) {
								bestIdx = string;
								bestDist = dist;
							}
							}
						}
					}
					if (bestIdx == -1) {
						// This note could not be assigned.
						newTrack.getTracks()[strings.length].add(currentEvent);
						currentNote[strings.length] = note;
					} else {
						newTrack.getTracks()[bestIdx].add(currentEvent);
						currentNote[bestIdx] = note;
					}
					msgs.remove();
					break;
				case NOTE_OFF:
					// This is actually a Note OFF
					// We skip this
					break;
				default:
					throw new RuntimeException("I should have been already processed");
				}

			}

			// 4. Note Off events with a matching previous Note ON - unmatched
			// are dropped FOREVER
			msgs = currentMoment.iterator();

			loopFour: while (msgs.hasNext()) {
				MidiEvent currentEvent = msgs.next();
				MidiMessage current = currentEvent.getMessage();
				if (!(current instanceof ShortMessage)) {
					throw new RuntimeException("Only short messages should be left");
				}

				ShortMessage m = (ShortMessage) current;
				switch (m.getCommand()) {
				case NOTE_ON:
					if (m.getData2() == 0) {
						// This is a secret note OFF
						int note = m.getData1();
						for (int string = 0; string < strings.length + 1; string++) {
							if (currentNote[string] == note) {
								lastNote[string] = currentNote[string];
								currentNote[string] = -1;
								freeSince[string] = now;								
								newTrack.getTracks()[string].add(currentEvent);
								msgs.remove();
								continue loopFour;
							}
						}
					}
					break;
				case NOTE_OFF:
					// This is actually a Note OFF
					int note = m.getData1();
					for (int string = 0; string < strings.length + 1; string++) {
						if (currentNote[string] == note) {
							lastNote[string] = currentNote[string];
							currentNote[string] = -1;
							freeSince[string] = now;
							newTrack.getTracks()[string].add(currentEvent);
							msgs.remove();
							continue loopFour;
						}
					}
					break;
				default:
					throw new RuntimeException("I should have been already processed");
				}

			}
		}

		return newTrack;
	}

}
