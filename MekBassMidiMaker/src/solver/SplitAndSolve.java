package solver;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import tools.Player;

/**
 *
 * @author Andrew Palmer
 *
 * Uses code written by Dean
 */
public class SplitAndSolve {


	/**
	 *
	 * @param seq
	 * @param tracks
	 * @param bass
	 */
	public SplitAndSolve(Sequence seq, int tracks,int bass){
		try{
			Sequence out = TrackSplitter.split(seq,tracks,bass);
			out = Solver.solve(out);
			int moved = 0;
			for(int i = 1; i<=tracks; i++){
				moved += out.getTracks()[i].size();
			}
			System.out.printf("Converting Track %d\n", bass);
			System.out.printf("Found %d moved events\n", moved);
			System.out.printf("Found %d unmoved events\n",out.getTracks()[0].size());
			Player.play(out);
		}
		catch(InvalidMidiDataException e){
			e.printStackTrace();
		}

	}

	public static void main(String[] args){
		File midiFile = new File(args[0]);
		Sequencer sequencer;
		Sequence sequence = null;
		try {
			sequencer = MidiSystem.getSequencer();
			// Set the sequence to be examined to the midiFile...
			sequencer.setSequence(MidiSystem.getSequence(midiFile));
			// ... And store the sequence.
			sequence = sequencer.getSequence();
		}
		catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
		catch (InvalidMidiDataException e){
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		SplitAndSolve sAndS =  new SplitAndSolve(sequence,4,1);

	}
}