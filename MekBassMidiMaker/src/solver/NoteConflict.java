package solver;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;

/**
 * 
 * @author Andrew
 *
 */
public class NoteConflict{
	private MidiEvent playedOn; //This should always be the earlier of the two
	private MidiEvent playedOff;
	private MidiEvent droppedOn; //this should be the later of the two
	private MidiEvent droppedOff;
	private Track workingTrack;
	private Track dropTrack;
	private int string;
	
	public NoteConflict(MidiEvent dOn, MidiEvent dOff, MidiEvent pOn, MidiEvent pOff, Track w , Track d , int str){
		droppedOn = dOn; droppedOff = dOff;
		playedOn = pOn; playedOff = pOff;
		workingTrack = w; dropTrack = d;
		string = str;
	}
	
	/**
	 * returns the time between the end of the first note and the start of the second.
	 * @return
	 */
	public long difference(int string){
		return droppedOn.getTick() - playedOff.getTick();
	}
	
	/**
	 * Returns the earliest tick in the conflict
	 * @return
	 */
	public long tick(){
		return playedOn.getTick();
	}
	
	/**
	 * @return the string the undropped note is on.
	 */
	public int string(){
		return string; 
	}
	
	/**
	 * This drops the first note of the conflict
	 */
	public void dropFirst(){
		
	}
	
	/**
	 * this drops the second note of the conflict
	 */
	public void dropSecond(){
		
	}
	
	/**
	 * this makes the first note shorter such that the second can be played
	 */
	public void delayFirstEnd(){
		
	}
	
	/**
	 * this delays the start of the second note such that it can be played
	 */
	public void delaySecondStart(){
		
	}
}
