package ui;

import helperCode.OctaveShifter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import solver.Cleaner;
import solver.Conflict;
import solver.MekString;
import solver.GreedySolver;
import solver.OOGreedySolver;
import solver.Solver;
import solver.TrackSplitter;
import tools.Player;

/**
 * Slave is a Go-between class; it works closely with other classes to make the
 * program work.
 * */
public class Slave {

	private static Sequence curMIDI; // The Current MIDI Sequence (NOT file)
	private static UI UI;
	private static Console console;
	private static boolean playing = false; // Whether the UI is playing a file right now or not.

	private static Simulation sim;
	private static String name = ""; // Name of the MIDI file.
	private static long prepositionLength;
	private static long prepositionDelay;
	private static MekString[] setOfStrings;
	private static int bassTrack = 2; // The currently selected Bass Track

	private static List<Conflict> setOfConflicts;


	protected static long microseconds = 0;
	private static int mekStringCursor = 0;
	private static boolean cleaned = false;


	/**
	 * A constructor for the Slave. It loads the default program configuration.
	 * */
	public Slave() throws IllegalArgumentException {
		parse(new File("default.cfg"));
	}

	/**
	 * Returns the current MIDI Sequence the program is using.
	 * @return The current MIDI Sequence the program is using.
	 * */
	public static Sequence getSequence(){
		return curMIDI;
	}

	/**
	 * Sets the current MIDI Sequence the program is using.
	 * @param m The MIDI Sequence the program should use now.
	 * */
	public static void setSequence(Sequence m){
		curMIDI = m;
	}

	/**
	 * Returns the Console.
	 * @return The Console.
	 * */
	public static Console getConsole() {
		return console;
	}

	/**
	 * Returns the UI.
	 * @return The UI.
	 * */
	public static UI getUI() {
		return UI;
	}

	/**
	 * Returns the length of note prepositions. This is used with
	 * James McVay's Mechbass.
	 * @return The length of note prepositions.
	 * */
	public static long getPrepositionLength(){
		return prepositionLength;
	}

	/**
	 * Sets the length of note prepositions. This is used with
	 * James McVay's Mechbass.
	 * @param l The note preposition length the program should use now.
	 * */
	public static void setPrepositionLength(long l){
		prepositionLength = l;
	}

	/**
	 * Returns the Delay of note prepositions. This is used with
	 * James McVay's Mechbass.
	 * @return The Delay of note prepositions.
	 * */
	public static long getPrepositionDelay(){
		return prepositionDelay;
	}

	/**
	 * Returns the delay of note prepositions. This is used with
	 * James McVay's Mechbass.
	 * @param l The note preposition delay the program should use now.
	 * */
	public static void setPrepositionDelay(long l){
		prepositionDelay = l;
	}

	/**
	 * Returns the number of MekStrings being used in the program now.
	 * @return The number of MekStrings being used by the program now.
	 * */
	public static int getNumberOfStrings(){
		return setOfStrings.length;
	}

	/**
	 * Sets the number of MekStrings to use in the program.
	 * @param i The number of MekStrings the program should use now.
	 * */
	public static void setNumberOfStrings(int i){
		mekStringCursor = 0;
		setOfStrings = new MekString[i];
	}

	/**
	 * Returns the array of MekStrings being used in the program now.
	 * @return The array MekStrings being used by the program now.
	 * */
	public static MekString[] getMekStringArray(){
		return setOfStrings;
	}

	/**
	 * Sets the name the MIDI File should be called now.
	 * @param n The name the MIDI File should be called now.
	 * */
	public static void setName(String n){
		name = n;
	}

	/**
	 * Sets the new UI the program should use.
	 * @param newUI The new UI the program should use.
	 * */
	public static void setUI(UI newUi) {
		UI = newUi;
	}

	/**
	 * Sets the simulation the program should use now.
	 * @param sim The simulation the program should use now.
	 * */
	static void setSim(Simulation sim){
		Slave.sim = sim;
	}

	/**
	 * Sets the simulation the program should use now.
	 * @param sim The simulation the program should use now.
	 * */
	static void setConsole(Console newConsole) {
		console = newConsole;
	}

	/**
	 * Signals the Player class to release the resources being used by the
	 * Sequencer.
	 * */
	public static void playerRelease() {
		Player.release();
	}

	/**
	 * Signals the Player class to stop playing the current MIDI sequence and
	 * set it to be ready to play from the beginning. IF it was playing in the
	 * first place.
	 * */
	public static void playerStop() {
		if(playing){
			Player.stop();
			microseconds = 0;
			playing = false;
			if (sim!=null) sim.stop();
		}
	}

