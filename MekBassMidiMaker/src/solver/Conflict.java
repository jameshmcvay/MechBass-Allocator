package solver;

import java.util.ArrayList;
import java.util.List;

public class Conflict {
	private int note;
	private List<NoteConflict> conflicts;
	
	public Conflict(int n){
		note = n; conflicts = new ArrayList<NoteConflict>();
	}
	
	public void addConf(NoteConflict con){
		conflicts.add(con);
	}
	
	

}
