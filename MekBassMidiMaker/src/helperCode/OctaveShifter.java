package helperCode;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import tools.Player;

/**
 *
 * This class exists solely to shift the octave up or down.<p>
 *
 * SOME RULES:<br>
 * 	key MUST stay between 0 - 127 (inclusive),<br>
 * 	octave MUST stay between 0 - 10 (inclusive),<br>
 * 	shift MUST ALSO stay between 0 - 10 (inclusive);<br>
 * 		HOWEVER, shift MUST NOT violate the above octave rule.
 * @author Dean Newberry
 * */

public class OctaveShifter {

	public final int KEY_MIN = 0;
	public final int OCTAVE_MIN = 0;
	public final int SHIFT_MIN = 0;

	public final int KEY_MAX = 127;
	public final int OCTAVE_MAX = 10;
	public final int SHIFT_MAX = 10;

	/**
	 *
	 * This method, when given a sequence will shift all the
	 * notes up the given number of octaves, OR if moving too
	 * far up or down, will shift the note as far as it will go.<p>
	 *
	 * For example, trying to shift key 13 down 2 octaves would
	 * make key go to -11, with an octave of -1. In this case, it
	 * would be shifted down only 1 octave to key = 1, octave = 0
	 * instead.<br>
	 * The reverse can happen too; if you try to shift key 109 up 2
	 * octaves, then the key would be 133 and the octave would be 11.
	 * In this case, it would be shifted up only 1 octave to key = 121,
	 * octave = 10 instead.<p>
	 *
	 * If the above example occurs, a message will be printed to warn
	 * the user - if this happens too much, the result will be a very
	 * flat sound part/file.<p>
	 *
	 * <b>REQUIRES:</b> A Sequence with playable notes and an integer for the
	 * number of octaves to shift (between 0 - 10 (inclusive)).<br>
	 * <b>ENSURES:</b> Every note in the Sequence adjusted by the "shift", except
	 * where this shift causes key or octave to go out of bounds.<br>
	 * 		- In this case, a warning will be displayed and the note will
	 * 		  be shifted to the minimum/maximum octave possible.
	 * @param seq A Sequence with playable notes.
	 * @param shift An integer for the number of octaves to shift (between 0 - 10 (inclusive))
	 * @return The passed in Sequence
	 * @author Dean Newberry
	 * */
	public static Sequence shiftOctave(Sequence seq, int shift){
		for (Track t : seq.getTracks()){
			for (int i=0; i < t.size(); i++) {
				shift_A_Note(seq, shift, t, i);
			}
		}
		return seq;
	}

	private static void shift_A_Note(Sequence seq, int shift, Track t, int i){
		MidiEvent event = t.get(i);
		MidiMessage message = event.getMessage();
		if (message instanceof ShortMessage) {
			ShortMessage sm = (ShortMessage) message;
			if (sm.getCommand() == ShortMessage.NOTE_OFF||
				sm.getCommand() == ShortMessage.NOTE_ON	||
				sm.getCommand() == ShortMessage.POLY_PRESSURE){
				boolean warnUser = false;
				int tempShift = shift;
				if (sm.getData1() + (tempShift * 12) >= 0 &&
						sm.getData1() + (tempShift * 12) <= 127){
					try {
						sm.setMessage(sm.getCommand(), sm.getChannel(),
						sm.getData1() + (tempShift * 12), sm.getData2());
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
						System.out.println("I *EXPECTED* to make a change to a "
								+ "ShortMessage where the command was NOTE_OFF, "
								+ "NOTE_ON or POLY_PRESSURE (Pitch Bend).");
					}
				}
				else if ((sm.getData1() + (tempShift * 12) < 0 ||
						sm.getData1() + (tempShift * 12) > 127)){
					warnUser = true;
					if (tempShift > 0){
						while(tempShift > 0){
							tempShift--;
							if (sm.getData1() + (tempShift * 12) >= 0 &&
									sm.getData1() + (tempShift * 12) <= 127){
								try {
									sm.setMessage(sm.getCommand(), sm.getChannel(),
									sm.getData1() + (tempShift * 12), sm.getData2());
									break;
								} catch (InvalidMidiDataException e) {
									System.out.println("I *EXPECTED* to make a change to a "
											+ "ShortMessage where the command was NOTE_OFF, "
											+ "NOTE_ON or POLY_PRESSURE (Pitch Bend).");
								}
							}
						}
					}
					else{
						while(tempShift < 0){
							tempShift++;
							if (sm.getData1() + (tempShift * 12) >= 0 &&
									sm.getData1() + (tempShift * 12) <= 127){
								try {
									sm.setMessage(sm.getCommand(), sm.getChannel(),
									sm.getData1() + (tempShift * 12), sm.getData2());
									break;
								} catch (InvalidMidiDataException e) {
									e.printStackTrace();
									System.out.println("I *EXPECTED* to make a change to a "
											+ "ShortMessage where the command was NOTE_OFF, "
											+ "NOTE_ON or POLY_PRESSURE (Pitch Bend).");
								}
							}
						}
					}
				}
				if (warnUser){
					System.out.printf("!!!\tWARNING! FAILED TO SHIFT NOTE "
					+ "%d OCTAVES! SHIFTED THE NOTE %d OCTAVES INSTEAD!\n!!!",
					shift, tempShift);
				}
			}
		}
	}

	public static void main(String args[]){
		File f =  new File("resources/DragonForce_Through-the-Fire-and-Flames.mid");
		try {
			Sequence s = MidiSystem.getSequence(f);
//			shiftOctave(s, -3);
			shiftOctave(s, 100);

			Player.play(s);

		} catch (InvalidMidiDataException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