	/**
	 * Signals the Player class to start playing the currently selected MIDI
	 * Sequence.
	 * */
	protected static void play() {
		if (curMIDI != null && !playing){
			Player.play(curMIDI,microseconds,cleaned);
			playing = true;
		}
		if (sim != null) {
			if(microseconds == 0){
				sim.stop();
			}
				sim.play();
		}
	}

	/**
	 * Signals the Player class to stop playing the currently selected MIDI
	 * Sequence, but preserve its position.
	 * */
	protected static void pause() {
		if(curMIDI != null && playing) {
			microseconds = Player.pause();
			playing = false;
		}
		if(sim != null){
			if(sim.isPlaying()){
				sim.pause();
			}
		}
	}

	/**
	 * Returns the currently selected bass track.
	 * @return The currently selected bass track.
	 * */
	public static int getBassTrack(){
		return bassTrack;
	}

	/**
	 * Sets the bass track the program should use now.
	 * @param i The bass track the program should use now.
	 * */
	public static void setBassTrack(int i){
		bassTrack = i;
	}

	/**
	 * Solves the currently selected MIDI File for use with Mechbass.
	 * Splits the track across four tracks, then assigns each note a string.
	 * @return A List of conflicts.
	 * */
	protected static List<Conflict> solve() {
		cleaned = false;
		if (curMIDI != null)
			try {
				Solver greedy = new OOGreedySolver(setOfStrings);
				curMIDI = TrackSplitter.split(curMIDI, setOfStrings.length, bassTrack);
				curMIDI = greedy.solve(curMIDI);
				setOfConflicts = Cleaner.getConflicts(curMIDI, setOfStrings);

				//serve users valid choices
				//receive users choice
				//call appropriate method

				// give the simulation the new midi
				curMIDI = Cleaner.prePos(curMIDI, prepositionDelay, setOfStrings, prepositionLength);
				if (sim!=null) sim.setSequence(curMIDI);
				return setOfConflicts;
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
		}
		return null;
	}

	/**
	 * Saves the MIDI File for use outside the program (such as with Mechbass).
	 * @param fileName The name the newly saved MIDI file should be saved as.
	 * */
	protected static void save(String fileName) {
		if (curMIDI != null) {
			try {
				MidiSystem.write(curMIDI, 1, new File(fileName));
			} catch (IOException e) {
				System.out.println("Could not save file");
			}
		}
	}

	/**
	 * Saves the MIDI File for use outside the program (such as with Mechbass).
	 * @param fileName The file the newly saved MIDI file should overwrite.
	 * */
	protected static List<Conflict> getConflicts(){
		return setOfConflicts;
	}

	protected static void save(File fileName) {
		if (curMIDI != null) {
			try {
				MidiSystem.write(curMIDI, 1, fileName);
			} catch (IOException e) {
				System.out.println("Could not save file");
			}
		}
	}

	/**
	 * Sets the current MIDI sequence.
	 *
	 * NOT to be confused with setSequence(Sequence m), this method gets the
	 * current MIDI sequence to be from the supplied file, returning the true
	 * upon success and false upon failure.
	 *
	 * @param path The filepath of the MIDI File we want the sequence from.
	 * @return True if the file was successfully opened and the sequence used as the current sequence - False otherwise.
	 * */
	protected static boolean setCurMIDI(String path) {
		try {
			File fi = new File(path);
			if (fi != null) {
				cleaned = false;
				curMIDI = MidiSystem.getSequence(fi);
				curMIDI = Cleaner.fixStupidity(curMIDI);
				if (sim!= null) sim.setSequence(curMIDI);
				System.out.print("successfully opened file \n");
				return true;
			}
		} catch (IOException e) {
			System.out.print("File was not found \n");
			return false;
		} catch (InvalidMidiDataException e) {
			System.out.print("Failed to create MIDI file \n");
			return false;
		}
		return false;
	}

	/**
	 * Signals the OctaveShifter class to shift all of the notes in the
	 * current MIDI sequence up.
	 * */
	protected static void octaveUp() {
		OctaveShifter.shiftOctave(curMIDI, 1);
	}

	/**
	 * Signals the OctaveShifter class to shift all of the notes in the
	 * current MIDI sequence down.
	 * */
	protected static void octaveDown() {
		OctaveShifter.shiftOctave(curMIDI, -1);
	}

