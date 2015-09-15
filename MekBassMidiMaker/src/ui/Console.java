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

public class Console extends OutputStream {

	boolean guiMode;
	TextArea area;
	BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
	String input = "i";
	Slave slave;

	public Console(boolean gui, TextArea text, Slave slave) {
		guiMode = gui;
		area = text;
		this.slave = slave;
	}

	public Console() {
		guiMode = false;
	}

	protected void startTerminalInput() {
		do {
			try {
				input = buf.readLine();
				if (input != null) {
					Parse(input);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (input != "END");
	}

	protected void Parse(String text) {
		String[] lines = text.split("\n");
		String rawInput = lines[lines.length-1].trim();
		String[] input = rawInput.split("\\s+");
		String command = input[0];
		switch (command) {
		case "open":
			if(input.length>1)
			open(rawInput);
			else output("no file specified");
			break;
		case "solve":
			if(input.length>1){
				this.solve(rawInput);
			}
			else slave.solve();
			break;
		case "play":
			slave.play();
			break;
		case "stop":
			slave.playerStop();
			break;
		case "octUp":
			slave.octaveUp();
			break;
		case "octDown":
			slave.octaveDown();
			break;
		case "save":
			if (input.length>1){
				this.save(rawInput);
			}
			else this.save();
			break;
		default:
			output("Command not recongnized");
		}
		// if (input.equals("open"))
		// output("No File Specified");
		// else if (input.startsWith("open")) {
		// input = input.substring(4).trim();
		// if(slave.setCurMIDI(input)) output("Successfully open file");
		// else output("Failed to open file");
		// return;
		// }
		//
		// if (input.equals("solve")) {
		// slave.solve();
		// return;
		// }
		// else if(input.startsWith("solve")){
		// input = input.substring(5).trim();
		// if(slave.setCurMIDI(input)) output("Successfully open file");
		// else output("Failed to open file");
		// slave.solve();
		// output("successfully solved");
		// }
		//
		// if (input.equals("play")) {
		// slave.play();
		// return;
		// }
		//
		// if(input.equals("stop")){
		// slave.playerStop();
		// return;
		// }
		//
		// if(input.equals("octUp")){
		// Slave.octaveUp();
		// }
		//
	}

	protected boolean getGUIMode() {
		return guiMode;
	}

	protected void open(String input){
		String FileName = input.substring(4).replace("\"", "").trim();
		Slave.setCurMIDI(FileName);
	}

	protected void save(String input){
		String fileName = input.substring(4).replace("\"", "").trim();
		Slave.save(fileName);
	}

	protected void save(){
		output("saving file as \"out.mid\" in the current directory");
		Slave.save("out.mid");
	}

	protected void solve(String input){
		String FileName = input.substring(5).replace("\"", "").trim();
		if(Slave.setCurMIDI(FileName)){
			slave.solve();
		}
	}

	protected void output(String text) {
		System.out.print(text + "\n");
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
