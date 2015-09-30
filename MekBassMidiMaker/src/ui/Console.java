package ui;

import helperCode.OctaveShifter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Stack;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import javafx.scene.control.TextArea;
import solver.MekString;
import solver.Solver;
import solver.TrackSplitter;
import tools.Player;

/**
 * The console used in both the GUI and non-GUI modes, it is responsible for
 * receiving user's text input and displaying any text output.
 *
 * @author Patrick Byers
 *
 */
public class Console extends OutputStream {

	boolean guiMode;
	TextArea area;
	BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
	String input = "i";
	Slave slave;
	Stack<String> prevCommand = new Stack<String>();
	Stack<String> commandStack = new Stack<String>();
	String SelectedCommand;
	String allText;

	int state;
	int curString;
	int numStrings;
	boolean setup = false;
	solver.MekString string;
	solver.MekString[] strings;
	int lowNote;
	int highNote;
	long[] timing;
	String setupName;
	int i;
	long prepTime;
	long prepSize;
	
	// Valid Commands so far:
	String[] validCommands = {"d",		   // Sets the configuration to default - perfect for hysteria. 
	                          "open",	   // Opens a MIDI file (specify filepath and name of file after open).
	                          "openConfig",// Opens a config file (specify filepath and name of file after openConfig).
	                          "solve",	   // Works the magic of the program to let the MIDI file play on MechBass.
	                          "play",	   // Plays the sequence
	                          "stop",	   // Stops the sequence
	                          "octUp",	   // Shifts the Octave of all the notes in the sequence up by 3
	                          "octDown",   // Shifts the Octave of all the notes in the sequence down by 3
	                          "save",	   // Saves the current sequence.
	                          "saveConfig",// Saves the current configuration.
	                          "setup",	   // ???
	                          "config",	   // ???
	                          "help",	   // A SUPER INSPIRING MESSAGE TO FILL THE MEEKEST HEART WITH THE COURAGE OF A LION.
	                          "END"		   // Ends the program's eternal misery and suffering at last. Such benevolence.
	                          			   // ANYTHING ELSE: NAH BRUH.
	};

	/**
	 * Create a console using for use within the gui.
	 *
	 * @param text
	 *            The text of the GUI for this console to use
	 * @param slave
	 *            The slave instance
	 */
	public Console(TextArea text, Slave slave) {
		guiMode = true;
		area = text;
		this.slave = slave;
	}

