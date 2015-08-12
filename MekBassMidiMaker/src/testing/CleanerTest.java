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


public class CleanerTest {

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
	
	

}
