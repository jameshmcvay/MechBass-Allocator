package tools;

import javax.sound.midi.*;

public class Timing {

	public static long getMinTime(Sequence s) {
		long minTime = Long.MAX_VALUE;
		Track tracks[] = s.getTracks();
		for (Track t : tracks) {
			if (t.get(0).getTick() < minTime) {
				minTime = t.get(0).getTick();
			}
		}
		return minTime;
	}

	public static long getMaxTime(Sequence s) {
		long maxTime = Long.MIN_VALUE;
		Track tracks[] = s.getTracks();
		for (Track t : tracks) {
			if (t.get(t.size()-1).getTick() > maxTime) {
				maxTime = t.get(t.size()-1).getTick();
			}
		}
		return maxTime;
	}
}
