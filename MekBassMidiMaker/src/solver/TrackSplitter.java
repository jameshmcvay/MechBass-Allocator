package solver;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import main.Parser;

/**
 *
 * @author Elliot Wilde and Andrew Palmer
 *
 */
public class TrackSplitter {

	/**
	 * Splits the sequence into the correct number of tracks and moves the bass track to the new sequence
	 * @param seq - The sequence for splitting
	 * @param trackCount - The number of tracks to split to
	 * @param bassTrack - The bass track to copy
	 * @return	The split sequence
	 * @throws InvalidMidiDataException
	 */
	public static Sequence split(Sequence seq, int trackCount, int bassTrack) throws InvalidMidiDataException {
		Sequence output = new solver.tests.Sequence(seq.getDivisionType(), seq.getResolution(), trackCount+1);
		Track inTrack = seq.getTracks()[bassTrack];
		int pos = 0;
		// add all of the events in the bass track to track zero in the output sequence
		do {
			output.getTracks()[0].add(inTrack.get(pos));
		} while ((++pos)<inTrack.size());

		// get some of the meta events from all the tracks in the input
		// and add them to the output track zero
		for (int j=0; j<seq.getTracks().length; ++j){
			// store the track
			Track tr = seq.getTracks()[j];
			// if it is the bass track, skip it as we have already copied everything from it
			if (j==bassTrack) continue;
			// go through all the events in the track
			for (int i=0; i<tr.size(); ++i){
				MidiMessage midmsg = tr.get(i).getMessage();
				if (midmsg instanceof MetaMessage){
					// and if they are of the approriate type
					// add them to the output track
					MetaMessage m = (MetaMessage) midmsg;
					// 0x51: tempo setting, 0x06: marker text
					if (m.getType() == 0x51 || m.getType() == 0x06){
						output.getTracks()[0].add(tr.get(i));
					}
				}
			}
		}


		// print some 'useful' information
		System.out.printf("Missed %d events? of %d\n", inTrack.size()-output.getTracks()[0].size(), inTrack.size() );
		// then return the new MIDI Sequence
		return output;
	}
/*
	input: n tracks
	output: m tracks

*/
}
