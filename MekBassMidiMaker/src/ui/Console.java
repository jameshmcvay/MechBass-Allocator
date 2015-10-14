package ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import solver.Conflict;
import solver.MekString;

/**
 * The console used in both the GUI and non-GUI modes, it is responsible for
 * receiving user's text input and displaying any text output.
 *
 * @author Patrick Byers
 *
 */
public class Console extends OutputStream {

	boolean guiMode; //whether or not we are using the console with a GUI
	
	//GUI text fields
	TextField textInputField;
	TextArea textOutputField;
	
	//Buffered reader for parsing input when in console mode
	BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
	String input = "i";
	
	//Fields for use with previous and next command features
	Stack<String> prevCommand = new Stack<String>(); //The commands called before the currently selected command
	Stack<String> commandStack = new Stack<String>(); //All the commands executed by the program
	Stack<String> nextCommand = new Stack<String>(); //The commands called after the currently selected commands
	String SelectedCommand; //The currently selected command


	//Fields for use in setup of configuration
	int state; //state of configuration setup we are currently in
	int curString; //the string we are currently setting in the configuration
	int numStrings; //the total number of strings for configuration
	boolean setup = false;//whether we are parsing commands straight to setup
	solver.MekString string; //A MekString to work on
	solver.MekString[] strings;//An array to store completed strings
	int lowNote; 
	int highNote;
	long[] timing; //An array to store string timings
	String setupName;
	int i;
	long prepTime;//Preposition time
	long prepSize;//Preposition length

	/*
	 * Start of Dean's code
	 */
	// Valid Commands so far:
	String[] validCommands = { "d", // Sets the configuration to default -
									// perfect for hysteria.
			"open", // Opens a MIDI file (specify filepath and name of file
					// after open).
			"openConfig",// Opens a config file (specify filepath and name of
							// file after openConfig).
			"solve", // Works the magic of the program to let the MIDI file play
						// on MechBass.
			"play", // Plays the sequence
			"stop", // Stops the sequence
			"octUp", // Shifts the Octave of all the notes in the sequence up by
						// 3
			"octDown", // Shifts the Octave of all the notes in the sequence
						// down by 3
			"save", // Saves the current sequence.
			"saveConfig",// Saves the current configuration.
			"setup", // ???
			"config", // ???
			"help", // A SUPER INSPIRING MESSAGE TO FILL THE MEEKEST HEART WITH
					// THE COURAGE OF A LION.
			"END" // Ends the program's eternal misery and suffering at last.
					// Such benevolence.
					// ANYTHING ELSE: NAH BRUH.
	};
	/*
	 * End of Dean's code
	 */

	//int resolution;
	//boolean fix = false;
	//List<NoteConflict> listOfNoteConflicts = new ArrayList<NoteConflict>();
	//Conflict curConflict;
	//Map<Integer, NoteConflict> corrections = new HashMap<Integer, NoteConflict>();

	List<Conflict> listOfConflicts;

	/**
	 * Create a console using for use within the GUI.
	 *
	 * @param text
	 *            The text of the GUI for this console to use
	 */
	public Console(TextField input, TextArea output) {
		guiMode = true;
		textInputField = input;
		textOutputField = output;
	}

	/**
	 * Create a console for use without a GUI.
	 */
	public Console() {
		guiMode = false;
		startTerminalInput();
	}

