/**
 * authors: Dean Newberry, Dylan Macdonald
 * UPDATE: This Parser will be more "User-Driven" - 
 * It will Parse when the USER wants it to parse
 * */

package main;

import java.io.File;
import java.io.IOException;
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

public class Parser3 {

	public static final int TRACK_SEQUENCE_NAME = 0x03;
	public static final int INSTRUMENT_NAME = 0x04;
	public static final int NOTE_ON = 0x90;
	public static final int NOTE_OFF = 0x80;
	public static final int PROGRAM_CHANGE = 0xC0;
	//	    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

	public static final String[] VOICES = {
		"UNDEFINED",
		"Acoustic Grand Piano",
		"Bright Acoustic Piano",
		"Electric Grand Piano",
		"Honky-tonk Piano",
		"Electric Piano 1",
		"Electric Piano 2",
		"Harpsichord",
		"Clavi",
		"Celesta",
		"Glockenspiel",
		"Music Box",
		"Vibraphone",
		"Marimba",
		"Xylophone",
		"Tubular Bells",
		"Dulcimer",
		"Drawbar Organ",
		"Percussive Organ",
		"Rock Organ",
		"Church Organ",
		"Reed Organ",
		"Accordion",
		"Harmonica",
		"Tango Accordion",
		"Acoustic Guitar (nylon)",
		"Acoustic Guitar (steel)",
		"Electric Guitar (jazz)",
		"Electric Guitar (clean)",
		"Electric Guitar (muted)",
		"Overdriven Guitar",
		"Distortion Guitar",
		"Guitar harmonics",
		"Acoustic Bass",
		"Electric Bass (finger)",
		"Electric Bass (pick)",
		"Fretless Bass",
		"Slap Bass 1",
		"Slap Bass 2",
		"Synth Bass 1",
		"Synth Bass 2",
		"Violin",
		"Viola",
		"Cello",
		"Contrabass",
		"Tremolo Strings",
		"Pizzicato Strings",
		"Orchestral Harp",
		"Timpani",
		"String Ensemble 1",
		"String Ensemble 2",
		"SynthStrings 1",
		"SynthStrings 2",
		"Choir Aahs",
		"Voice Oohs",
		"Synth Voice",
		"Orchestra Hit",
		"Trumpet",
		"Trombone",
		"Tuba",
		"Muted Trumpet",
		"French Horn",
		"Brass Section",
		"SynthBrass 1",
		"SynthBrass 2",
		"Soprano Sax",
		"Alto Sax",
		"Tenor Sax",
		"Baritone Sax",
		"Oboe",
		"English Horn",
		"Bassoon",
		"Clarinet",
		"Piccolo",
		"Flute",
		"Recorder",
		"Pan Flute",
		"Blown Bottle",
		"Shakuhachi",
		"Whistle",
		"Ocarina",
		"Lead 1 (square)",
		"Lead 2 (sawtooth)",
		"Lead 3 (calliope)",
		"Lead 4 (chiff)",
		"Lead 5 (charang)",
		"Lead 6 (voice)",
		"Lead 7 (fifths)",
		"Lead 8 (bass + lead)",
		"Pad 1 (new age)",
		"Pad 2 (warm)",
		"Pad 3 (polysynth)",
		"Pad 4 (choir)",
		"Pad 5 (bowed)",
		"Pad 6 (metallic)",
		"Pad 7 (halo)",
		"Pad 8 (sweep)",
		"FX 1 (rain)",
		"FX 2 (soundtrack)",
		"FX 3 (crystal)",
		"FX 4 (atmosphere)",
		"FX 5 (brightness)",
		"FX 6 (goblins)",
		"FX 7 (echoes)",
		"FX 8 (sci-fi)",
		"Sitar",
		"Banjo",
		"Shamisen",
		"Koto",
		"Kalimba",
		"Bag pipe",
		"Fiddle",
		"Shanai",
		"Tinkle Bell",
		"Agogo",
		"Steel Drums",
		"Woodblock",
		"Taiko Drum",
		"Melodic Tom",
		"Synth Drum",
		"Reverse Cymbal",
		"Guitar Fret Noise",
		"Breath Noise",
		"Seashore",
		"Bird Tweet",
		"Telephone Ring",
		"Helicopter",
		"Applause",
		"Gunshot"
	};

	private File midiFile;
	private Sequencer sequencer;
	private Sequence sequence;
	private Track[] tracks;
	
	public Parser3(String fileName){
		midiFile = new File(fileName);
		setUpParser();
	}
	
	public File getFile(){
		return midiFile;
	}
	
	public void setFile(String fileName){
		midiFile = new File(fileName);
		setUpParser();
	}
	
