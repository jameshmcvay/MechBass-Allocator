package main;

import java.io.File;
import java.io.IOException;

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

public class Parser {

	 public static final int NOTE_ON = 0x90;
	    public static final int NOTE_OFF = 0x80;
//	    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

		public enum note {
			C, CSharp,
			DFlat, D, DSharp,
			EFlat, E, ESharp,
			F, FSharp,
			GFlat, G, GSharp,
			AFlat, A, ASharp,
			BFlat, B
		}

	public static void main(String[] args) {
		if (args.length < 1){

			return;

		}
		else{
			File midiFile = new File(args[0].toString());
			try {
				Sequencer sequencer = MidiSystem.getSequencer();
				sequencer.setSequence(MidiSystem.getSequence(midiFile));
				Sequence sequence = sequencer.getSequence();
				Track[] tracks = sequence.getTracks();
				int trackNo = tracks.length;
				System.out.println("Number of Tracks = " + trackNo);
				int trackNumber = 0;
				for (Track t : tracks){
					System.out.println("Track " + trackNumber++ + ": size = " + t.size() + "\n");
					 for (int i=0; i < t.size(); i++) {
		                MidiEvent event = t.get(i);
		                MidiMessage message = event.getMessage();
		                /*if (message instanceof MetaMessage) {
		                	System.out.println("@" + event.getTick() + " ");
		                	MetaMessage mm = (MetaMessage) message;
		                	System.out.println(String.format("%02x", Byte.parseByte(((Integer) mm.getType()).toString())));

		                	String hexString = "";

		                	for (Byte b : mm.getData()){
		                		hexString += String.format("%02x", Byte.parseByte(b.toString()));
		                	}
		                	byte[] bytes =  DatatypeConverter.parseHexBinary(hexString);
		                	System.out.println(new String(bytes, "UTF-8"));
		                	System.out.println();
		                }
		                else */if (message instanceof ShortMessage) {
		                	ShortMessage sm = (ShortMessage) message;
		                	String smString = String.format("%02x", Integer.parseInt(((Integer) sm.getStatus()).toString()));
		                	if (smString.startsWith("C") || smString.startsWith("c")){
		                		System.out.println("@" + event.getTick() + " ");
		                		System.out.println(smString);
		                		System.out.println(sm.getData1());
			                	System.out.println();
		                	}
		                }
					 }
				}

			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}