	/**
	 * Like the octaveUp and OctaveDown methods above, but this method allows
	 * you to specify how much to shift the octave by.
	 * @param i How far to shift the Octave by (+'ve numbers for up, -'ve numbers for down).
	 * */
	protected static void shiftOctave(int i) {
		OctaveShifter.shiftOctave(curMIDI, i);
	}

	/**
	 * Sets the name to call the MIDI File, the length of the prepositions,
	 * the delay of the prepositions and the array of MekStrings used by the
	 * program.
	 * @param n The name the MIDI File should be called now.
	 * @param prepTime The note preposition delay the program should use now.
	 * @param prepSize The note preposition length the program should use now.
	 * @param strings The array of MekStrings the program should use now.
	 * */
	public static void setSettings(String n, long prepTime, long prepSize,MekString[] strings){
		name = n;
		setOfStrings = strings;
		prepositionLength = prepSize;
		prepositionDelay = prepTime;
	}

	/**
	 * Parses a Configuration File (a .csv file), and sets the Slave's data to
	 * the inside the file.
	 *
	 * @param fi The File to parse and load data from.
	 * @return True if the file is successfully loaded and the data set - False otherwise.
	 * */
	public static boolean parse(File fi){
		try {
			Scanner sc =  new Scanner(fi);

			sc.next();
			sc.next();
			name = sc.next();
			sc.next();
			sc.next();
			prepositionLength = sc.nextLong();
			sc.next();
			sc.next();
			prepositionDelay = sc.nextLong();
			sc.next();
			sc.next();
			setOfStrings = new MekString[sc.nextInt()];
			for(int i = 0; i < setOfStrings.length; i++){
				sc.next();
				sc.next();
				int low = sc.nextInt();
				sc.next();
				sc.next();
				int high =  sc.nextInt();
				long[] time = new long[(high - low)];
				sc.nextLine();
				String[] values = sc.nextLine().split(",");
				for(int j = 0; j < values.length-1; j++){
					time[j] = Long.parseLong(values[j].trim());
				}
				setOfStrings[i] = new MekString(low, high, time);
				mekStringCursor++;
			}
			sc.close();
		} catch (FileNotFoundException e) {
			return false;
		}

		return true;
	}

	/**
	 * Saves the current configuration as a file that can be reopened by this
	 * program.
	 * */
	public static void saveConfig(File fi){
		try {

			PrintStream ps = new PrintStream(fi);

			ps.println("Name = " + name);
			ps.println("PrepositionLength = " + prepositionLength);
			ps.println("PrepositionDelay = " + prepositionDelay);
			ps.println("Strings = " + setOfStrings.length);
			for(int i  = 0; i < setOfStrings.length; i++){
				ps.println("LowNote = " + setOfStrings[i].lowNote);
				ps.println("HighNote = " + setOfStrings[i].highNote);
				String output = "";
				for(int j = 0; j < setOfStrings[i].interval.length;j++){
					output += setOfStrings[i].interval[j] + ", ";
				}
				ps.println(output);
				ps.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Prints the current Configuration to System.out.
	 * */
	public static void getConfig(){
		System.out.println("Name = " + name);
		System.out.println("PrepositionLength = " + prepositionLength);
		System.out.println("PrepositionDelay = "  + prepositionDelay );
		System.out.println("Strings = " + setOfStrings.length);
		for(int i  = 0; i < setOfStrings.length; i++){
			System.out.println("LowNote = " + setOfStrings[i].lowNote);
			System.out.println("HighNote = " + setOfStrings[i].highNote);
			String output = "";
			for(int j = 0; j < setOfStrings[i].interval.length;j++){

				output += setOfStrings[i].interval[j] + ",";
			}
			System.out.println(output);
		}
	}

	/**
	 * Adds a MekString
	 * @param mekString The MekString to add.
	 * */
	public static void addToMekString(MekString mekString) {
		setOfStrings[mekStringCursor++] = mekString;

	}

	/**
	 * Returns a copy of an Array of MekStrings.
	 * @return A copy of an Array of MekStrings.
	 * */
	public static MekString[] getStringConfig(){
		if (setOfStrings != null) return Arrays.copyOf(setOfStrings, setOfStrings.length);
		else return null;
	}

	/**
	 * Signals the the Cleaner class to clean the currently selected MIDI
	 * Sequence.
	 * */
	public static void clean() {
		cleaned = true;
		Cleaner.clean(getSequence());
	}


}
