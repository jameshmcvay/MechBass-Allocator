package ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import javafx.scene.control.TextArea;
import solver.Solver;
import tools.Player;

public class Console {

	boolean guiMode;
	Sequence curMIDI;
	TextArea area;
	BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
	String input = "i";

	public Console(boolean gui, TextArea text) {
		guiMode = gui;
		area = text;
	}

	public Console(boolean gui) {
		guiMode = gui;
		if (guiMode) {

		}
	}

	protected void startTerminalInput() {
		do {
			try {
				input = buf.readLine();
				if(input != null){
				Parse(input);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (input != "END");
	}

	protected void Parse(String text) {
		String input = text.trim();

		if (input.equals("open"))
			output("No File Specified");
		else if (input.startsWith("open")) {
			input = input.substring(4).trim();
			if(setCurMIDI(input)) output("Successfully open file");
			else output("Failed to open file");
			return;
		}

		if (input.equals("solve")) {
			solve();
			return;
		}
		else if(input.startsWith("solve")){
			input = input.substring(5).trim();
			if(setCurMIDI(input)) output("Successfully open file");
			else output("Failed to open file");
			solve();
			output("successfully solved");
		}

		if (input.equals("play")) {
			play();
			return;
		}

	}

	protected void output(String text) {
		if (guiMode)
			area.setText(text+" \n");
		else
			System.out.println(text);
	}

	protected void solve() {
		if (curMIDI != null)
			curMIDI = Solver.solve(curMIDI);
	}

	protected void play() {
		if (curMIDI != null)
			Player.play(curMIDI);
	}

	protected boolean setCurMIDI(String path) {
		try {
			File fi = new File(path);
			if (fi != null){
				curMIDI = MidiSystem.getSequence(fi);
				return true;
			}
		} catch (IOException e) {
			output("The file was not found");
			return false;
		}
		catch (InvalidMidiDataException e) {
			output("Failed to convert to a sequence");
			return false;
		}
		return false;
	}

	public static void main(String args[]) {
		Console c = new Console(false);
		c.startTerminalInput();
	}

	public void stop() {
		playerStop();
		playerRelease();
	}

	public void playerRelease() {
		Player.release();
	}

	public void playerStop() {
		Player.stop();
	}
}