	public Sequencer getSequencer(){
		return sequencer;
	}
	
	public Sequence getSequence(){
		return sequence;
	}
	
	public Track[] getTracks(){
		return tracks;
	}
	
	public Track getTrack(int i){
		if (i < 0 || i >= tracks.length)
			throw new IndexOutOfBoundsException(
				String.format("Index out of Bounds! You specified %d "
				+ "as your index; the acceptable range is 0 - %d.",
				i, tracks.length-1));
		else
			return tracks[i];
	}
	
	public void setUpParser(){
		// Set the sequencer - the thing that allows MIDI files to be played
		try {
			sequencer = MidiSystem.getSequencer();
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Set the sequence to be examined to the midiFile...
		try {
			sequencer.setSequence(MidiSystem.getSequence(midiFile));
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ... And store the sequence.
		sequence = sequencer.getSequence();

		// Store the tracks and the number of tracks, printing the number.
		Track[] tracks = sequence.getTracks();
	}
	
	public void parse(){
		int trackNo = tracks.length;
		System.out.println("Number of Tracks = " + trackNo);
		
		// Set a counting variable
		int trackNumber = 0;

		// FOR EVERY TRACK...
		for (Track t : tracks){
			// ... Print out some basic information (Track Number and the Size of the track)
			System.out.println("Track " + trackNumber++ + ": size = " + t.size() + "\n");

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
					ArrayList<Byte> byteList = parseMetaMessage(message, event);
					
					MetaMessage mm = (MetaMessage) message;
					String mmString = String.format("%02x", Integer.parseInt(((Integer) mm.getType()).toString()));
					if (mmString.equals("03"))
						try {
							track_SequenceName = new String(mm.getData(), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					if (mmString.equals("04")){
						try {
							instrumentName = new String(mm.getData(), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						instrumentChanges.add(instrumentName);
					}

				}
				else if (message instanceof ShortMessage) {
					ArrayList<Byte> byteList = parseShortMessage(message, event);

					ShortMessage sm = (ShortMessage) message;
					String smString = String.format("%02x", Integer.parseInt(((Integer) sm.getStatus()).toString()));

					if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE){
						instrumentChanges.add(Parser3.VOICES[sm.getData1()] + " (" + sm.getData1() + ")");
					}
				}
			}
			System.out.println("Number of Channels: " + channels + ",\n" +
				"Track Name: " + track_SequenceName + ",\n" + 
				"STARTING Instrument Name: " + instrumentName + ",\n" + 
				"All Instruments used: ");
			for (String s : instrumentChanges)
				System.out.println("\t" + s);
			System.out.println();
		}
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
	public ArrayList<Byte> parseMetaMessage(MidiMessage message, MidiEvent event){
		if (message instanceof MetaMessage) {
			ArrayList<Byte> bytes = new ArrayList<>();

			// This is a META MESSAGE; convert the message and print basic information.
			// This information is WHEN the event happens, what kind of message it is and what TYPE it is.
			MetaMessage mm = (MetaMessage) message;
			System.out.println("@" + event.getTick() + "(META_MESSAGE)");
			System.out.println(String.format("%02x", Byte.parseByte(((Integer) mm.getType()).toString())));

			String mmString = String.format("%02x", Integer.parseInt(((Integer) mm.getType()).toString()));

			// Print the Message data, plus a new line
			try {
				System.out.println(new String(mm.getData(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				System.out.println("??? Data could not be encoded ???");
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
	public ArrayList<Byte> parseShortMessage(MidiMessage message, MidiEvent event){
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
			if (sm.getCommand() == NOTE_ON || sm.getCommand() == NOTE_OFF) {
				// Setup a String variable for the command, then record details of the command.
				String command;
				int channel = sm.getChannel();
				int key = sm.getData1();
				int octave = (key / 12)-1;
				int note = key % 12;
				String noteName = getNoteName(note);
				int velocity = sm.getData2();
				if (sm.getCommand() == NOTE_ON)
					command = "Note on";
				else
					command = "Note off";
				// After that, Print the details.
				System.out.println("Channel " + channel + ": " + command + ", " +
						noteName + octave + " key=" + key + " velocity: " + velocity);
			} else if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE){
				System.out.println("@" + event.getTick() + "(SHORT_MESSAGE)");
				System.out.println(smString);
				System.out.println(sm.getData1());
				System.out.println();
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
		return Parser3.note.values()[note].name();
	}

	public enum note {
		C, CSharp,
		D, DSharp,
		E,
		F, FSharp,
		G, GSharp,
		A, ASharp,
		B
	}
	public static void main(String[] args) {
		if (args.length < 1){
			return;
		}
		else{
			new Parser3(args[0].toString());
		}
	}

}