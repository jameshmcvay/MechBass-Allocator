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
	public final int interval;

	public MekString(int low, int high, int time){
		this.lowNote = low;
		this.highNote = high;
		this.interval = time;
	}

	public MekString(int low, int high){
		this.lowNote = low;
		this.highNote = high;
		this.interval = 0;
	}
}