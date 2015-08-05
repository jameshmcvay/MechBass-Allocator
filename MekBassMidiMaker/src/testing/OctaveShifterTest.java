package testing;

import static org.junit.Assert.*;
import helperCode.OctaveShifter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import main.Parser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OctaveShifterTest {

	private final File MIDIFILE = new File("./resources/Twinkle_Twinkle_in_octaves.mid");
	private Sequencer sequencer;
	private Sequence sequence;
	
	@Before
	public void setUp() throws Exception {
		sequencer = MidiSystem.getSequencer();
		// Set the sequence to be examined to the midiFile...
		sequencer.setSequence(MidiSystem.getSequence(MIDIFILE));
		// ... And store the sequence.
		sequence = sequencer.getSequence();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testShiftOctaveUpOne() {
		try {
			setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Sequence newSeq = OctaveShifter.shiftOctave(sequence, 1);
		try {
			PrintWriter writer = new PrintWriter("TTIO_Output.txt", "UTF-8");
			Track[] tracks = sequence.getTracks();
			int trackNo = tracks.length;
			writer.write("Number of Tracks = " + trackNo + Character.LINE_SEPARATOR);

			// Set a counting variable
			int trackNumber = 0;

			// FOR EVERY TRACK...
			for (Track t : tracks){
				// ... Print out some basic information (Track Number and the Size of the track)
				writer.write("Track " + trackNumber++ + ": size = " + t.size() + "\n");

				String instrumentName = "Undefined";
				String track_SequenceName = "Undefined";
				int channels = 0;
				List<String> instrumentChanges = new ArrayList<>();

				for (int i=0; i < t.size(); i++) {
					// GO THROUGH ALL THE EVENTS...
					MidiEvent event = t.get(i);
					// ... And get the messages at each one.
					MidiMessage message = event.getMessage();

					if (message instanceof MetaMessage) {
						ArrayList<Byte> byteList = parseMetaMessage(message, event, writer);
						
						MetaMessage mm = (MetaMessage) message;
						String mmString = String.format("%02x", Integer.parseInt(((Integer) mm.getType()).toString()));
						if (mmString.equals("03"))
							track_SequenceName = new String(mm.getData(), "UTF-8");
						if (mmString.equals("04")){
							instrumentName = new String(mm.getData(), "UTF-8");
							instrumentChanges.add(instrumentName);
						}

					}
					else if (message instanceof ShortMessage) {
						ArrayList<Byte> byteList = parseShortMessage(message, event, writer);

						ShortMessage sm = (ShortMessage) message;
						String smString = String.format("%02x", Integer.parseInt(((Integer) sm.getStatus()).toString()));

						if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE){
							instrumentChanges.add(Parser.VOICES[sm.getData1()] + " (" + sm.getData1() + ")");
						}
					}
				}
				writer.write("Number of Channels: " + channels + ",\n" +
					"Track Name: " + track_SequenceName + ",\n" + 
					"STARTING Instrument Name: " + instrumentName + ",\n" + 
					"All Instruments used: ");
				for (String s : instrumentChanges)
					writer.write("\t" + s);
				writer.write("\n");
			}
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotEquals(sequence, newSeq);
	}

	/**
	 * author: Dean Newberry
	 *
	 * parseMetaMessage parses a MetaMessage, prints important information, and returns
	 * an ArrayList of ALL the bytes in the MetaMessage (INCLUDING bytes from unprinted information).
	 * THIS SHOULD PERFECTLY RETURN THE HEADER CHUNK OF A MIDI FILE.
	 *
	 * @param message - a MidiMessage.
	 * @param event - the MidiEvent the MidiMessage came from. Mainly used for the "tick" data printout.
	 * @returns A list of every single byte in the message that was passed.
	 *
	 * REQUIRES: a MidiEvent and the corresponding MidiMessage that is compatible with MetaMessage methods 
	 * 		(i.e. message must be an instance of MetaMessage).
	 * ENSURES: a list of all the bytes in the message that has been passed.
	 * */
	public ArrayList<Byte> parseMetaMessage(MidiMessage message, MidiEvent event, PrintWriter writer){
		if (message instanceof MetaMessage) {
			ArrayList<Byte> bytes = new ArrayList<>();

			// This is a META MESSAGE; convert the message and print basic information.
			// This information is WHEN the event happens, what kind of message it is and what TYPE it is.
			MetaMessage mm = (MetaMessage) message;
			writer.write("@" + event.getTick() + "(META_MESSAGE)");
			writer.write(String.format("%02x", Byte.parseByte(((Integer) mm.getType()).toString())));

			String mmString = String.format("%02x", Integer.parseInt(((Integer) mm.getType()).toString()));

			// Print the Message data, plus a new line
			try {
				writer.write(new String(mm.getData(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				writer.write("??? Data could not be encoded ???");
				e.printStackTrace();
			}
			for (Byte b : mm.getData()){
				bytes.add(b);
			}
			return bytes;
		}
		else {
			throw new IllegalArgumentException("An error occured - Please check the MidiMessage "
					+ "that was passed in is an instance of MetaMessage.");
		}
	}

	/**
	 * author: Dean Newberry
	 *
	 * parseShortMessage parses a ShortMessage, prints important information, and returns
	 * an ArrayList of ALL the bytes in the ShortMessage (INCLUDING bytes from unprinted information).
	 * THIS SHOULD PERFECTLY RETURN THE TRACK CHUNK OF A MIDI FILE (assuming it gets passed in).
	 *
	 * @param message - a MidiMessage.
	 * @param event - the MidiEvent the MidiMessage came from. Mainly used for the "tick" data printout.
	 * @returns A list of every single byte in the message that was passed.
	 *
	 * REQUIRES: a MidiEvent and the corresponding MidiMessage that is compatible with ShortMessage methods 
	 * 		(i.e. message must be an instance of ShortMessage).
	 * ENSURES: a list of all the bytes in the message that has been passed.
	 * */
	public ArrayList<Byte> parseShortMessage(MidiMessage message, MidiEvent event, PrintWriter writer){
		if (!(message instanceof ShortMessage) || !message.equals(event.getMessage())){
			throw new IllegalArgumentException("An error occured - Please check:\n"
					+ "That the MidiMessage that was passed in is an instance of ShortMessage,\n"
					+ "That the MidiMessage came from the supplied event");
		}
		else {
			ArrayList<Byte> bytes = new ArrayList<>();

			// This is a SHORT MESSAGE; convert the message and print basic information.
			// This information is WHEN the event happens, what kind of message it is,
			// what channel the message relates to and what TYPE of message it is.
			// HOWEVER, this will ONLY be printed if the command is recognized as IMPORTANT, such as:
			// NOTE_ON, NOTE_OFF, PROGRAM_CHANGE (i.e. instrument change).
			ShortMessage sm = (ShortMessage) message;
			String smString = String.format("%02x", Integer.parseInt(((Integer) sm.getStatus()).toString()));

			// If the command is NOTE_ON or NOTE_OFF...
			if (sm.getCommand() == Parser.NOTE_ON || sm.getCommand() == Parser.NOTE_OFF) {
				// Setup a String variable for the command, then record details of the command.
				String command;
				int channel = sm.getChannel();
				int key = sm.getData1();
				int octave = (key / 12)-1;
				int note = key % 12;
				String noteName = getNoteName(note);
				int velocity = sm.getData2();
				if (sm.getCommand() == Parser.NOTE_ON)
					command = "Note on";
				else
					command = "Note off";
				// After that, Print the details.
				writer.write("Channel " + channel + ": " + command + ", " +
						noteName + octave + " key=" + key + " velocity: " + velocity);
			} else if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE){
				writer.write("@" + event.getTick() + "(SHORT_MESSAGE)");
				writer.write(smString);
				writer.write(sm.getData1());
				writer.write("\n");
			}
			for (Byte b : sm.getMessage()){
				bytes.add(b);
			}
			return bytes;
		}
	}

	/**
	 * author: Dean Newberry
	 *
	 * getNoteName cleans up the process of finding the name of the note.
	 * It should be used anywhere you need to get Note Names from the note enum.
	 *
	 * @param note - the integer that represents the MIDI note (goes from 0 - 127)
	 *
	 * REQUIRES: an integer between 0 and 127 (inclusive)
	 * ENSURES: a String is returned that directly matches one of the values in the
	 * note enum.
	 * */
	public String getNoteName(int note){
		if (note < 0 || note > 127){
			throw new IllegalArgumentException(String.format("The note is out of range: note = %d, "
					+ "but should be between 0 and 127 (inclusive).", note));
		}
		return Parser.note.values()[note].name();
	}
	
}
