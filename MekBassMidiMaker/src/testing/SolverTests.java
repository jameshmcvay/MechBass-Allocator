package testing;

import static org.junit.Assert.*;
import static javax.sound.midi.Sequence.*;
import static javax.sound.midi.ShortMessage.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import org.junit.Test;

import solver.MekString;
import solver.Solver;

public class SolverTests {

	//---------------------------------------------------------------
	//--------------- Tests for solver ------------------------------
	//---------------------------------------------------------------
	@Test
	public void solve(){
		try {
			Sequence seq = new Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,500));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			Sequence compare = new Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[1].add(CleanerTest.makeNote(2,500));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(2,600));
			Solver.solve(seq, new MekString[]{CleanerTest.buildString()});
			assert(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}

}
