package tools;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.*;

public class Player {

	private Sequencer seq;
	private long startTime;
	private long endTime;

	public Player() {
		try {
			seq = MidiSystem.getSequencer();

			if (seq == null) {
				System.err.println("Sequencer not avaliable");
				return;
			} else {
				seq.open();
			}
		} catch (MidiUnavailableException e) {
			System.err.println("Sequencer not avaliable");
		}
		seq.setLoopCount(1);
	}

//	public void play(Sequence s) {
//		try {
//			seq.setSequence(s);
//		} catch (InvalidMidiDataException e) {
//			System.err.println("Invalid midi data encountered");
//		}
//		setPoints(s);
//		System.out.println("Starting playback");
//		seq.start();
//		System.out.println(startTime+" : "+endTime+" Giving a play time of "+(endTime-startTime));
//		try {
//			Thread.sleep((endTime-startTime)/1000 + 1);
//		} catch (InterruptedException e) {
//			System.err.println("Thread Interupted error");
//		}
//		seq.stop();
//		System.out.println("Stopped");
//	}
//
	public void play(Sequence s) {
		try {
			seq.setSequence(s);
		} catch (InvalidMidiDataException e) {
			System.err.println("Invalid midi data encountered");
		}
		setPoints(s);
		System.out.println("Starting playback");
		seq.setLoopStartPoint(startTime);
		seq.setLoopEndPoint(endTime);
		seq.start();
		System.out.println(startTime+" : "+endTime+" Giving a play time of "+(endTime-startTime));
	}


	private void setPoints(Sequence s){
		startTime = Timing.getMinTime(s);
		endTime = Timing.getMaxTime(s);
	}

	public static void main(String[] args) {
		Player p = new Player();
		File midFile = new File("resources/Twinkle_Twinkle_in_octaves.mid");
		try {
			Sequence s = MidiSystem.getSequence(midFile);
			p.play(s);
		} catch (InvalidMidiDataException e) {
			System.err.println("File is an invalid midi file");
		} catch (IOException e) {
			System.err.println("File failed to load");
		}
	}
}