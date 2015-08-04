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
	private static int lastString = -1;

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
						List<Integer> rightStrings = new ArrayList<Integer>();
						for (int j = 0; j < strings.length; j++){
							if (note > strings[j].lowNote && note < strings[j].highNote && lastNote[j] == -1) {
								rightStrings.add(j);
							}
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
							// put it in last note for that string
							lastNote[useString] = note;
							lastString = useString;
						}
						break;
					}
					// if the if was false, then it is actually a note off
				case NOTE_OFF:
					int note = shrtmsg.getData1();
					int useString = -1;
					for (int j=0; j < strings.length; ++j){
						if (lastNote[j]==note) useString=j;
					}
					if (useString==-1) break; // if the note wasn't played, dont do anything with it
					// now we have the correct string
					// so we add the noteoff to the correct track
					moveEvent(tr,seq.getTracks()[useString+1],tr.get(i));

					// and set the other stuff to nothing
					lastNote[useString] = -1;
					stringTimes[useString] = tr.get(i).getTick();
					lastString = -1;
					break;
				case PITCH_BEND:
					default:
					// if there is a string playing
					if (lastString >= 0) {
						// add it to that
						moveEvent(tr,seq.getTracks()[lastString+1],tr.get(i));
					}
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
	static void moveEvent(Track from, Track to, MidiEvent event){
		to.add(event);
		from.remove(event);
	}


}
