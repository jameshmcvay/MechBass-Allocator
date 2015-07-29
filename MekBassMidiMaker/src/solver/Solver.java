package solver;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.ShortMessage.*;

public class Solver {

	// TODO: REPLACE ME WITH DYNAMIC
	private static final MekString[] strings = new MekString[]{ new MekString(43, 56), new MekString(38, 51),
																new MekString(33, 46), new MekString(28, 41)};
	private static long[] stringTimes;
	private static int[] lastNote;

	static Sequence solve(Sequence seq){
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
					// check whether or not it is actually a note on...
					if (shrtmsg.getData2()!=0){
						// find the right string
						int note = shrtmsg.getData1();
						List<MekString> rightStrings = new ArrayList<MekString>();
						for (MekString ms: strings){
							if (note > ms.lowNote && note < ms.highNote) {
								rightStrings.add(ms);
							}
						}
						// we now have a list of strings which can theoretically play the note, one or more may be occupied


						// put it there

						// put it in last note for that string
						break;
					}
					// if the if was false, then it is actually a note off
				case NOTE_OFF:
					break;
					default:
				}
			}
		}

		return null;
	}

	/**
	 * Removes event from the first Track, and inserts it into the second.
	 * @param from The Track to remove the event from
	 * @param to The Track to add the event to
	 * @param event The event
	 */
	static void moveEvent(Track from, Track to, MidiEvent event){
		to.add(event);
		from.remove(event);
	}


}
