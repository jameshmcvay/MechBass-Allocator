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
import solver.GraphSolver;
import solver.MekString;
import solver.GreedySolver;
import solver.Solver;
import solver.TrackSplitter;
import tools.Player;

public class Slave {

	private static Sequence curMIDI;
	private static UI UI;
	private static Console console;
	private boolean guiMode;

	private static boolean playing = false;

	private static Simulation sim;
	private static String name = "";
	private static long prepositionLength;
	private static long prepositionDelay;
	private static MekString[] setOfStrings;
	private static int bassTrack = 2;

	private static List<Conflict> setOfConflicts;

	private static boolean cleaned = false;

	public Slave() throws IllegalArgumentException {
		parse(new File("default.csv"));
	}

	public static Sequence getSequence(){
		return curMIDI;
	}

	public static void setSequence(Sequence m){
		curMIDI = m;
	}

	public static Console getConsole() {
		return console;
	}

	public static UI getUI() {
		return UI;
	}

	public static void setPrepositionLength(long l){
		prepositionLength = l;
	}

	public static void setPrepositionDelay(long l){
		prepositionDelay = l;
	}

	public static void setNumberOfStrings(int i){
		mekStringCursor = 0;
		setOfStrings = new MekString[i];
	}

	public static int getNumberOfStrings(){
		return setOfStrings.length;
	}

	public static MekString[] getMekStringArray(){
		return setOfStrings;
	}

	public static void setName(String n){
		name = n;
	}

	public static void setUI(UI newUi) {
		UI = newUi;
	}

	static void setSim(Simulation sim){
		Slave.sim = sim;
	}

	static void setConsole(Console newConsole) {
		console = newConsole;
	}

	public static void playerRelease() {
		Player.release();
	}

	public static void playerStop() {
		if(playing){
			Player.stop();
			microseconds = 0;
			playing = false;
			if (sim!=null) sim.stop();
		}
	}

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

	protected static long microseconds = 0;

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

	public static void setBassTrack(int i){
		bassTrack = i;
	}

	public static int getBassTrack(){
		return bassTrack;
	}

	protected static List<Conflict> solve() {
		cleaned = false;
		if (curMIDI != null)
			try {
				Solver greedy = new GreedySolver(setOfStrings);
				Solver graph = new GraphSolver(setOfStrings);
				curMIDI = TrackSplitter.split(curMIDI, setOfStrings.length, bassTrack);
				curMIDI = greedy.solve(curMIDI);
//				curMIDI = graph.solve(curMIDI);
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

	protected static void save(String fileName) {
		if (curMIDI != null) {
			try {
				MidiSystem.write(curMIDI, 1, new File(fileName));
			} catch (IOException e) {
				System.out.println("Could not save file");
			}
		}
	}

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

	protected static boolean setCurMIDI(String path) {
		try {
			File fi = new File(path);
			if (fi != null) {
				cleaned = false;
				curMIDI = MidiSystem.getSequence(fi);
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

	protected static void octaveUp() {
		OctaveShifter.shiftOctave(curMIDI, 1);
	}

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

	public static void setSettings(String n, long prepTime, long prepSize,MekString[] strings){
		name = n;
		setOfStrings = strings;
		prepositionLength = prepSize;
		prepositionDelay = prepTime;
	}


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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		}

		return true;
	}

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
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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

	private static int mekStringCursor = 0;

	public static void addToMekString(MekString mekString) {
		setOfStrings[mekStringCursor++] = mekString;

	}

	public static MekString[] getStringConfig(){
		if (setOfStrings != null) return Arrays.copyOf(setOfStrings, setOfStrings.length);
		else return null;
	}

	public static void clean() {
		cleaned = true;
		Cleaner.clean(getSequence());
	}


}
