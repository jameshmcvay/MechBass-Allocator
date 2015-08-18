package ui;

import helperCode.OctaveShifter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import javafx.scene.control.TextArea;
import solver.Solver;
import solver.TrackSplitter;
import tools.Player;

public class Console extends OutputStream{

	boolean guiMode;
	TextArea area;
	BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
	String input = "i";
	UI ui;
	Sequence curMIDI;

	public Console(TextArea text, UI ui) {
		guiMode = true;
		area = text;
		this.ui = ui;
	}

	public Console() {
		guiMode = false;
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

		if(input.equals("stop")){
			playerStop();
			return;
		}

		if(input.equals("octUp")){
			OctaveShifter.shiftOctave(curMIDI, 3);
		}

	}

	protected void output(String text) {
			System.out.print(text + "\n");
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

	public static void playerRelease(){
		Player.release();
	}

	public void playerStop(){
		Player.stop();
	}

	protected void play() {
	    if(curMIDI != null)
		Player.play(curMIDI);
	}

	protected void solve() {
	   	if(curMIDI != null)
	   		try {
	            curMIDI = TrackSplitter.split(curMIDI, 4, 2);
	        } catch (InvalidMidiDataException e) {
	            e.printStackTrace();
	        }
		}

	public static void main(String args[]) {
		Console c = new Console();
		c.startTerminalInput();
	}

	@Override
	public void write(int i) throws IOException {
		area.appendText(String.valueOf((char) i));

	}
}
