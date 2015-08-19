package testing;

import static org.junit.Assert.*;
import static javax.sound.midi.Sequence.*;
import static javax.sound.midi.ShortMessage.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.junit.Test;

import solver.MekString;
import solver.Solver;

public class SolverTests {
	
	//This tests that two sequences can be equal as per testing.sequence
	@Test
	public void testSequenceEquals(){
		try {
			testing.Sequence seq = new testing.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			testing.Sequence compare = new testing.Sequence(PPQ,1,2);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			Solver.solve(seq, new MekString[]{});
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//this tests two different sequences are not equal
	@Test
	public void testSequenceNotEquals(){
		try {
			testing.Sequence seq = new testing.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			testing.Sequence compare = new testing.Sequence(PPQ,1,2);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,10000));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,100000));
			Solver.solve(seq, new MekString[]{});
			assertFalse(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//---------------------------------------------------------------
	//--------------- Tests for solver ------------------------------
	//---------------------------------------------------------------
	@Test
	public void solveNoStrings(){
		try {
			testing.Sequence seq = new testing.Sequence(PPQ,1,1);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			testing.Sequence compare = new testing.Sequence(PPQ,1,1);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			Solver.solve(seq, new MekString[]{});
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//tests that having invalid string notes will leave the seq as is
	@Test
	public void solveInvalidStringNotes(){
		try {
			testing.Sequence seq = new testing.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,500));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			testing.Sequence compare = new testing.Sequence(PPQ,1,2);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[0].add(CleanerTest.makeNote(2,500));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			Solver.solve(seq, new MekString[]{new MekString(-4,-3,new long[]{100})});
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//tests that a negative increment works
	@Test
	public void solveInvalidStringIncrement(){
		try {
			testing.Sequence seq = new testing.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,500));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			testing.Sequence compare = new testing.Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[1].add(CleanerTest.makeNote(2,500));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(2,600));
			Solver.solve(seq, new MekString[]{new MekString(1,2,new long[]{-400})});
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//very simple, one string, 4 events (2 on, 2 off), no conflicts
	@Test
	public void solveOneFourNone(){
		try {
			testing.Sequence seq = new testing.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,500));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			testing.Sequence compare = new testing.Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[1].add(CleanerTest.makeNote(2,500));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(2,600));
			Solver.solve(seq, new MekString[]{CleanerTest.buildString()});
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//one string, 4 events, 1 conflict
	@Test
	public void solveOneFourOne(){
		try {
			testing.Sequence seq = new testing.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,100));
			testing.Sequence compare = new testing.Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[0].add(CleanerTest.makeNote(2,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(2,100));
			Solver.solve(seq, new MekString[]{CleanerTest.buildString()});
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//two strings, 4 events, no conflicts
		@Test
		public void solveTwoFourNone(){
			try {
				testing.Sequence seq = new testing.Sequence(PPQ,1,3);
				seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
				seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
				seq.getTracks()[0].add(CleanerTest.makeNote(2,0));
				seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,100));
				testing.Sequence compare = new testing.Sequence(PPQ,1,3);
				compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
				compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
				compare.getTracks()[2].add(CleanerTest.makeNote(2,0));
				compare.getTracks()[2].add(CleanerTest.makeNoteOff(2,100));
				Solver.solve(seq, new MekString[]{CleanerTest.buildString(),CleanerTest.buildString()});
				assertTrue(seq.equals(compare));
			} catch (InvalidMidiDataException e) {
				fail("this shouldn't happen");
				e.printStackTrace();
			}
		}
		
		//two strings, 6 events, One conflict
				@Test
				public void solveTwoFourOne(){
					try {
						testing.Sequence seq = new testing.Sequence(PPQ,1,3);
						seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
						seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
						seq.getTracks()[0].add(CleanerTest.makeNote(2,0));
						seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,100));
						seq.getTracks()[0].add(CleanerTest.makeNote(3,0));
						seq.getTracks()[0].add(CleanerTest.makeNoteOff(3,100));
						testing.Sequence compare = new testing.Sequence(PPQ,1,3);
						compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
						compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
						compare.getTracks()[2].add(CleanerTest.makeNote(2,0));
						compare.getTracks()[2].add(CleanerTest.makeNoteOff(2,100));
						compare.getTracks()[0].add(CleanerTest.makeNote(3,0));
						compare.getTracks()[0].add(CleanerTest.makeNoteOff(3,100));
						Solver.solve(seq, new MekString[]{CleanerTest.buildString(),CleanerTest.buildString()});
						assertTrue(seq.equals(compare));
					} catch (InvalidMidiDataException e) {
						fail("this shouldn't happen");
						e.printStackTrace();
					}
				}
				
				

}
