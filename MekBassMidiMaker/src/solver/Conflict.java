package solver;

import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.MidiEvent;

/**
 * 
 * @author Andrew
 *
 */
public class Conflict {
	private MidiEvent note;
	private Map<Integer,NoteConflict> conflicts;
	private boolean resolved;
	
	public Conflict(MidiEvent n){
		note = n; conflicts = new HashMap<Integer,NoteConflict>(); resolved = false;
	}
	
	public void addConf(NoteConflict con, int string){
		conflicts.put(string,con);
	}
	
	public boolean resolved(){
		return resolved;
	}
	
	public MidiEvent getNote(){
		return note;
	}
	
	public NoteConflict getConf(int str){
		return conflicts.get(str);
	}
	
	public int strings(){
		return conflicts.size();
	}
	
	public String toString(){
		return "Conflict containing a note playable on " + conflicts.size() +" strings\n";
	}
	
}
