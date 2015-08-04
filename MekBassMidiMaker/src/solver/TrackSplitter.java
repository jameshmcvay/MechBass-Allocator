package solver;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import main.Parser;

public class TrackSplitter {

	private final Sequence seq;
	private Sequence output;
	private final int trackCount;
	private final int bassTrack;

	public TrackSplitter(Sequence sequence, int trackCount, int bassTrack) throws InvalidMidiDataException {
		this.seq = sequence;
		this.trackCount = trackCount;
		this.bassTrack = bassTrack;
		this.output = new Sequence(seq.getDivisionType(), seq.getResolution(), trackCount+1);
	}

	public Sequence getNewSequence(){
		// TODO: this
		Track inTrack = seq.getTracks()[bassTrack];
		int pos = 0;
		do {
			output.getTracks()[0].add(inTrack.get(pos));
		} while ((++pos)<inTrack.size()); // this may be off by one, wait for indexoutofbounds
		// pass to solver

		return output;
	}

/*
	input: n tracks
	output: m tracks

*/
}
