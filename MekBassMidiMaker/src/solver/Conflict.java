package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiEvent;

/**
 * 
 * @author Andrew
 *
 */
public class Conflict {
	private MidiEvent note;
	private List<NoteConflict> conflicts;
	private boolean resolved;
	
	public Conflict(MidiEvent n){
		note = n; conflicts = new ArrayList<NoteConflict>(); resolved = false;
	}
	
	public void addConf(NoteConflict con){
		conflicts.add(con);
	}
	
	public boolean resolved(){
		return resolved;
	}
	
	public MidiEvent getNote(){
		return note;
	}
	
	public List<NoteConflict> getConf(){
		return conflicts;
	}
	
	public int strings(){
		return conflicts.size();
	}
	
	public long start(){
		long min = Long.MAX_VALUE;
		for(NoteConflict con : conflicts){
			if(con.tick() < min){
				min = con.tick();
			}
		}
		return min;
	}
	
	public long end(){
		long max = 0;
		for(NoteConflict con : conflicts){
			if(con.maxTick() > max){
				max = con.tick();
			}
		}
		return max;
	}
	
	public String toString(){
		return "Conflict containing a note playable on " + conflicts.size() +" strings\n";
	}
	
}
