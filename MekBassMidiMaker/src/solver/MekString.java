package solver;

/**
 * Tuple for string information<p>
 * Contains: <br>
 * Highest and Lowest notes that the string can play. <br>
 * The Range of Notes that the string can play. <br>
 * The time taken to travel between two adjacent notes.
 * @author Elliot Wilde, Andrew Palmer
 */
public class MekString {
	/**
	 * The Lowest note in MIDI notation that the string can play;
	 */
	public final int lowNote;
	/**
	 * The Highest note in MIDI notation that the string can play;
	 */
	public final int highNote;
	/**
	 * The Highest note minus the Lowest note in MIDI notation that the string can play;
	 */
	public final int noteRange;
	/**
	 * The Interval (in μs) between adjacent frets.<br>
	 * The time between the first and second frets would be in index 0
	 */
	public final int[] interval;

	public MekString(int low, int high, int[] time){
		this.lowNote = low;
		this.highNote = high;
		this.interval = time;
		this.noteRange = highNote - lowNote;
	}

	public MekString(int low, int high){
		this.lowNote = low;
		this.highNote = high;
		this.noteRange = highNote - lowNote;
		this.interval = new int[this.noteRange];
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