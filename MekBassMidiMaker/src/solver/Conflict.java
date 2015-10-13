package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiEvent;

/**
 * 
 * @author Andrew Palmer
 *
 */
public class Conflict {
	private MidiEvent note;
	private List<NoteConflict> conflicts;
	private boolean resolved;
	
	/**
	 * Creates a new conflict object relating to dropped note n.
	 * @param n - The MidiEvent representing the dropped note.
	 */
	public Conflict(MidiEvent n){
		note = n; conflicts = new ArrayList<NoteConflict>(); resolved = false;
	}
	
	/**
	 * Adds a noteConflict to the conflict list.
	 * @param con
	 */
	public void addConf(NoteConflict con){
		conflicts.add(con);
	}
	
	/**
	 * Marks a conflict as resolved.
	 * @return
	 */
	public boolean resolved(){
		return resolved;
	}
	
	/**
	 * Returns the dropped note of the conflict.
	 * @return MidiEvent - Dropped note
	 */
	public MidiEvent getNote(){
		return note;
	}
	
	/**
	 * Returns the list of NoteConflicts
	 * @return
	 */
	public List<NoteConflict> getConf(){
		return conflicts;
	}
	
	/**
	 * Returns the number of different strings the dropped note could be played on,
	 * if they didn't have conflicting notes on them.
	 * @return
	 */
	public int strings(){
		return conflicts.size();
	}
	
	/**
	 * Returns the tick of the first note in any of the NoteConflicts contained in
	 * this conflict.
	 * 
	 * Use this to determine start time for playback.
	 * 
	 * @return long - Tick
	 */
	public long start(){
		long min = Long.MAX_VALUE;
		for(NoteConflict con : conflicts){
			if(con.tick() < min){
				min = con.tick();
			}
		}
		return min;
	}
	
	/**
	 * Returns the tick of the last note of any of the NoteConflicts contained in this conflict.
	 * 
	 * Use this to determine end time for playback.
	 * 
	 * @return long - Tick
	 */
	public long end(){
		long max = 0;
		for(NoteConflict con : conflicts){
			if(con.maxTick() > max){
				max = con.tick();
			}
		}
		return max;
	}
	
	/**
	 * Returns a string informing the user of the number of strings the dropped note could be played on.
	 */
	public String toString(){
		return "Conflict containing a note playable on " + conflicts.size() +" strings\n";
	}
	
}
