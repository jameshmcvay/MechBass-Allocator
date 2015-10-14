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

	public static final int KEY_MIN = 0;
	public static final int OCTAVE_MIN = 0;
	public static final int SHIFT_MIN = 0;

	public static final int KEY_MAX = 127;
	public static final int OCTAVE_MAX = 10;
	public static final int SHIFT_MAX = 10;

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
	 * @param shift An integer for the number of octaves to shift
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

	/**
	 * This method shifts a single note up or down a number of octaves.
	 * 
	 * @param seq A Sequence with playable notes.
	 * @param shift An integer for the number of octaves to shift
	 * @param t The track we're getting the event and message from.
	 * @param i The index of the event we want inside t.
	 * @return The passed in Sequence
	 * @author Dean Newberry
	 * */
	private static void shift_A_Note(Sequence seq, int shift, Track t, int i){
		MidiEvent event = t.get(i); // grab the event from the track...
		MidiMessage message = event.getMessage(); // ... grab the Message from the event...
		if (message instanceof ShortMessage) { // ... if the Message is a ShortMessage...
			ShortMessage sm = (ShortMessage) message; // ... make the Message a ShortMessage.
			// ONLY do something else IFF the ShortMessage is a about a pitch bend or a note on/off.
			if (sm.getCommand() == ShortMessage.NOTE_OFF|| 
				sm.getCommand() == ShortMessage.NOTE_ON	||
				sm.getCommand() == ShortMessage.POLY_PRESSURE){
				boolean warnUser = false; // Set up a flag in case we want to warn the user.
				// Warnings occur when the user tries to shift a note too far.
				int tempShift = shift;
				// If we can shift the octave...
				if (sm.getData1() + (tempShift * 12) >= 0 &&
						sm.getData1() + (tempShift * 12) <= 127){
					try {
						// ... set the ShortMessage to itself, with the octave shifted.
						sm.setMessage(sm.getCommand(), sm.getChannel(),
						sm.getData1() + (tempShift * 12), sm.getData2());
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
						System.out.println("I *EXPECTED* to make a change to a "
								+ "ShortMessage where the command was NOTE_OFF, "
								+ "NOTE_ON or POLY_PRESSURE (Pitch Bend).");
					}
				}
				// If we can't shift the octave... 
				else if ((sm.getData1() + (tempShift * 12) < 0 ||
						sm.getData1() + (tempShift * 12) > 127)){
					// Warn the user.
					warnUser = true;
					if (tempShift > 0){
						// Keep trying to shift the octave up until you hit 0 
						// or you succeed.
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
						// Keep trying to shift the octave down until you hit 0
						// or you succeed.
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
				// Warn the user if we aren't able to shift the desired number of octaves.
				if (warnUser){
					System.out.printf("!!!\tWARNING! FAILED TO SHIFT NOTE "
					+ "%d OCTAVES! SHIFTED THE NOTE %d OCTAVES INSTEAD!\n!!!",
					shift, tempShift);
				}
			}
		}
	}

	public static void main(String args[]){
		File f =  new File("resources/hysteria Allocated.mid");
		try {
			Sequence s = MidiSystem.getSequence(f);
//			shiftOctave(s, -2);
			shiftOctave(s, 4);

			Player.play(s);

		} catch (InvalidMidiDataException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
