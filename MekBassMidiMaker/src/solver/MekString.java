package solver;

/**
 * tuple for string information?<br>
 *		note top/bottom<br>
 *		time between frets<br>
 *		channel/track?
 */
public class MekString {
	public final int lowNote;
	public final int highNote;
	public final int[] interval; // made an array as they are different between different frets 

	public MekString(int low, int high, int[] time){
		this.lowNote = low;
		this.highNote = high;
		this.interval = time;
	}

	public MekString(int low, int high){
		this.lowNote = low;
		this.highNote = high;
		this.interval = new int[]{0};
	}
	
	/**
	 * returns the sum of all intervals between start and stop.
	 * @param start
	 * @param stop
	 * @return Sum of intervals
	 */
	public int addIntervals(int start, int stop){
		int ret = 0; 
		for(int i = start; i < stop; i++){ //not inclusive as start and stop represent notes and interval is between them
			ret += interval[i];
		}
		return ret;
	}
	
	/**
	 * Checks if two notes on the string are conflicting
	 * @param note1
	 * @param note2
	 * @return 
	 */
	public boolean conflicting(int note1,int note2,int duration){
		if(addIntervals(note1-lowNote,note2-lowNote)< duration) return true;
		return false;
	}
}