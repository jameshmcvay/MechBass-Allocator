package solver.tests;

import static org.junit.Assert.*;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.Sequence.*;
import static javax.sound.midi.ShortMessage.*;

import org.junit.Test;

import solver.Cleaner;
import solver.Conflict;
import solver.MekString;
import solver.GreedySolver;


public class CleanerTest {
	
	/**
	 * Helper method for string building
	 * @return a mekstring
	 */
	public static MekString buildString(){
		MekString str = new MekString(1,4,new long[]{50000000,100000000,150000000});
		return str;
	}
	
	/**
	 * helper method for adding a note
	 * @param note
	 * @param time
	 * @return a midievent representing a note at a time
	 */
	public static MidiEvent makeNote(int note, long time){
		return makeNote(note, time, 100);
	}
	
	public static MidiEvent makeNote(int note, long time, int velocity){
		return makeNote(note,time,velocity,0);
	}
	
	public static MidiEvent makeNote(int note, long time, int velocity, int channel){
		ShortMessage shrt;
		try {
			shrt = new ShortMessage(NOTE_ON,channel,note,velocity);
			return new MidiEvent(shrt, time);
		} 
		catch (InvalidMidiDataException e) {			
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * helper method for making a note off
	 * @param note
	 * @param time
	 * @return a note off event for note at time
	 */
	public static MidiEvent makeNoteOff(int note, long time, int channel){
		ShortMessage shrt;
		try {
			shrt = new ShortMessage(NOTE_OFF,channel,note,0);
			return new MidiEvent(shrt, time);
		} 
		catch (InvalidMidiDataException e) {			
			e.printStackTrace();
		}
		return null;
	}
	
	public static MidiEvent makeNoteOff(int note, long time){
		return makeNoteOff(note, time, 0);
	}
	
	
	//-------------------------------------------------------------------------------
	//---------Tests for Mekstring --------------------------------------------------
	//-------------------------------------------------------------------------------
	
	@Test
	public void addIntervalsTest(){
		MekString str = new MekString(1,2,new long[]{100});
		assertTrue(str.addIntervals(0,1) == 100);
	}
	
	@Test
	public void addIntervalsTestMulti(){
		MekString str = new MekString(1,3,new long[]{100,100});
		assertTrue(str.addIntervals(0,2) == 200);
	}
	
	@Test
	public void differenceTest(){
		MekString str = new MekString(1,2,new long[]{100});
		assertTrue(str.differenceTime(1,2) == 100);
	}
	
	@Test
	public void differenceTestMulti(){
		MekString str = new MekString(1,3,new long[]{100,200});
		assertTrue(str.differenceTime(1,3) == 300);
	}
	
	@Test
	public void playableFalse(){
		MekString str = new MekString(1,2,new long[]{100});
		assertTrue(str.playable(4) == false);
	}
	
	@Test
	public void playableTrue(){
		MekString str = new MekString(1,2,new long[]{100});
		assertTrue(str.playable(1) == true);
	}
	
	
	//-------------------------------------------------------------------------------
	//---------Tests for Cleaner.clean-----------------------------------------------
	//-------------------------------------------------------------------------------
			
	//passes as long as the sequence has the correct number of tracks after cleaning
	@Test
	public void cleanTestPass(){
		try {
			Sequence seq = new Sequence(PPQ,1,2);
			Cleaner.clean(seq);
			assertTrue(seq.getTracks().length == 1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------------
	//-------------Tests for removal of velocity 0 note ons--------------------------
	//-------------------------------------------------------------------------------
	@Test
	public void deStupifyPass(){
		try {
			Sequence seq = new Sequence(PPQ,1,1);
			Track tr = seq.getTracks()[0];
			tr.add(new MidiEvent(new ShortMessage(NOTE_ON,0,1,0), 100));
			Cleaner.fixStupidity(seq);
			ShortMessage sm = (ShortMessage) tr.get(0).getMessage();
			assertTrue(sm.getCommand()==NOTE_OFF);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------------
	//-------------Tests for conflict scanning---------------------------------------
	//-------------------------------------------------------------------------------
	
	//Tests that conflicting returns true if notes are conflicting - 1 interval
	@Test
	public void conflictingTrue(){
		MekString string = buildString();
		assertTrue(string.conflicting(1, 2, 50, 1));
	}
	
	//Multi interval
	@Test
	public void conflictingTrueMulti(){
		MekString string = buildString();
		assertTrue(string.conflicting(1, 4, 300, 1));
	}
	
	//Tests that conflicting returns false if notes are not conflicting 
	@Test
	public void conflictingFalse(){
		MekString string = buildString();
		assertFalse(string.conflicting(1, 2, 150000000, 1));
	}
	
	//Multi interval
	@Test 
	public void conflictingFalseMulti(){
		MekString string = buildString();
		assertFalse(string.conflicting(1, 4, 500000000, 1));
	} 
	
	//tests scantimings returns 0 for non conflicting sequence
	@Test
	public void noConflicts(){
		try {
			Sequence seq = new Sequence(PPQ,1,1);
			seq.getTracks()[0].add(makeNote(1,0));
			seq.getTracks()[0].add(makeNote(2,500));
			MekString[] strings = new MekString[]{buildString()};
			assertTrue(Cleaner.scanTimings(seq, strings)==0);
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		} 
	}
	
	//should be 1 conflict
	@Test
	public void oneConflict(){
		try {
			Sequence seq = new Sequence(PPQ,1,1);
			seq.getTracks()[0].add(makeNote(1,0));
			seq.getTracks()[0].add(makeNote(2,50));
			seq.getTracks()[0].add(makeNoteOff(1,25));
			seq.getTracks()[0].add(makeNoteOff(2,75));
			MekString[] strings = new MekString[]{buildString()};
			System.out.printf("found %d conflicts", Cleaner.scanTimings(seq, strings));
			assertTrue(Cleaner.scanTimings(seq, strings)==1);
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		} 
	}
	
	//---------------------------------------------------------------
	//-------------Tests for channel fixing--------------------------
	//---------------------------------------------------------------
	
	@Test
	public void twoChan(){
		try {
			Sequence seq = new solver.tests.Sequence(PPQ,1,1);
			seq.getTracks()[0].add(makeNote(1,0,100,1));
			seq.getTracks()[0].add(makeNote(2,50,100,1));
			seq = Cleaner.fixChannel(seq);
			Sequence two = new Sequence(PPQ,1,1);
			seq.getTracks()[0].add(makeNote(1,0,100,0));
			seq.getTracks()[0].add(makeNote(2,50,100,0));
			assertTrue(seq.equals(two));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		} 
	}
	
	
	//---------------------------------------------------------------
	//-------------Tests for pre positioning-------------------------
	//---------------------------------------------------------------
	
	@Test
	public void preposOneStringNoConOneNote(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[1].add(CleanerTest.makeNote(1,100));
			seq.getTracks()[1].add(CleanerTest.makeNoteOff(1,200));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,100,100,1));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,200,1));
			compare.getTracks()[1].add(CleanerTest.makeNote(1, 0, 1, 1));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1, 14, 1));
			Cleaner.prePos(seq,100,new MekString[]{new MekString(1,2,new long[]{0})},14);
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//This test wont pass, as it was built using tick numbers for prepositioning before
	//We moved to ms like we should have been. the expected output has not been updated.
	@Test
	public void preposOneStringNoConTwoNotes(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[1].add(CleanerTest.makeNote(1,100));
			seq.getTracks()[1].add(CleanerTest.makeNoteOff(1,200));
			seq.getTracks()[1].add(CleanerTest.makeNote(2,420));
			seq.getTracks()[1].add(CleanerTest.makeNoteOff(2,520));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,2);
			compare.getTracks()[1].add(CleanerTest.makeNote(1,100,100,1));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,200,1));
			compare.getTracks()[1].add(CleanerTest.makeNote(2,420,100,1));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(2,520,1));
			compare.getTracks()[1].add(CleanerTest.makeNote(1,0,1,1));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(1,14,1));
			compare.getTracks()[1].add(CleanerTest.makeNote(2,220,1,1));
			compare.getTracks()[1].add(CleanerTest.makeNoteOff(2,234,1));
			long f = (long) ((float)seq.getMicrosecondLength()/(float)seq.getTickLength());
			Cleaner.prePos(seq,100,new MekString[]{new MekString(1,2,new long[]{1000})},14);
			//System.out.printf("Track Length:\n uS = %d \n ticks: %d \n uS/ticks: %d\n",seq.getMicrosecondLength(),seq.getTickLength(),f);
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	//--------------------------------------------------------------------------------------
	//---------------------Tests for conflict list building --------------------------------
	//-------------------------------------------------------------------------------------
	
	@Test
	public void conflictBuildNoEvents(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[1].add(CleanerTest.makeNote(1,100));
			seq.getTracks()[1].add(CleanerTest.makeNoteOff(1,200));
			assertTrue(Cleaner.getConflicts(seq, new MekString[]{new MekString(1,2,new long[]{100})}).isEmpty());
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	@Test
	public void conflictBuildOneEventMakesObject(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,2);
			seq.getTracks()[1].add(CleanerTest.makeNote(1,100));
			seq.getTracks()[1].add(CleanerTest.makeNoteOff(1,200));
			seq.getTracks()[0].add(CleanerTest.makeNote(1,150));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,250));
			List<Conflict> con = Cleaner.getConflicts(seq, new MekString[]{new MekString(1,2,new long[]{50000000})});
			System.out.printf(con.get(0).toString());
			System.out.printf(con.get(0).getConf().get(0).toString());
			assertTrue(Cleaner.getConflicts(seq, new MekString[]{new MekString(1,2,new long[]{100})}).size() == 1);
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	@Test
	public void conflictBuildOneEventTwoStrings(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,3);
			seq.getTracks()[1].add(CleanerTest.makeNote(1,100));
			seq.getTracks()[1].add(CleanerTest.makeNoteOff(1,200));
			seq.getTracks()[2].add(CleanerTest.makeNote(1,100));
			seq.getTracks()[2].add(CleanerTest.makeNoteOff(1,200));
			seq.getTracks()[0].add(CleanerTest.makeNote(1,150));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,250));
			//System.out.println(Cleaner.getConflicts(seq, new MekString[]{new MekString(1,2,new long[]{100})}).size());
			assertTrue(Cleaner.getConflicts(seq, new MekString[]{new MekString(1,2,new long[]{100}),new MekString(1,2,new long[]{100})}).get(0).strings() == 2);
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
}
