package tools;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.*;
/**
 * This class creates a MIDI player for a given sequence obtains from the java MIDI Library.
 * @author Dylan Macdonald, Patrick Bayers
 *
 */
public class Player {
	static private Sequencer seq;

	/**
	 * Start audio output. Loops once.
	 */
	public static void play(Sequence s) {
	    if(seq == null){
		play(s,0);
	    }
	}

	/**
	 * Start audio playback from a given point along the track.
	 */
	public static void play(Sequence s, long microSeconds){
		assert(s != null);
		try {
			seq = MidiSystem.getSequencer();
			seq.open();
			seq.setSequence(s);
			seq.setMicrosecondPosition(microSeconds);

			seq.start();
		} catch (MidiUnavailableException | InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Cease audio playback. Does not release system resources
	 * @return Whether the sequencer was stopped
	 */
	public static boolean stop(){
		if(seq != null && seq.isRunning()){
			seq.stop();
			return true;
		}
		return false;
	}

	/**
	 * Release system resources taken by the sequencer
	 */
	public static void release(){
		if(seq == null){
			return;
		}
		if(seq.isRunning()){
			seq.stop();
		}
		seq.close();
	}

	public static void main(String[] args) {

		File midFile = new File("resources/stairway.mid");
		try {
			Sequence s = MidiSystem.getSequence(midFile);
			play(s,400000000);

		} catch (InvalidMidiDataException e) {
			System.err.println("File is an invalid midi file");
		} catch (IOException e) {
			System.err.println("File failed to load");
		}
	}
}