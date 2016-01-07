package solver;


import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.ShortMessage.*;

/**
 * This is the core of the MekBassMidiMaker program as it is responsible for crafting a midi sequence
 * that the MekBass can actually play.<br>
 * Output can be changed by setting the String configuration (generally speaking, more strings can play more sounds).
 * @author Elliot Wilde
 * @author Andrew Palmer
 *
 */
public class GreedySolver implements Solver {

	// TODO: REPLACE ME WITH DYNAMIC
	private static MekString[] strings = new MekString[]{ new MekString(43, 56), new MekString(38, 51),
																new MekString(33, 46), new MekString(28, 41)};
	private static long[] stringTimes;
	private static int[] lastNote;
	private static int lastString = -1;

	public GreedySolver(MekString[] str){
		strings = str;
	}

	public GreedySolver(){

	}

	/**
	 *
	 */
	public Sequence solve(Sequence seq){
		return solve(seq, strings);
	}

	/**
	 * Takes a Sequence, and splits it up into a pre-defined number of tracks, dropping notes that do not fit within the
	 * constraints of the tracks, specified previously by the user.
	 * @param seq The Sequence to be bodged
	 * @return The New Sequence
	 */
	public Sequence solve(Sequence seq, MekString[] str){
		strings = str;
		// array of long for strings representing timestamp of most recent finished note
		stringTimes = new long[strings.length];
		lastNote = new int[strings.length];
		for (int i=0; i<strings.length; ++i) lastNote[i] = -1;


		Track tr = seq.getTracks()[0];
		// loop through
		for (int i=0; i<tr.size(); ++i){
			MidiMessage midmsg = tr.get(i).getMessage();
			// check what type of message it is
			if (midmsg instanceof ShortMessage){
				ShortMessage shrtmsg = (ShortMessage) midmsg;
				// check what kind of command it is (note on/off, etc)
				switch (shrtmsg.getCommand()){
				case NOTE_ON:
					// get a list of applicable strings
					int note = shrtmsg.getData1();
					// flag saying whether or not there is a potential conflict
					int stringInUse = -1;
					List<Integer> rightStrings = new ArrayList<Integer>();
					for (int j = 0; j < strings.length; j++){
						if (note >= strings[j].lowNote && note <= strings[j].highNote){
							if (lastNote[j] == -1) {
								rightStrings.add(j);
							} else {
								// if one of the strings that could be used is in use, set a flag
								stringInUse = j;
							}
						}
					}

					// if there is nothing in the list of strings AND lastString is not '-1' AND the stringInUse flag has been raised
					// we want to look at the last note added, and see if it could go on another string instead
					// then try again
					rearrangeFailure:
					if (rightStrings.isEmpty() && stringInUse != -1){
						// get the string/track
						Track lst = seq.getTracks()[stringInUse+1];
						// get the event
						int dec = 2;
						MidiEvent lstEvent = null;
						MidiMessage lstmsg = null;
						// this skips past metamessages and escape when it has the offending event
						do {
							if (dec >lst.size()) {
								break rearrangeFailure;
							}
							lstEvent = lst.get(lst.size()-dec);
							lstmsg = lstEvent.getMessage();
							dec++;
						} while (lstmsg instanceof MetaMessage);

						// get the note
						int lstNote = ((ShortMessage)lstmsg).getData1();
						// get the strings it could play on (minus the one it was just on)
						List<Integer> lastStrings = new ArrayList<Integer>();
						for (int j = 0; j < strings.length; j++){
							if (lstNote >= strings[j].lowNote && lstNote <= strings[j].highNote){
								if (lastNote[j] == -1 && j != stringInUse) {
									lastStrings.add(j);
								}
							}
						}
						// pick the most suitable string (last used longest time ago)

						long minTime = Long.MAX_VALUE;
						int useString = -1;
						for(Integer j : lastStrings){
							if (stringTimes[j] < minTime){
								minTime = stringTimes[j];
								useString = j;
							}
						}

						if (useString == -1) { // if it cant fit on any strings, dont go any further in the conflict resolution
							break;
						}
						// add it to that string (if there is another string), and remove it from the string it was on
						moveEvent(lst, seq.getTracks()[useString+1], lstEvent);
						lastNote[useString] = lstNote;
						lastNote[stringInUse] = -1;

						// then try the current event again
						for (int j = 0; j < strings.length; j++){
							if (note >= strings[j].lowNote && note <= strings[j].highNote){
								if (lastNote[j] == -1) {
									rightStrings.add(j);
								}
							}
						}
						// and now that we are done here(hopefully), we continue on our merry way
					}

					// we now have a list of strings which can theoretically play the note. we assign it to
					//the string that was played the longest time ago.
					//TODO: figure out which is closer to the middle
					long minTime = Long.MAX_VALUE;
					int useString = -1;
					for(Integer j : rightStrings){
						if (stringTimes[j] < minTime){
							minTime = stringTimes[j];
							useString = j;
						}
					}
					if(useString >= 0 && useString < strings.length){
						// put it there if it is valid
						moveEvent(tr,seq.getTracks()[useString+1],tr.get(i));
						i--;
						// put it in last note for that string
						lastNote[useString] = note;
						lastString = useString;
					}
					break;
					// if the if was false, then it is actually a note off
				case NOTE_OFF:
					note = shrtmsg.getData1();
					useString = -1;
					for (int j=0; j < strings.length; ++j){
						if (lastNote[j]==note) useString=j;
					}
					if (useString==-1){
						break; // if the note wasn't played, dont do anything with it
					}
					// now we have the correct string
					stringTimes[useString] = tr.get(i).getTick();
					// so we add the noteoff to the correct track
					moveEvent(tr,seq.getTracks()[useString+1],tr.get(i));
					i--;

					// and set the other stuff to nothing
					lastNote[useString] = -1;
					lastString = -1;
					break;
				case PROGRAM_CHANGE:
					for (int j = 1; j <= strings.length; j++){
						seq.getTracks()[j].add(tr.get(i));
					}
					tr.remove(tr.get(i));
					i--;
					break;
				case PITCH_BEND:
					default:
					// if there is a string playing
					if (lastString >= 0) {
						// add it to that
						moveEvent(tr,seq.getTracks()[lastString+1],tr.get(i));
						i--;
					}
				}
			}
			else{
				// this copies all meta messages (other than end of track) to all other tracks
				MetaMessage m = (MetaMessage) midmsg;
				if (m.getType() == 0x2f) break;
				for (int j = 1; j <= strings.length; j++){
					seq.getTracks()[j].add(tr.get(i));
				}
			}
		}

		return seq;
	}

	/**
	 * Removes event from the first Track, and inserts it into the second.
	 * @param from The Track to remove the event from
	 * @param to The Track to add the event to
	 * @param event The event
	 */
	private static void moveEvent(Track from, Track to, MidiEvent event){
		to.add(event);
		from.remove(event);
	}



}
