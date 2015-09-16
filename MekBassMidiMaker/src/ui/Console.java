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

/**
 * The console used in both the GUI and non-GUI modes, it is responsible for receiving user's text input
 * and displaying any text output.
 * @author Patrick Byers
 *
 */
public class Console extends OutputStream {

	boolean guiMode;
	TextArea area;
	BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
	String input = "i";
	Slave slave;
/**
 * Create a console using for use within the gui.
 * @param text The text of the GUI for this console to use
 * @param slave The slave instance
 */
	public Console(TextArea text, Slave slave) {
		guiMode = true;
		area = text;
		this.slave = slave;
	}
/**
 * Create a console for use without a GUI.
 * @param slave The slave instance.
 */
	public Console(Slave slave) {
		guiMode = false;
		this.slave = slave;
		startTerminalInput();
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
		} while (!input.equals("END"));
	}
/**
 * Parse a block of input
 * @param text The block of input
 */
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
		case "END":
			System.exit(0);
		default:
			output("Command not recongnized");
		}
	}
/**
 * Returns whether or not the console is set to GUI mode
 * @return GUI mode
 */
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
	/**
	 * Output the string to the appropriate area
	 * @param text
	 */
	protected void output(String text) {
		System.out.print(text + "\n");
	}

	@Override
	public void write(int i) throws IOException {
		area.appendText(String.valueOf((char) i));

	}
}
