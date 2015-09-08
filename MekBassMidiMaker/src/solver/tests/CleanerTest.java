package solver.tests;

import static org.junit.Assert.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.Sequence.*;
import static javax.sound.midi.ShortMessage.*;

import org.junit.Test;

import solver.Cleaner;
import solver.MekString;
import solver.Solver;


public class CleanerTest {
	
	/**
	 * Helper method for string building
	 * @return a mekstring
	 */
	public static MekString buildString(){
		MekString str = new MekString(1,4,new long[]{100,200,300});
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
		ShortMessage shrt;
		try {
			shrt = new ShortMessage(NOTE_ON,0,note,velocity);
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
	public static MidiEvent makeNoteOff(int note, long time){
		ShortMessage shrt;
		try {
			shrt = new ShortMessage(NOTE_OFF,0,note,100);
			return new MidiEvent(shrt, time);
		} 
		catch (InvalidMidiDataException e) {			
			e.printStackTrace();
		}
		return null;
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
			assert(seq.getTracks().length == 1);
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
			assert(sm.getCommand()==NOTE_OFF);
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
		assert(string.conflicting(1, 2, 50));
	}
	
	//Multi interval
	@Test
	public void conflictingTrueMulti(){
		MekString string = buildString();
		assertTrue(string.conflicting(1, 4, 300));
	}
	
	//Tests that conflicting returns false if notes are not conflicting 
	@Test
	public void conflictingFalse(){
		MekString string = buildString();
		System.out.println(string.addIntervals(0,1));
		assertFalse(string.conflicting(1, 2, 300));
	}
	
	//Multi interval
	@Test 
	public void conflictingFalseMulti(){
		MekString string = buildString();
		assertFalse(string.conflicting(1, 4, 1000));
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
			assert(false);
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
			MekString[] strings = new MekString[]{buildString()};
			assert(Cleaner.scanTimings(seq, strings)==1);
		} catch (InvalidMidiDataException e) {
			assert(false);
			e.printStackTrace();
		} 
	}
	
	//---------------------------------------------------------------
	//-------------Tests for pre positioning-------------------------
	//---------------------------------------------------------------
	
	@Test
	public void preposOneStringNoConOneNote(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,1);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,200));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,1);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,100));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,200));
			compare.getTracks()[0].add(CleanerTest.makeNote(1, 0, 1));
			Cleaner.prePos(seq,100,new MekString[]{new MekString(1,2,new long[]{0})});
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	@Test
	public void preposOneStringNoConTwoNotes(){
		try {
			solver.tests.Sequence seq = new solver.tests.Sequence(PPQ,1,1);
			seq.getTracks()[0].add(CleanerTest.makeNote(1,100));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(1,200));
			seq.getTracks()[0].add(CleanerTest.makeNote(2,400));
			seq.getTracks()[0].add(CleanerTest.makeNoteOff(2,500));
			solver.tests.Sequence compare = new solver.tests.Sequence(PPQ,1,1);
			compare.getTracks()[0].add(CleanerTest.makeNote(1,100));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(1,200));
			compare.getTracks()[0].add(CleanerTest.makeNote(2,400));
			compare.getTracks()[0].add(CleanerTest.makeNoteOff(2,500));
			compare.getTracks()[0].add(CleanerTest.makeNote(1,0,1));
			compare.getTracks()[0].add(CleanerTest.makeNote(2, 300,1));
			Cleaner.prePos(seq,100,new MekString[]{new MekString(1,2,new long[]{100})});
			assertTrue(seq.equals(compare));
		} catch (InvalidMidiDataException e) {
			fail("this shouldn't happen");
			e.printStackTrace();
		}
	}
	
	
}
