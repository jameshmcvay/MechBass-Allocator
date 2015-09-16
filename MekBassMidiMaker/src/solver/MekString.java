package solver;

/**
 * Tuple for string information<p>
 * Contains: <br>
 * Highest and Lowest notes that the string can play. <br>
 * The Range of Notes that the string can play. <br>
 * The time taken to travel between two adjacent notes.
 * @author Elliot Wilde
 * @author Andrew Palmer
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
	public final long[] interval;

	/**
	 * Construct a MekString with a highest and lowest note and an array consisting of the time it takes
	 * for the string to move between adjacent frets.
	 * @param low The lowest note that the string can play
	 * @param high The highest note that the string can play
	 * @param time An array of times between frets
	 */
	public MekString(int low, int high, long[] time){
		this.lowNote = low;
		this.highNote = high;
		this.interval = time;
		this.noteRange = highNote - lowNote;
	}

	/**
	 * Construct a MekString with a highest and lowest note.<br>
	 * In this constructor the array of note times has been omitted, and <b>zero</b> is used for the timings instead (as in: instantaneous).
	 * @param low The lowest note that the string can play
	 * @param high The highest note that the string can play
	 */
	public MekString(int low, int high){
		this.lowNote = low;
		this.highNote = high;
		this.noteRange = highNote - lowNote;
		this.interval = new long[this.noteRange];
	}
	
	/**
	 * Initialises all note delays to i
	 */
	public void initTimings(long i){
		for(long j: interval){
			j = i;
		}
	}
	
	/**
	 * returns the sum of all intervals between start and stop.
	 * @param start
	 * @param stop
	 * @return Sum of intervals
	 */
	public long addIntervals(int start, int stop){
		long ret = 0;
		if(stop <= interval.length)
			for(int i = start; i < stop; i++){ //not inclusive as start and stop represent notes and interval is between them
				ret += interval[i];
			}
		return ret;
	}

	/**
	 * Checks if two notes on the string are conflicting
	 * @param note1
	 * @param note2
	 * @param duration - the time between note1 finishing and note 2 starting
	 * @return the
	 */
	public boolean conflicting(int note1,int note2, long duration){
		if(difference(note1,note2) > duration) return true;
		return false;
	}

	/**
	 * Checks the minimum time between two notes
	 * @param note1
	 * @param note2
	 * @return the min time between note1 and note2
	 */
	public long difference(int note1, int note2){
		if(note1<note2) return addIntervals(note1-lowNote,note2-lowNote);
		else return addIntervals(note2-lowNote, note1-lowNote);
	}
}