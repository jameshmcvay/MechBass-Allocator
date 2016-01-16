package solver;

import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;

/**
 * 
 * @author Andrew Palmer
 *
 */
public class NoteConflict{
	private List<MidiEvent> played1; //This should always be the earliest of the three
	private List<MidiEvent> played2; //This should always be the earliest of the three
	private List<MidiEvent> dropped; //this should be the later of the two
	private Track workingTrack;
	private Track dropTrack;
	private int string;
	
	/**
	 * Creates a NoteConflict.
	 * @param drop - The dropped note and related Midi Events
	 * @param play1 - The note that the dropped note is conflicting with and related events.
	 * @param play2 - the note and related events after play1 on the same string. May also conflict with drop.
	 * @param w - The track representing the string play1 and play2 are on
	 * @param d - The track representing dropped notes.
	 * @param str - The index of the string play1 an two are on.
	 */
	public NoteConflict(List<MidiEvent> drop,List<MidiEvent> play1,List<MidiEvent> play2, Track w , Track d , int str){
		dropped = drop;	played1 = play1; played2 = play2;
		workingTrack = w; dropTrack = d;
		string = str;
	}
	
	/**
	 * returns the time between the end of the first note and the start of the second.
	 * @return
	 */
	public long difference(int string){
		return dropped.get(0).getTick() - played1.get(played1.size()-1).getTick();
	}
	
	/**
	 * Returns the earliest tick in the conflict
	 * @return
	 */
	public long tick(){
		return played1.get(0).getTick();
	}
	
	/**
	 * Returns the latest tick in the conflict
	 * @return
	 */
	public long maxTick(){
		return played2.get(played2.size() - 1).getTick();
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
		//swap the tracks the notes are on
		drop1();
		play();
	}
	
	/**
	 * this drops the second note of the conflict
	 */
	public void dropLast(){
		drop2();
		play();
	}
	
	/**
	 * this makes the first note shorter such that the second can be played
	 */
	public void delayFirstEnd(long time){
		//move played off back
		long cur = played1.get(played1.size()-1).getTick();
		played1.get(played1.size()-1).setTick(cur - time);
		//add dropped note back in
		play();
	}
	
	/**
	 * this delays the start of the second note such that it can be played
	 */
	public void delaySecondStart(long time){
		//move dropped on forward
		long cur = dropped.get(0).getTick();
		dropped.get(0).setTick(cur + time);
		//add dropped note back in
		play();
	}
	
	/**
	 * Returns true if shortening the first note of the conflict could resolve it.
	 * @param time
	 * @return
	 */
	public boolean delayFirstPossible(long time){
		return(played1.get(0).getTick() < played1.get(played1.size()-1).getTick() - time);
	}
	

	/**
	 * Returns true if shortening the second note of the conflict could resolve it.
	 * @param time
	 * @return
	 */
	public boolean delaySecondPossible(long time){
		return(dropped.get(0).getTick() + time < dropped.get(dropped.size()-1).getTick());
	}
	
	/**
	 * Move all events relating to the first note in the conflict to track 0.
	 */
	private void drop1(){
		for(MidiEvent d: played1){
			dropTrack.add(d);
			workingTrack.remove(d);
		}
	}
	
	/**
	 * Move all events relating to the last note in the conflict to track 0.
	 */
	private void drop2(){
		for(MidiEvent d: played2){
			dropTrack.add(d);
			workingTrack.remove(d);
		}
	}
	
	/**
	 * Move the dropped note from track 0 to the track the conflicting notes are on.
	 */
	private void play(){
		for(MidiEvent d: dropped){
			workingTrack.add(d);
			dropTrack.remove(d);
		}
	}
	
	/**
	 * returns the track the notes invlved in this conflict that have note been dropped are on.
	 * @return
	 */
	public Track getTrack(){
		return workingTrack;
	}
	
	/**
	 * Swaps the two note groups supplied.
	 * @param other - The note to swap with the first note of this conflict.
	 * @param otherString
	 */
	public static void swap(List<MidiEvent> note1, Track string1, List<MidiEvent> note2, Track string2){
		for (MidiEvent o: note1){
			string2.add(o);
			string1.remove(o);
		}
		for (MidiEvent m: note2){
			string1.add(m);
			string2.remove(m);
		}
	}
	
	/**
	 * Prints which string this noteconflict is on.
	 */
	public String toString(){
		return "Conflict on string " + string + "\n"; 
	}
}
