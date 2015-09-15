package solver;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * 
 * @author Andrew
 *
 */
public class Conflict{
	private MidiEvent playedOn; //This should always be the earlier of the two
	private MidiEvent playedOff;
	private MidiEvent droppedOn; //this should be the later of the two
	private MidiEvent droppedOff;
	private int string;
	
	public Conflict(MidiEvent dOn, MidiEvent dOff, MidiEvent pOn, MidiEvent pOff, int str){
		droppedOn = dOn; droppedOff = dOff;
		playedOn = pOn; playedOff = pOff;
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
}
