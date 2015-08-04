package tools;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.*;

public class Player {

	Sequencer seq;

	/**
	 * Use me to play a track from the beginning
	 */
	public Player(Sequence s) {
		try {
			if (s == null) {
				System.err.println("Sequencer not avaliable");
				return;
			} else {

				seq = MidiSystem.getSequencer();
				seq.open();
				seq.setSequence(s);
				seq.setLoopCount(1);
			}
		} catch (MidiUnavailableException | InvalidMidiDataException e) {
			System.err.println("Sequencer not avaliable");
		}

	}

	public void play() {
		System.out.println("Starting playback");

		seq.setTickPosition(1000);
		seq.start();
//		System.out.println(startTime+" : "+endTime+" Giving a play time of "+(endTime-startTime));
//		try {
//			Thread.sleep((seq.getMicrosecondLength()));
//			System.out.printf("start: %d, end: %d \n", seq.getLoopStartPoint(),seq.getLoopEndPoint());
//		} catch (InterruptedException e) {
//			System.err.println("Thread Interupted error");
//		}

	}


	public boolean stop(){
		if(seq.isRunning()){
			seq.stop();
			return true;
		}
		return false;

	}
//
//	public void play(Sequence s) {
//		try {
//			seq.setSequence(s);
//		} catch (InvalidMidiDataException e) {
//			System.err.println("Invalid midi data encountered");
//		}
//		setPoints(s);
//		System.out.println("Starting playback");
//		seq.setLoopStartPoint(startTime);
//		seq.setLoopEndPoint(endTime);
//		seq.start();
//		System.out.println(startTime+" : "+endTime+" Giving a play time of "+(endTime-startTime));
//	}


	public static void main(String[] args) {

		File midFile = new File("resources/stairway.mid");
		try {
			Sequence s = MidiSystem.getSequence(midFile);
			Player p = new Player(s);
			p.play();

			System.out.println("Done");

		} catch (InvalidMidiDataException e) {
			System.err.println("File is an invalid midi file");
		} catch (IOException e) {
			System.err.println("File failed to load");
		}
	}
}