	/**
	 * Create a console for use without a GUI.
	 *
	 * @param slave
	 *            The slave instance.
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
					read(input);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!input.equals("END"));
	}

	protected void read(String text) {
		if (text.equals("") || text == null || text.equals("\n")) {
			area.setText("No command input");
			area.appendText("");
			return;
		}
		String[] lines = text.split("\n");
		String rawInput = lines[lines.length - 1].trim();
		if (setup) {
			setupParse(rawInput);
		} else {
			parse(rawInput);
		}
	}

	protected void setupParse(String text) {
		switch (state) {
		case 0:
			setupName = text;
			state++;
			output("Input the prepositioning timing");
			break;
		case 1:
			prepTime = (long) Integer.parseInt(text);
			output("input the prepositioning length");
			state++;
			break;
		case 2:
			prepSize = (long) Integer.parseInt(text);
			output("input the number of strings");
			state++;
			break;
		case 3:
			curString = 1;
			numStrings = Integer.parseInt(text);
			output("Please input the lowest note of string " + curString);
			strings = new solver.MekString[numStrings];
			state++;
			break;
		case 4:
			lowNote = Integer.parseInt(text);
			output("Please input the highest note of string " + curString);
			state++;
			break;
		case 5:
			highNote = Integer.parseInt(text);
			output("Please input the timing between note " + lowNote + " and "
					+ (lowNote + 1) + " on string " + curString);
			state++;
			timing = new long[highNote - lowNote];
			i = 0;
			break;
		case 6:
			if (i < timing.length - 1) {
				timing[i] = Long.parseLong(text);
				i++;
				output("Please input the timing between note " + (lowNote + i)
						+ " and " + (lowNote + i + 1) + " on string "
						+ curString);
			} else {
				timing[i] = Long.parseLong(text);
				string = new MekString(lowNote, highNote, timing);
				strings[curString - 1] = string;
				if (curString < numStrings) {
					curString++;
					output("Please input the lowest note of string "
							+ curString);
					state = 4;
				} else {
					state = 0;
					setup = false;
					Slave.setSettings(setupName, prepTime, prepSize, strings);
					output("Setup complete");
				}
			}
			break;
		}
	}

	/**
	 * Parse a block of input
	 *
	 * @param text
	 *            The block of input
	 */
	protected void parse(String rawInput) {
		String[] input = rawInput.split("\\s+");
		String command = input[0];
		commandStack.push(rawInput);
		switch (command) {
		case "d":
			openConfig("openConfig default.csv");
			break;
		case "open":
			if (input.length > 1)
				open(rawInput);
			else
				output("no file specified");
			break;
		case "openConfig":
			if (input.length > 1)
				openConfig(rawInput);
			else
				output("no file specified");
			break;
		case "solve":
			if (input.length > 1) {
				this.solve(rawInput);
			} else
				slave.solve();
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
			if (input.length > 1) {
				this.save(rawInput);
			} else
				this.save();
			break;
		case "saveConfig":
			if (input.length > 1) {
				this.saveConfig(rawInput);
			} else
				this.saveConfig();
			break;
		case "setup":
			setup = true;
			output("input the name of the setup");
			break;
		case "config":
			Slave.getConfig();
			break;
		case "help":
			output("There is no help all hope is lost");
		case "END":
			System.exit(0);
		default:
			output("Command not recongnized");
		}
		prevCommand = commandStack;
		// allText = area.getText();
	}

	/**
	 * Returns whether or not the console is set to GUI mode
	 *
	 * @return GUI mode
	 */
	protected boolean getGUIMode() {
		return guiMode;
	}

	protected void prevCommand() {
		area.clear();
		this.SelectedCommand = prevCommand.pop();
		output(allText + SelectedCommand);
	}

	protected void open(String input) {
		String FileName = input.substring(4).replace("\"", "").trim();
		Slave.setCurMIDI(FileName);
	}

	protected void openConfig(String input) {
		String FileName = input.substring(10).replace("\"", "").trim();
		File fi = new File(FileName);
		if (fi != null) {
			Slave.parse(fi);
		}
	}

	protected void saveConfig() {
		output("saving config as \"default.csv\" in the current directory");
		File fi = new File("default.csv");
		Slave.saveConfig(fi);
	}

	protected void saveConfig(String input) {
		String fileName = input.substring(10).replace("\"", "").trim();
		File fi = new File(fileName);
		Slave.saveConfig(fi);
	}

	protected void save(String input) {
		String fileName = input.substring(4).replace("\"", "").trim();
		Slave.save(fileName);
	}

	protected void save() {
		output("saving file as \"out.mid\" in the current directory");
		Slave.save("out.mid");
	}

	protected void solve(String input) {
		String FileName = input.substring(5).replace("\"", "").trim();
		if (Slave.setCurMIDI(FileName)) {
			slave.solve();
		}
	}

	/**
	 * Output the string to the appropriate area
	 *
	 * @param text
	 */
	protected void output(String text) {
		for (char c : text.toCharArray()) {
			try {
				write((int) c);
			} catch (IOException e) {
				System.out.write((int) c);
			}
		}
		try {
			write('\n');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.write('\n');
		}
	}

	@Override
	public void write(int i) throws IOException {
		if (guiMode)
			area.appendText(String.valueOf((char) i));
		else
			System.out.write(i);
	}
}
