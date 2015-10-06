package solver;

import java.io.File;

import solver.GreedySolver;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import tools.Player;

/**
 * Uses code written by Dean
 *
 * @author Andrew Palmer
 *
 */
public class SplitAndSolve {
	private static MekString[] strings = new MekString[]{ new MekString(43, 56), new MekString(38, 51),
		new MekString(33, 46), new MekString(28, 41)};

	/**
	 *
	 * @param seq
	 * @param tracks
	 * @param bass
	 */
	public SplitAndSolve(Sequence seq, int tracks,int bass){
		try{
			for(MekString m: strings){
				m.initTimings(300);
			}
			Solver blah =  new GreedySolver();
			Sequence out = TrackSplitter.split(seq,tracks,bass);
			out = blah.solve(out);
			int moved = 0;
			for(int i = 1; i<=tracks; i++){
				moved += out.getTracks()[i].size();
			}
			System.out.printf("Converting Track %d\n", bass);
			System.out.printf("Found %d moved events\n", moved);
			System.out.printf("Found %d unmoved events\n",out.getTracks()[0].size());
//			seq.deleteTrack(seq.getTracks()[bass]);
//			out.deleteTrack(out.getTracks()[0]);
			out = Cleaner.prePos(out, 50, strings, 14);

			try {
				out = Cleaner.clean(out);
				MidiSystem.write(out, 1, new File("hyst.mid"));
			} catch (IOException e) {
				System.out.println("Write Failed\n");
				e.printStackTrace();
			}

			Player.release();
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
			System.out.println("Midi Unavailable\n");
			e.printStackTrace();
		}
		catch (InvalidMidiDataException e){
			System.out.println("Midi Invalid\n");
			e.printStackTrace();
		}
		catch (IOException e) {
			System.out.println("IO Exception\n");
			e.printStackTrace();
		}
		SplitAndSolve sAndS =  new SplitAndSolve(sequence,4,9);
	}
}