	/**
	 * start a loop to check for input from the console.
	 */
	private void startTerminalInput() {
		do {
			try {
				input = buf.readLine();
				if (input != null) {
					read(input);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (!input.equals("END"));
	}

	/**
	 * parse input to check for and redirect special cases.
	 *
	 * @param text The string of text to parse
	 */
	protected void read(String text) {
		if (text.equals("") || text == null || text.equals("\n")) {
			textInputField.setText("No command input");
			textInputField.appendText("");
			return;
		}
		String rawInput = text.trim();
		if (guiMode) {
			output(rawInput);
			textInputField.clear();
		}
		if (setup) {
			setupParse(rawInput);
		} else {
			parse(rawInput);
		}
	}

	/**
	 * run through the setup process.
	 *
	 * @param text The string of text to parse
	 */
	private void setupParse(String text) {
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
	 * Parse regular commands from the input.
	 *
	 * @param text
	 *            The string of text to parse
	 */
	private void parse(String rawInput) {
		String[] input = rawInput.split("\\s+");
		String command = input[0].toLowerCase();
		commandStack.push(rawInput);
		switch (command) {
		case "d":
			openConfig("openConfig default.csv");
			break;
		case "load":
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
				listOfConflicts = Slave.solve();
			break;
//		case "fix":
//			listOfConflicts = slave.getConflicts();
//			fix = true;
//			correctError(rawInput);
//			break;
		case "play":
			Slave.play();
			break;
		case "stop":
			Slave.playerStop();
			break;
		case "octUp":
			Slave.octaveUp();
			break;
		case "octDown":
			Slave.octaveDown();
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
		case "clean":
			Slave.clean();
			break;
		case "basstrack":
			if (input.length > 1) {
				this.setBassTrack(rawInput);
			} else
				output("Bass track is set to track " + Slave.getBassTrack());
			break;
		case "help":
			output("There is no help all hope is lost");
		case "end":
			System.exit(0);
			break;
		default:
			output("Command not recongnized");
		}
		prevCommand = commandStack;
		nextCommand.empty();
	}

	/**
	 * Returns whether or not the console is set to GUI mode.
	 *
	 * @return GUI mode
	 */
	private boolean getGUIMode() {
		return guiMode;
	}

	/**
	 * Parse a open command to extract the file and open it.
	 *
	 * @param input The open command of the form "open <file path>"
	 */
	protected void open(String input) {
		String FileName = input.substring(4).replace("\"", "").trim();
		Slave.setCurMIDI(FileName);
	}

	/**
	 * Parse an openConfig command to extract the configuration file to open and set the configuration file.
	 *
	 * @param input The openConfig command of the form "openConfig <file path>"
	 */
	protected void openConfig(String input) {
		String FileName = input.substring(10).replace("\"", "").trim();
		File fi = new File(FileName);
		if (fi != null) {
			Slave.parse(fi);
		}
	}

	/**
	 * Save the current configuration as default.csv in the current directory.
	 */
	private void saveConfig() {
		output("saving config as \"default.csv\" in the current directory");
		File fi = new File("default.csv");
		Slave.saveConfig(fi);
	}

	/**
	 * Save the current configuration as the file specified.
	 *
	 * @param input The saveConfig command of the form "saveConfig <file name>"
	 */
	private void saveConfig(String input) {
		String fileName = input.substring(10).replace("\"", "").trim();
		File fi = new File(fileName);
		Slave.saveConfig(fi);
	}

	/**
	 * set the bass track to the specified number.
	 *
	 * @param input The BassTrack command of the form "basstrack <track number>"
	 */
	private void setBassTrack(String input) {
		int track =Slave.getBassTrack();
		try{
		track = Integer.parseInt(input.substring(9).replace("\"", "")
				.trim());
		}
		catch (NumberFormatException e){
			output("you done goofed");
			return;
		}
		Slave.setBassTrack(track);
		output("Bass Track is now set to track " + track);
	}

	/**
	 * save the current MIDI file as the file specified.
	 *
	 * @param input The save command of the form "save <file path>"
	 */
	private void save(String input) {
		String fileName = input.substring(4).replace("\"", "").trim();
		Slave.save(fileName);
	}

	/**
	 * save the current midi file as the default "out.mid" in the current directory.
	 */
	private void save() {
		output("saving file as \"out.mid\" in the current directory");
		Slave.save("out.mid");
	}
	/**
	 * load the a MIDI into current MIDI and solve it.
	 *
	 * @param input the file to be solve of the form "solve <file name>
	 */
	private void solve(String input) {
		String FileName = input.substring(5).replace("\"", "").trim();
		if (Slave.setCurMIDI(FileName)) {
			listOfConflicts = Slave.solve();
		}
	}

//	protected void correctError(String input) {
//		if (listOfConflicts.size() == 0) {
//			output("No conflicts founds, no need to fix");
//			fix = false;
//			return;
//		}
//		switch (resolution) {
//		case 0:
//			Boolean solving = false;
//			for (Conflict c : listOfConflicts) {
//				if ((!c.resolved()) && (c.strings() != 0)) {
//					solving =true;
//					curConflict = c;
//					listOfNoteConflicts = c.getConf();
//					resolution++;
//					c.markResolved();
//					break;
//				}
//			}
//			if (!solving) {
//				fix = false;
//				output("all errors fixed");
//				return;
//			}
//		case 1:
//			output("The note "
//					+ curConflict.getNote().getMessage().getMessage()[1]
//					+ " is conflicting");
//			output("The options are to:");
//			int i = 1;
//			corrections.clear();
//			for (NoteConflict nc : listOfNoteConflicts) {
//				corrections.put(i, nc);
//				corrections.put(i + 1, nc);
//				corrections.put(i + 2, nc);
//				corrections.put(i + 3, nc);
//				output("option " + i + ", drop the note before in track "
//						+ nc.string());
//				output("option " + (i + 1) + ", drop the note after in track "
//						+ nc.string());
//				output("option " + (i + 2)
//						+ ", advance the end of the note before in track "
//						+ nc.string());
//				output("option " + (i + 3)
//						+ ", delay the start of the note after in track "
//						+ nc.string());
//				i = i + 4;
//			}
//			output("type in the number of the option you would like to carryout");
//			resolution++;
//			return;
//		case 2:
//			int opt = Integer.parseInt(input);
//			int Action = opt % 4;
//			NoteConflict nCon = corrections.get(opt);
//			switch (Action) {
//			case 0:
//				nCon.dropFirst();
//				break;
//			case 1:
//				nCon.dropLast();
//				break;
//			case 2:
//				nCon.delayFirstEnd(1);
//				break;
//			case 3:
//				nCon.delaySecondStart(1);
//				break;
//			}
//			resolution = 0;
//			correctError("");
//		}
//	}

	/**
	 * Output the string to the appropriate area.
	 *
	 * @param text the string of text to output to the appropriate area
	 */
	private void output(String text) {
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
			System.out.write('\n');
		}
	}
	/**
	 * set the command in the input field to be the command issued previous to the one currently displayed there.
	 */
	protected void CallPrevious() {
		if (!prevCommand.isEmpty()) {
			nextCommand.push(textInputField.getText());
			String command = prevCommand.pop();
			textInputField.setText(command);
			textInputField.positionCaret(textInputField.getLength());
		}
	}
	/**
	 * set the command in the input field to be the command issued after the one currently displayed
	 */
	protected void callNext(){
		if (!nextCommand.isEmpty()) {
			prevCommand.push(textInputField.getText());
			String command = nextCommand.pop();
			textInputField.setText(command);
			textInputField.positionCaret(textInputField.getLength());
		}
	}

	@Override
	public void write(int i) throws IOException {
		if (guiMode)
			textOutputField.appendText(String.valueOf((char) i));
		else
			System.out.write(i);
	}
}
