package ui;

import helperCode.OctaveShifter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import solver.Cleaner;
import solver.Conflict;
import solver.MekString;
import solver.GreedySolver;
import solver.Solver;
import solver.TrackSplitter;
import tools.Player;

public class Slave {

	private static Sequence curMIDI;
	private UI ui;
	private Console console;
	private boolean guiMode;

	private static String name = "";
	private static long prepositionLength;
	private static long prepositionDelay;
	private static MekString[] setOfStrings;
	private static int bassTrack = 2;

	private static List<Conflict> setOfConflicts;

	public Slave() throws IllegalArgumentException {

	}

	public static Sequence getSequence(){
		return curMIDI;
	}

	public static void setSequence(Sequence m){
		curMIDI = m;
	}

	public Console getConsole() {
		return console;
	}

	public UI getUI() {
		return ui;
	}

	public static void setPrepositionLength(long l){
		prepositionLength = l;
	}

	public static void setPrepositionDelay(long l){
		prepositionDelay = l;
	}

	public static void setNumberOfStrings(int i){
		setOfStrings = new MekString[i];
	}

	public static void setName(String n){
		name = n;
	}

	void setUI(UI ui) {
		this.ui = ui;
	}

	void setConsole(Console console) {
		this.console = console;
	}

	public void playerRelease() {
		Player.release();
	}

	public void playerStop() {
		Player.stop();
	}

	protected void play() {
		if (curMIDI != null) Player.play(curMIDI);
	}

	public static void setBassTrack(int i){
		bassTrack = i;
	}

	protected List<Conflict> solve() {

		if (curMIDI != null)
			try {
				Solver greedy = new GreedySolver();
				curMIDI = TrackSplitter.split(curMIDI, 4, bassTrack);
				curMIDI = greedy.solve(curMIDI);
				setOfConflicts = Cleaner.getConflicts(curMIDI, setOfStrings);
				return setOfConflicts;
				//serve users valid choices
				//receive users choice
				//call appropriate method
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
				curMIDI = MidiSystem.getSequence(fi);
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
		OctaveShifter.shiftOctave(curMIDI, 3);
	}

	protected static void octaveDown() {
		OctaveShifter.shiftOctave(curMIDI, -3);
	}

	public static void main(String args[]) {

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

}
