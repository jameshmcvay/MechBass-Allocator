package testing;

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


public class CleanerTest {
	
	public static MekString buildString(){
		MekString str = new MekString(1,4,new long[]{100,200,300});
		return str;
	}
	
	public static MidiEvent makeNote(int note, long time){
		ShortMessage shrt;
		try {
			shrt = new ShortMessage(NOTE_ON,0,note,100);
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
		assert(string.conflicting(1, 4, 300));
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
			assert(Cleaner.scanTimings(seq, strings)==0);
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
}
