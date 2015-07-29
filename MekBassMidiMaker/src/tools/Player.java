package tools;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.*;

public class Player {

	private Sequencer seq;

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
	}

	public void play(Sequence s) {
		try {
			seq.setSequence(s);
		} catch (InvalidMidiDataException e) {
			System.err.println("Invalid midi data encountered");
		}
		System.out.println("Starting playback"); 
		seq.start();
	}

	public static void main(String[] args) {
		Player p = new Player();
		File midFile = new File("resources/stairway.mid");
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
