package solver.tests;

import static org.junit.Assert.*;
import static javax.sound.midi.Sequence.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.junit.Test;

import solver.MekString;
import solver.GreedySolver;
import solver.Solver;

public class SolverTests {

	//This tests that two sequences can be equal as per testing.sequence
	@Test
	public void testSequenceEquals(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,2);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			Solver solve = new GreedySolver(new MekString[]{});
			solve.solve(seq);
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
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,2);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,10000));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,100000));
			Solver solve = new GreedySolver(new MekString[]{});
			solve.solve(seq);
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
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,1);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,1);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			Solver solve = new GreedySolver(new MekString[]{});
			solve.solve(seq);
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
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,500));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,2);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[0].add(CleanerTest.makeNote(2,500));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			Solver solve = new GreedySolver(new MekString[]{new MekString(-4,-3,new long[]{100})});
			solve.solve(seq);
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
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,500));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[1].add(CleanerTest.makeNote(2,500));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(2,600));
			Solver solve = new GreedySolver(new MekString[]{new MekString(1,2,new long[]{-400})});
			solve.solve(seq);
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
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,500));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,600));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[1].add(CleanerTest.makeNote(2,500));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(2,600));
			Solver solve = new GreedySolver(new MekString[]{CleanerTest.buildString()});
			solve.solve(seq);
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
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,100));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[0].add(CleanerTest.makeNote(2,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(2,100));
			Solver solve = new GreedySolver(new MekString[]{CleanerTest.buildString()});
			solve.solve(seq);
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
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,3);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,100));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,3);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[2].add(CleanerTest.makeNote(2,0));
			compare.getTracks()[2].add(CleanerTest.makeNoteOff(2,100));
			Solver solve = new GreedySolver(new MekString[]{CleanerTest.buildString(),CleanerTest.buildString()});
			solve.solve(seq);
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}

	//two strings, 6 events, One conflict
	@Test
	public void solveTwoSixOne(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,3);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,100));
			seq.getTracks()[0].add(CleanerTest.makeNote(3,0));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(3,100));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,3);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,100));
			compare.getTracks()[2].add(CleanerTest.makeNote(2,0));
			compare.getTracks()[2].add(CleanerTest.makeNoteOff(2,100));
			compare.getTracks()[0].add(CleanerTest.makeNote(3,0));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(3,100));
			Solver solve = new GreedySolver(new MekString[]{CleanerTest.buildString(),CleanerTest.buildString()});
			solve.solve(seq);
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------
	//--------------- Tests for conflict resolver--------------------
	//---------------------------------------------------------------

	@Test
	public void testSolveTwoNotesConflictSolved(){
		try{
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ, 1, 3);
			seq.getTracks()[0].add(CleanerTest.makeNote(3, 50));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(3, 100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2, 70));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2, 120));
			Solver solve = new GreedySolver(new MekString[]{new MekString(1,3,new long[]{100,200,300}),
					new MekString(3,5,new long[]{100,200,300})});
			solve.solve(seq);

			assertEquals("There Should be only one event in the zeroth track", 1, seq.getTracks()[0].size());
			assertEquals("There Should be only two extra events in the first track", 3, seq.getTracks()[1].size());
			assertEquals("There Should be only two extra events in the second track", 3, seq.getTracks()[2].size());

		} catch (InvalidMidiDataException e){
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}

	@Test
	public void testSolveTwoNotesConflictUnsolved(){
		try{
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ, 1, 3);
			seq.getTracks()[0].add(CleanerTest.makeNote(2, 50));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2, 100));
			seq.getTracks()[0].add(CleanerTest.makeNote(2, 70));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2, 120));
			Solver solve = new GreedySolver(new MekString[]{new MekString(1,3,new long[]{100,200,300}),
											new MekString(3,5,new long[]{100,200,300})});
			solve.solve(seq);

			assertEquals("There Should be two unmoved events in the zeroth track", 3, seq.getTracks()[0].size());
			assertEquals("There Should be only two extra events in the first track", 3, seq.getTracks()[1].size());
			assertEquals("There Should be no extra events in the second track", 1, seq.getTracks()[2].size());

		} catch (InvalidMidiDataException e){
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}

	@Test
	public void testSolveThreeNotesConflictSolved(){
		try{
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ, 1, 5);
			// put a note in track one, to force the second note into track two
			seq.getTracks()[0].add(CleanerTest.makeNote(1, 10));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1, 11));
			seq.getTracks()[0].add(CleanerTest.makeNote(3, 50));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(3, 100));
			seq.getTracks()[0].add(CleanerTest.makeNote(7, 60));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(7, 110));

			// and now, the offending note
			seq.getTracks()[0].add(CleanerTest.makeNote(5, 70));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(5, 120));

			Solver solve = new GreedySolver(new MekString[]{new MekString(1,3,new long[]{100,200,300}),
											new MekString(3,5,new long[]{100,200,300}),
											new MekString(5,7,new long[]{100,200,300}),
											new MekString(7,9,new long[]{100,200,300})});
			solve.solve(seq);

			assertEquals("There Should be only one event in the zeroth track", 1, seq.getTracks()[0].size());
			assertEquals("There Should be only two extra events in the first track", 3, seq.getTracks()[1].size());
			assertEquals("There Should be only two extra events in the second track", 3, seq.getTracks()[2].size());
			assertEquals("There Should be only two extra events in the third track", 3, seq.getTracks()[3].size());
			assertEquals("There Should be only two extra events in the fourth track", 3, seq.getTracks()[4].size());
			assertEquals("The first event in the first track should have a note of 1", 1, ((ShortMessage)seq.getTracks()[1].get(0).getMessage()).getData1());
			assertEquals("The first event in the second track should have a note of 3", 3, ((ShortMessage)seq.getTracks()[2].get(0).getMessage()).getData1());
			assertEquals("The first event in the third track should have a note of 5", 5, ((ShortMessage)seq.getTracks()[3].get(0).getMessage()).getData1());
			assertEquals("The first event in the fourth track should have a note of 7", 7, ((ShortMessage)seq.getTracks()[4].get(0).getMessage()).getData1());

		} catch (InvalidMidiDataException e){
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}

	@Test
	public void testSolveThreeNotesConflictUnolved(){
		try{
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ, 1, 5);
			// put a note in track one, to force the second note into track two
			seq.getTracks()[0].add(CleanerTest.makeNote(1, 10));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1, 11));

			seq.getTracks()[0].add(CleanerTest.makeNote(4, 50));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(4, 100));
			seq.getTracks()[0].add(CleanerTest.makeNote(6, 60));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(6, 110));

			// and now, the offending note
			seq.getTracks()[0].add(CleanerTest.makeNote(5, 70));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(5, 120));

			Solver solve = new GreedySolver(new MekString[]{new MekString(1,3,new long[]{100,200,300}),
											new MekString(3,5,new long[]{100,200,300}),
											new MekString(5,7,new long[]{100,200,300}),
											new MekString(7,9,new long[]{100,200,300})});
			solve.solve(seq);

			assertEquals("There Should be only two extra events in the zeroth track", 3, seq.getTracks()[0].size());
			assertEquals("There Should be only two extra events in the first track", 3, seq.getTracks()[1].size());
			assertEquals("There Should be only two extra events in the second track", 3, seq.getTracks()[2].size());
			assertEquals("There Should be only two extra events in the third track", 3, seq.getTracks()[3].size());
			assertEquals("There Should be no extra events in the fourth track", 1, seq.getTracks()[4].size());
			assertEquals("The first event in the first track should have a note of 1", 1, ((ShortMessage)seq.getTracks()[1].get(0).getMessage()).getData1());
			assertEquals("The first event in the second track should have a note of 4", 4, ((ShortMessage)seq.getTracks()[2].get(0).getMessage()).getData1());
			assertEquals("The first event in the third track should have a note of 6", 6, ((ShortMessage)seq.getTracks()[3].get(0).getMessage()).getData1());

		} catch (InvalidMidiDataException e){
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}

}
