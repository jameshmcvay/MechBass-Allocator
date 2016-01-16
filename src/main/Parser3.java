/**
 * authors: Dean Newberry, Dylan Macdonald
 * UPDATE: This Parser will be more "User-Driven" - It will parse upon 
 * creation, and also when the user wants it to parse.
 * */

package main;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

	// A list of all the instruments; use VOICES[xxx] to get the instrument.
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
	
	// unnecessary?
	private ArrayList<ArrayList<Integer>> bytelists = new ArrayList<>();
	
	// MIDI File Stuffs
	private ArrayList<String> trackNames = new ArrayList<>();
	private ArrayList<ArrayList<String>> trackInstruments = new ArrayList<>();
	
	// A helper list that stores the ticks of all the events
	private ArrayList<Long> ticks = new ArrayList<>();
	
	private boolean ready = false;
	
	/**
	 * author: Dean Newberry
	 * 
	 * A constructor that allows the user to pass in the path of the
	 * file they want.
	 * 
	 * I consider this method "safe" because the Parser is just being created.
	 * That said, PLEASE ensure the string you pass in is a filepath to a MIDI
	 * file, or the Parser3 will NOT work.
	 * 
	 * REQUIRES: A String containing a valid filepath to a MIDI file.
	 * ENSURES: A new instance of Parser3 that will actually do 
	 * 		semi-coherent things (assuming, of course, that the MIDI file is
	 * 		written in a semi-coherent way).
	 * */
	public Parser3(String fileName){
		midiFile = new File(fileName);
		setUpParser();
		quietParse();
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * A constructor that allows the user to pass in a file.
	 * 
	 * I consider this method "(Potentially) unsafe" because, 
	 * although the Parser is just being created, if the File object
	 * changes WITHOUT the "setUpParser()" method being called, the 
	 * behaviour is undefined. Setting the File object to final so that
	 * it can't be changed *MIGHT* help.
	 * 
	 * REQUIRES: A File object of a MIDI file.
	 * ENSURES: A new instance of Parser3 that will actually do 
	 * 		semi-coherent things (assuming, of course, that the MIDI file is
	 * 		written in a semi-coherent way, AND the file doesn't change).
	 * */
	public Parser3(File file){
		midiFile = file;
		setUpParser();
		quietParse();
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * A constructor that allows the user to pass in a Sequence.
	 * 
	 * This method was created as a convenience for when the MIDI File
	 * or Filename is unavailable, but a sequence is.
	 * 
	 * Please note that this is VERY VERY nearly like the setUpParser() method,
	 * but it doesn't use the file.
	 * 
	 * REQUIRES: A Sequence object of a MIDI file.
	 * ENSURES: A new instance of Parser3 that will actually do 
	 * 		semi-coherent things.
	 * */
	public Parser3(Sequence s){
		midiFile = null;
		sequence = s;
		setUpParserSeq();
		quietParse();
	}
	
	/**
	 * Returns the file.
	 * 
	 * I consider this method "ABSOLUTELY safe" because the Parser 
	 * just returns the file it's using.
	 * 
	 * REQUIRES: N/A
	 * ENSURES: The user can receive the file the parser is currently using. 
	 * */
	public File getFile(){
		return midiFile;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Allows the user to set the file, then automatically sets the parser
	 * up as if it was just constructed.
	 * 
	 * I consider this method "(Potentially) unsafe" because, 
	 * although the Parser will be almost like new, if the File object
	 * changes WITHOUT the "setUpParser()" method being called, the 
	 * behaviour is undefined. Setting the File object to final so that
	 * it can't be changed *MIGHT* help.
	 * 
	 * REQUIRES: A File object of a MIDI file.
	 * ENSURES: The file the parser uses gets changed, and a method will make
	 * 		the parser practically "as-new", and ready to parse the new MIDI 
	 * 		(assuming, of course, the file doesn't change).
	 * */
	public void setFile(String fileName){
		midiFile = new File(fileName);
		setUpParser();
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Allows the user to set the file, then automatically sets the parser
	 * up as if it was just constructed.
	 * 
	 * I consider this method "(Potentially) unsafe" because, 
	 * although the Parser will be almost like new, if the File object
	 * changes WITHOUT the "setUpParser()" method being called, the 
	 * behaviour is undefined. Setting the File object to final so that
	 * it can't be changed *MIGHT* help.
	 * 
	 * REQUIRES: A File object of a MIDI file.
	 * ENSURES: The file the parser uses gets changed, and a method will make
	 * 		the parser practically "as-new", and ready to parse the new MIDI 
	 * 		(assuming, of course, the file doesn't change).
	 * */
	public void setFile(File file){
		midiFile = file;
		setUpParser();
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Returns the Sequencer the parser is using.
	 * 
	 * I consider this method "ABSOLUTELY safe" because the Parser 
	 * just returns the Sequencer it's using.
	 * 
	 * REQUIRES: N/A
	 * ENSURES: The user can receive the Sequencer the parser is currently using.
	 * */
	public Sequencer getSequencer(){
		return sequencer;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Allows the user to set the Sequencer the parser is using.
	 * 
	 * I consider this method "ABSOLUTELY UNSAFE" - in fact, I can't see any
	 * immediate benefit to using it. That said, there may be a reason this must
	 * be done.
	 * 
	 * REQUIRES: A Sequencer and 5 shots of absinthe (maybe an extra 2 for luck).
	 * ENSURES: The user can change the sequencer the parser is using 
	 * 		(because YOLO bruh).
	 * */
	public void setSequencer(Sequencer s){
		sequencer = s;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Returns the Sequence the parser is using.
	 * 
	 * I consider this method "ABSOLUTELY safe" because the Parser 
	 * just returns the Sequence it's using.
	 * 
	 * REQUIRES: N/A
	 * ENSURES: The user can receive the Sequence the parser is currently using.
	 * */
	public Sequence getSequence(){
		return sequence;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Allows the user to set the Sequence the parser is using.
	 * 
	 * I consider this method "ABSOLUTELY UNSAFE" - in fact, I can't see any
	 * immediate benefit to using it. That said, there may be a reason this must
	 * be done.
	 * 
	 * HOWEVER, this CAN be used to test the Solver. So that's something I guess...
	 * 
	 * REQUIRES: A Sequence and 5 shots of absinthe (maybe an extra 2 for luck).
	 * ENSURES: The user can change the sequence the parser is using 
	 * 		(because YOLO bruh).
	 * */
	public void setSequence(Sequence s){
		sequence = s;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Returns the Tracks from the Sequence the parser is using.
	 * 
	 * I consider this method "ABSOLUTELY safe" because the Parser 
	 * just returns the Tracks it's using.
	 * 
	 * REQUIRES: N/A
	 * ENSURES: The user can receive the Tracks the parser is currently using.
	 * */
	public Track[] getTracks(){
		return tracks;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Returns a specific Track from the sequence the parser is using, 
	 * assuming the supplied integer is between 0 and tracks.length.
	 * 
	 * I consider this method "ABSOLUTELY safe" because the Parser 
	 * just returns the specified Track from the sequence it's using,
	 * assuming the supplied integer is between 0 and tracks.length.
	 * 
	 * REQUIRES: An integer between 0 and tracks.length.
	 * ENSURES: The user can receive the specified Track from the sequence
	 * 		the parser is currently using.
	 * */
	public Track getTrack(int i){
		if (i < 0 || i >= tracks.length)
			throw new IndexOutOfBoundsException(
				String.format("Index out of Bounds! You specified %d "
				+ "as your index; the acceptable range is 0 - %d.",
				i, tracks.length-1));
		else
			return tracks[i];
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Returns the list of bytelists the Parser stored for the user.
	 * 
	 * I consider this method "safe" because the Parser just returns 
	 * the list of lists. But PLEASE don't change them...
	 * 
	 * REQUIRES: N/A
	 * ENSURES: The user can receive the List of List of Bytes.
	 * */
	public ArrayList<ArrayList<Integer>> getByteLists(){
		return bytelists;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Gets the ArrayList of names for each track.
	 * The index of the name corresponds to the index of the track.
	 *
	 * REQUIRES: N/A.
	 * ENSURES: Returns the ArrayList of track names.
	 * */
	public ArrayList<String> getTrackNames() {
		return trackNames;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Gets the name of the track at the specified index.
	 * The index of the name corresponds to the index of the track.
	 *
	 * REQUIRES: "i" must be between 0 and the size of the TrackName list.
	 * ENSURES: Returns name of the track at index "i".
	 * */
	public String getTrackName(int i) {
		if (i < 0 || i >= trackNames.size())
			throw new IndexOutOfBoundsException("'i' is out of bounds. You "
					+ "specifed " + i + ", but the acceptable range is 0 - "
					+ (trackNames.size()-1) + ".");
		return trackNames.get(i);
	}
	
/**
	 * author: Dean Newberry
	 * 
	 * Gets the List of Lists of instruments that are used in the MIDI file.
	 * The FIRST corresponds to the index of the track, the SECOND index 
	 * corresponds to the instrument.
	 *
	 * REQUIRES: N/A.
	 * ENSURES: Returns the list of lists of instruments used in the MIDI file.
	 * */
	public ArrayList<ArrayList<String>> getTrackInstruments() {
		return trackInstruments;
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Gets the List of instruments that are used in the specified track.
	 * The index of the returned list corresponds to the instrument.
	 *
	 * REQUIRES: "i" must be between 0 and the size of the TrackInstruments list.
	 * ENSURES: Returns the list of instruments used in the track at index "i".
	 * */
	public ArrayList<String> getTrackInstruments(int i) {
		if (i < 0 || i >= trackInstruments.size())
			throw new IndexOutOfBoundsException("'i' is out of bounds. You "
					+ "specifed " + i + ", but the acceptable range is 0 - "
					+ (trackInstruments.size()-1) + ".");
		return trackInstruments.get(i);
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Gets the instrument from the specified track and index.
	 * This method is probably unnecessary, but is here for completion.
	 *
	 * REQUIRES: "i" must be between 0 and the size of the TrackInstruments list.
	 * ENSURES: Returns the list of instruments used in the track at index "i".
	 * */
	public String getTrackInstrument(int i, int o) {
		if (i < 0 || i >= trackInstruments.size())
			throw new IndexOutOfBoundsException("'i' is out of bounds. You "
					+ "specifed " + i + ", but the acceptable range is 0 - "
					+ (trackInstruments.size()-1) + ".");
		if (o < 0 || o >= trackInstruments.get(i).size())
			throw new IndexOutOfBoundsException("'o' is out of bounds. You "
					+ "specifed " + o + ", but the acceptable range is 0 - "
					+ (trackInstruments.get(i).size()-1) + ".");
		return trackInstruments.get(i).get(o);
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Gets the List of the "ticks" of all the short messages in the MIDI file.
	 * These "ticks" are basically the timestamps of when the events occur.
	 * 
	 * Strictly speaking, this method should only be needed for testing purposes.
	 *
	 * REQUIRES: N/A.
	 * ENSURES: Returns the list of ticks in the short messages in the MIDI file.
	 * */
	public ArrayList<Long> getTicks() {
		return ticks;
	}	

	/**
	 * author: Dean Newberry
	 * 
	 * Sets the parser up to parse the MIDI file. 
	 * THIS METHOD *WILL* FAIL IF INAPPROPRIATE INPUTS WERE USED FOR
	 * CONSTRUCTORS/setFile METHODS.
	 * 
	 * I consider this method "safe", with the caveat that the file has been
	 * properly specified and has not changed.
	 * 
	 * IF THIS METHOD IS *NOT* CALLED ON IT'S OWN, 
	 * I CANNOT GUARANTEE IT'S BEHAVIOUR.
	 * 
	 * REQUIRES: The File the Parser is using MUST be a valid MIDI file.
	 * ENSURES: The Parser will be set up and ready to parse the MIDI file.
	 * */
	public void setUpParser(){
		ready = false;
		boolean problem = false;
		// Set the sequencer - the thing that allows MIDI files to be played
		try {
			sequencer = MidiSystem.getSequencer();
		} catch (MidiUnavailableException e) {
			System.out.println("There is a problem with this MIDI file!");
			problem = true;
			e.printStackTrace();
		}
		// Set the sequence to be examined to the midiFile...
		try {
			sequencer.setSequence(MidiSystem.getSequence(midiFile));
		} catch (InvalidMidiDataException e) {
			System.out.println("There is a problem with this MIDI file!");
			problem = true;
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ... And store the sequence.
		try{
			sequence = sequencer.getSequence();
			tracks = sequence.getTracks();
		}
		catch (NullPointerException e){
			System.out.println("There is a problem with this MIDI file!");
			problem = true;
			e.printStackTrace();
		}
		bytelists.clear();
		trackNames.clear();
		for (ArrayList<String> al : trackInstruments)
			al.clear();
		trackInstruments.clear();
		ticks.clear();
		
		if (ready == problem){
			// if there are no problems...
			ready = true;
			// the parser is READY!
		}
	}
	
	/**
	 * author: Dean Newberry
	 * 
	 * Sets the parser up to parse the Sequence. 
	 * THIS METHOD *WILL* FAIL IF INAPPROPRIATE INPUTS WERE USED FOR
	 * CONSTRUCTORS/setFile METHODS.
	 * 
	 * ALSO, this method should ONLY be used if the Parser is instantiated 
	 * from a sequence.
	 * 
	 * I consider this method "safe", with the caveat that the file has been
	 * properly specified and has not changed.
	 * 
	 * IF THIS METHOD IS *NOT* CALLED ON IT'S OWN, 
	 * I CANNOT GUARANTEE IT'S BEHAVIOUR.
	 * 
	 * REQUIRES: The File the Parser is using MUST be a valid MIDI file.
	 * ENSURES: The Parser will be set up and ready to parse the MIDI file.
	 * */
	public void setUpParserSeq(){
		ready = false;
		boolean problem = false;
		// Set the sequencer - the thing that allows MIDI files to be played
		try {
			sequencer = MidiSystem.getSequencer();
		} catch (MidiUnavailableException e) {
			System.out.println("There is a problem with this MIDI file!");
			problem = true;
			e.printStackTrace();
		}
		// Set the sequence to be examined to the sequence passed in...
		try {
			sequencer.setSequence(sequence);
		} catch (InvalidMidiDataException e) {
			System.out.println("There is a problem with this MIDI file!");
			problem = true;
			e.printStackTrace();
		}
		// ... And store the sequence.
		try{
			tracks = sequence.getTracks();
		}
		catch (NullPointerException e){
			System.out.println("There is a problem with this MIDI Sequence!");
			problem = true;
			e.printStackTrace();
		}
		bytelists.clear();
		trackNames.clear();
		for (ArrayList<String> al : trackInstruments)
			al.clear();
		trackInstruments.clear();
		ticks.clear();
		
		if (ready == problem){
			// if there are no problems...
			ready = true;
			// the parser is READY!
		}		
	}
	
	public void parse(){
		// If the parser isn't set up properly... 
		if (!ready){
			System.out.println("The parser is not ready. "
			+ "Please specify a proper file to load data.");
			return;
			// Tell the user and do nothing.
		}
		// Otherwise...
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
			ArrayList<String> instrumentChanges = new ArrayList<>();

			for (int i=0; i < t.size(); i++) {
				// GO THROUGH ALL THE EVENTS...
				MidiEvent event = t.get(i);
				// ... And get the messages at each one.
				MidiMessage message = event.getMessage();

				if (message instanceof MetaMessage) {
					ArrayList<Integer> byteList = parseMetaMessage(message, event, true);
					bytelists.add(byteList);
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
					ArrayList<Integer> byteList = parseShortMessage(message, event, true);
					bytelists.add(byteList);
					
					ShortMessage sm = (ShortMessage) message;

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
			trackNames.add(track_SequenceName);
			trackInstruments.add(instrumentChanges);
		}
	}

	/**
	 * author: Dean Newberry
	 * 
	 * This is a carbon copy of the parse method, except there is NO printing
	 * (EXCEPT for error messages and the warning that prints to let the user know
	 * the parser isn't ready).
	 * */
	public void quietParse(){
		if (!ready){
			System.out.println("The parser is not ready. "
			+ "Please specify a proper file to load data.");
			return;
		}
		
		// FOR EVERY TRACK...
		for (Track t : tracks){
			// ... Print out some basic information (Track Number and the Size of the track)
			String instrumentName = "Undefined";
			String track_SequenceName = "Undefined";
			ArrayList<String> instrumentChanges = new ArrayList<>();

			for (int i=0; i < t.size(); i++) {
				// GO THROUGH ALL THE EVENTS...
				MidiEvent event = t.get(i);
				// ... And get the messages at each one.
				MidiMessage message = event.getMessage();

				if (message instanceof MetaMessage) {
					ArrayList<Integer> byteList = parseMetaMessage(message, event, false);
					bytelists.add(byteList);
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
					ArrayList<Integer> byteList = parseShortMessage(message, event, false);
					bytelists.add(byteList);
					
					ShortMessage sm = (ShortMessage) message;

					if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE){
						instrumentChanges.add(Parser3.VOICES[sm.getData1()] + " (" + sm.getData1() + ")");
					}
				}
			}
			trackNames.add(track_SequenceName);
			trackInstruments.add(instrumentChanges);
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
	 * @param verbose - a boolean flag indicating whether extra information should be printed out. 
	 * @returns A list of every single byte in the message that was passed.
	 *
	 * REQUIRES: a MidiEvent and the corresponding MidiMessage that is compatible with MetaMessage methods 
	 * 		(i.e. message must be an instance of MetaMessage).
	 * ENSURES: a list of all the bytes in the message that has been passed.
	 * */
	public ArrayList<Integer> parseMetaMessage(MidiMessage message, MidiEvent event, boolean verbose){
		if (message instanceof MetaMessage) {
			ArrayList<Integer> bytes = new ArrayList<>();

			// This is a META MESSAGE; convert the message and print basic information.
			// This information is WHEN the event happens, what kind of message it is and what TYPE it is.
			MetaMessage mm = (MetaMessage) message;
			if (verbose){
				System.out.println("@" + event.getTick() + "(META_MESSAGE)");
				System.out.println(String.format("%02x", Byte.parseByte(((Integer) mm.getType()).toString())));
				// Print the Message data, plus a new line
				try {
					System.out.println(new String(mm.getData(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					System.out.println("??? Data could not be encoded ???");
					e.printStackTrace();
				}
			}
			bytes.add(mm.getStatus());
			for (Byte b : mm.getData()){
				bytes.add((int) (b & 0xFF));
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
	 * @param verbose - a boolean flag indicating whether extra information should be printed out.
	 * @returns A list of every single byte in the message that was passed.
	 *
	 * REQUIRES: a MidiEvent and the corresponding MidiMessage that is compatible with ShortMessage methods 
	 * 		(i.e. message must be an instance of ShortMessage).
	 * ENSURES: a list of all the bytes in the message that has been passed.
	 * */
	public ArrayList<Integer> parseShortMessage(MidiMessage message, MidiEvent event, boolean verbose){
		if (!(message instanceof ShortMessage) || !message.equals(event.getMessage())){
			throw new IllegalArgumentException("An error occured - Please check:\n"
					+ "That the MidiMessage that was passed in is an instance of ShortMessage,\n"
					+ "That the MidiMessage came from the supplied event");
		}
		else {
			ArrayList<Integer> bytes = new ArrayList<>();

			// This is a SHORT MESSAGE; convert the message and print basic information.
			// This information is WHEN the event happens, what kind of message it is,
			// what channel the message relates to and what TYPE of message it is.
			// HOWEVER, this will ONLY be printed if the command is recognized as IMPORTANT, such as:
			// NOTE_ON, NOTE_OFF, PROGRAM_CHANGE (i.e. instrument change).
			ShortMessage sm = (ShortMessage) message;
			String smString = String.format("%02x", Integer.parseInt(((Integer) sm.getStatus()).toString()));
			if (verbose){
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
					System.out.println("@" + event.getTick() + "(SHORT_MESSAGE)\tChannel " + channel + ": " + command + ", " +
							noteName + octave + " key=" + key + " velocity: " + velocity);
				} else if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE){
					System.out.println("@" + event.getTick() + "(SHORT_MESSAGE)");
					System.out.println(smString);
					System.out.println(sm.getData1());
					System.out.println();
				}
			}
			for (Byte b : sm.getMessage()){
				bytes.add((int) (b & 0xFF));
			}
			
			ticks.add(event.getTick());
			
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
			/* Go through the process of extracting the Sequence from a file */
			// Set up variables (sequencer, sequence and File).
			Sequencer seq = null;
			Sequence s = null;
			File file = new File(args[0].toString());
			
			// Get Sequencer
			try {
				seq = MidiSystem.getSequencer();
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
			// Set the sequence to be examined to the midiFile...
			try {
				seq.setSequence(MidiSystem.getSequence(file));
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ... And store the sequence.
			try{
				s = seq.getSequence();
			}
			catch (NullPointerException e){
				e.printStackTrace();
			}
			
			// Now instantiate the parser using a sequence.
			Parser3 p = new Parser3(s);
			// It should automatically parse the file.
			
			// print the list of the track names given.
			System.out.println("Track Names:\n");
			for (String st : p.getTrackNames())
				System.out.println("\t" + st);
			
			// formating neatness stuffs.
			System.out.println("\n=====\n");
			
			// print the list of the list of track instruments.
			System.out.println("Track Instruments:\n");
			int count = 0; // This keeps track of which track we're in.
			for (ArrayList<String> st : p.getTrackInstruments()){
				System.out.println("\t" + count++ + ":");
				for (String str : st){
					System.out.println("\t\t" + str);
				}
			}
		}
	}

}