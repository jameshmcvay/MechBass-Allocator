package solver;

import javax.sound.midi.Sequence;

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
	 * Adds pre positioning notes to the MIDI so MekBass can play it
	 * @param seq
	 * @return
	 */
	public static Sequence prePos(Sequence seq){
		//TODO: implement
		return seq;
	}
}
