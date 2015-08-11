package solver;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;
import static javax.sound.midi.ShortMessage.PITCH_BEND;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *
 * @author Andrew Palmer
 *
 *cleaner cuts the excess events after solving and adds pre-positioning.
 */
public class Cleaner {

	/**
	 * Takes a sequence and removes track 0. Run a MIDI through this after solving to\
	 * remove unalocated events
	 * @param seq - The Sequence
	 * @return the cleaned Sequence
	 */
	public static Sequence clean(Sequence seq){
		seq.deleteTrack(seq.getTracks()[0]);
		return seq;
	}
	
	/**
	 * Scans a track for any events which can't be played due to transition interval
	 * @param seq - The sequence to be checked
	 * @param inter - The transition interval in s^(-6)
	 * @param strings - the information on notes played by and intervals on the strings
	 * @return the number of conflicting notes
	 */
	public static int scanTimings(Sequence seq, int inter, MekString[] strings){
		int conflicts = 0;
		//for each track
		for(int i = 0; i < seq.getTracks().length; i++){
			Track cur = seq.getTracks()[i];
			//check that each note on doesn't conflict with the previous note off
			for(int j = cur.size(); j == 0; --j){
				MidiMessage midNoteOn = cur.get(j).getMessage();
				if (midNoteOn instanceof ShortMessage){
					ShortMessage noteOn = (ShortMessage) midNoteOn;
					// check what kind of command it is (note on/off, etc)
					if(noteOn.getCommand() == NOTE_ON){
						// check whether or not it is actually a note on...
						if (noteOn.getData2()!=0 && noteOn.getData2()!=1){
							int note1 = noteOn.getData1();
							//find the previous note off
							MidiMessage midNoteOff;
							for(int k = j; k == 0; --k){
								midNoteOff = cur.get(k).getMessage();
								if(midNoteOff instanceof ShortMessage){
									ShortMessage noteOff = (ShortMessage) midNoteOff;
									if(noteOff.getCommand() == NOTE_OFF){
										
									}
								}
							}
						}
					}
				}
			}
		}
		return conflicts;
	}

	/**
	 * Adds pre positioning notes to the MIDI so MekBass can play it
	 * @param seq
	 * @return
	 */
	public static Sequence prePos(Sequence seq){
		//TODO: implement
		return seq;
	}
}
