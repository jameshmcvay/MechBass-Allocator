package ui;

import helperCode.OctaveShifter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import javafx.scene.control.TextArea;
import solver.Solver;
import solver.TrackSplitter;
import tools.Player;

public class Console {

	boolean guiMode;
	TextArea area;
	BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
	String input = "i";
	Slave slave;

	public Console(boolean gui, TextArea text,Slave slave) {
		guiMode = gui;
		area = text;
		this.slave = slave;
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
			if(slave.setCurMIDI(input)) output("Successfully open file");
			else output("Failed to open file");
			return;
		}

		if (input.equals("solve")) {
			slave.solve();
			return;
		}
		else if(input.startsWith("solve")){
			input = input.substring(5).trim();
			if(slave.setCurMIDI(input)) output("Successfully open file");
			else output("Failed to open file");
			slave.solve();
			output("successfully solved");
		}

		if (input.equals("play")) {
			slave.play();
			return;
		}

		if(input.equals("stop")){
			slave.playerStop();
			return;
		}

		if(input.equals("octUp")){
			Slave.octaveUp();
		}

	}

	protected boolean getGUIMode(){
		return guiMode;
	}

	protected void output(String text) {
		if (getGUIMode())
			area.setText(text+" \n");
		else
			System.out.println(text);
	}


	public static void main(String args[]) {
		Console c = new Console(false);
		c.startTerminalInput();
	}
}
