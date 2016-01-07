package testing;

import static org.junit.Assert.*;
import helperCode.OctaveShifter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import main.Parser;
import main.Parser3;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OctaveShifterTestNew {

	private final File MIDIFILE = new File("./resources/Twinkle_Twinkle_in_octaves.mid");
	private final String MIDIFILESTRING = "./resources/Twinkle_Twinkle_in_octaves.mid";
	private Parser3 parser = new Parser3(MIDIFILESTRING);
	
	
	@Test
	public void testShiftOctaveUpOne() {
		Sequence seq = parser.getSequence();
		Sequence newSeq = OctaveShifter.shiftOctave(new Parser3(MIDIFILESTRING).getSequence(), 1);
		assertNotEquals("These sequences should NOT be equal", seq, newSeq);
		try {
			PrintWriter seqWriter = new PrintWriter("control.txt", "UTF-8");
			PrintWriter newWriter = new PrintWriter("O_SHIFT.txt", "UTF-8");
			Track[] tracks = seq.getTracks();
			
			// FOR EVERY TRACK...
			for (Track t : tracks){
				// ... Print out some basic information (Track Number and the Size of the track)
				
				for (int i=0; i < t.size(); i++) {
					// GO THROUGH ALL THE EVENTS...
					MidiEvent event = t.get(i);
					// ... And get the messages at each one.
					MidiMessage message = event.getMessage();

					if (message instanceof ShortMessage) {
						ShortMessage sm = (ShortMessage) message;
						String smString = String.format("%02x", Integer.parseInt(((Integer) sm.getStatus()).toString()));
						parseShortMessage(sm, event, seqWriter);
					}
				}
			}
		
			Track[] newTracks = newSeq.getTracks();
			
			// FOR EVERY TRACK...
			for (Track t : newTracks){
				// ... Print out some basic information (Track Number and the Size of the track)
				
				for (int i=0; i < t.size(); i++) {
					// GO THROUGH ALL THE EVENTS...
					MidiEvent event = t.get(i);
					// ... And get the messages at each one.
					MidiMessage message = event.getMessage();

					if (message instanceof ShortMessage) {
						ShortMessage sm = (ShortMessage) message;
						String smString = String.format("%02x", Integer.parseInt(((Integer) sm.getStatus()).toString()));
						parseShortMessage(sm, event, newWriter);
					}
				}
			}
			seqWriter.close();
			newWriter.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File seqFile = new File("control.txt");
		File newFile = new File("O_SHIFT.txt");
		try {
			Scanner seqScan = new Scanner(seqFile);
			Scanner newScan = new Scanner(newFile);
			while (seqScan.hasNext() && newScan.hasNext()){
				int num1 = seqScan.nextInt();
				int num2 = newScan.nextInt();
				assertTrue("Something has gone wrong with Octave Shifter.", 
						num1 == num2-12);
			}
			if ((seqScan.hasNext() && !newScan.hasNext()) || (!seqScan.hasNext() && newScan.hasNext())){
				fail("The size of the files is unequal for some reason...");
			}
			seqScan.close();
			newScan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * author: Dean Newberry
	 *
	 * parseShortMessage parses a ShortMessage.
	 * This method has been ripped and adapted from the Parser Class.
	 *
	 * REQUIRES: a MidiEvent and the corresponding MidiMessage that is compatible with ShortMessage methods 
	 * 		(i.e. message must be an instance of ShortMessage) and the writer to write to write data to.
	 * ENSURES: A file with data will be written and saved (data = key of ShortMessages when command
	 * is NOTE_ON or NOTE_OFF.
	 * */
	public void parseShortMessage(MidiMessage message, MidiEvent event, PrintWriter writer){
		if (!(message instanceof ShortMessage) || !message.equals(event.getMessage())){
			throw new IllegalArgumentException("An error occured - Please check:\n"
					+ "That the MidiMessage that was passed in is an instance of ShortMessage,\n"
					+ "That the MidiMessage came from the supplied event");
		}
		else {
			ShortMessage sm = (ShortMessage) message;
			String smString = String.format("%02x", Integer.parseInt(((Integer) sm.getStatus()).toString()));
			if (sm.getCommand() == Parser.NOTE_ON || sm.getCommand() == Parser.NOTE_OFF) {
				writer.write(sm.getData1() + System.lineSeparator());
			}
		}
	}
	
}
