package ui;

import helperCode.OctaveShifter;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import solver.SplitAndSolve;
import solver.TrackSplitter;
import tools.Player;

public class Slave {

	static Sequence curMIDI;
	private UI ui;
	private Console console;
	private boolean guiMode;

	public Slave() throws IllegalArgumentException{

	}

	public Console getConsole(){
		return console;
	}

	public UI getUI(){
		return ui;
	}

	void setUI(UI ui){
		this.ui = ui;
	}

	void setConsole(Console console){
		this.console = console;
	}

	public void playerRelease(){
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

	protected static boolean setCurMIDI(String path) {
		try {
			File fi = new File(path);
			if (fi != null){
				curMIDI = MidiSystem.getSequence(fi);
				return true;
			}
		} catch (IOException e) {
			return false;
		}
		catch (InvalidMidiDataException e) {
			return false;
		}
		return false;
	}

	protected static void octaveUp(){
		OctaveShifter.shiftOctave(curMIDI, 3);
	}

	protected void save(File fi) throws IOException{
		MidiSystem.write(curMIDI, 1, fi);
	}

}
