package solver;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;
import static javax.sound.midi.ShortMessage.PITCH_BEND;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *
 * @author Andrew Palmer
 */

/**
 *cleaner cuts the excess events after solving and adds pre-positioning.
 */
public class Cleaner {

	/**
	 * Takes a sequence and removes track 0. Run a MIDI through this after solving to\
	 * remove unalocated events
	 * @param seq - The Sequence
	 * @return the cleaned Sequence
	 */
	public static Sequence clean(Sequence seq){
		seq.deleteTrack(seq.getTracks()[0]);
		return seq;
	}

	/**
	 * Turns all note ons with velocity 0 into note offs.
	 * @param seq
	 * @return
	 */
	public static Sequence fixStupidity(Sequence seq){
		try{
			for(int i = 0; i < seq.getTracks().length; i++){
				Track cur = seq.getTracks()[i];
				//check that each note on doesn't conflict with the previous note off
				for(int j = cur.size()-1; j == 0; --j){
					MidiMessage midmsg = cur.get(j).getMessage();
					if (midmsg instanceof ShortMessage){
						ShortMessage shrtmsg = (ShortMessage) midmsg;
						if(shrtmsg.getCommand() == NOTE_ON && shrtmsg.getData2() == 0){
							ShortMessage noteOff = new ShortMessage(NOTE_OFF, shrtmsg.getChannel(), shrtmsg.getData1(), shrtmsg.getData2());
							MidiEvent event = new MidiEvent(noteOff,cur.get(j).getTick());
							if(cur.remove(cur.get(j))) cur.add(event);
						}
					}
				}
			}

		}
		catch(InvalidMidiDataException e){
			e.printStackTrace();
		}
		return seq;
	}

	/**
	 * Scans a track for any events which can't be played due to transition interval
	 * @param seq - The sequence to be checked
	 * @param inter - The transition interval in s^(-6)
	 * @param strings - the information on notes played by and intervals on the strings
	 * @return the number of conflicting notes
	 */
	public static int scanTimings(Sequence seq, MekString[] strings){
		int conflicts = 0;
		//for each track
		for(int i = 0; i < seq.getTracks().length; i++){
			Track cur = seq.getTracks()[i];
			//check that each note on doesn't conflict with the previous note off
			for(int j = cur.size()-1; j == 0; --j){
				MidiMessage midNoteOn = cur.get(j).getMessage();
				if (midNoteOn instanceof ShortMessage){
					ShortMessage noteOn = (ShortMessage) midNoteOn;
					// check what kind of command it is (note on/off, etc)
					if(noteOn.getCommand() == NOTE_ON){
						// check whether or not it is actually a note on or prepos
						// Possibly just check that each prepos is followed by the same note?
						if (noteOn.getData2()!=1){
							int note1 = noteOn.getData1();
							//find the previous note off
							ShortMessage noteOff;
							int prevIndex = getPrev(j,cur);
							if(prevIndex>0){
								noteOff = (ShortMessage) cur.get(prevIndex).getMessage();
								int note2 = noteOff.getData1();
								if(strings[i].conflicting(note1, note2, cur.get(prevIndex).getTick() - cur.get(j).getTick())) conflicts++;
							}
						}
					}
				}
			}
		}
		return conflicts;
	}

	/**
	 * Gets the previous note
	 */
	private static int getPrev(int index, Track cur){
		for(int k = index-1; k == 0; --k){
			MidiMessage midNoteOff = cur.get(k).getMessage();
			if(midNoteOff instanceof ShortMessage){
				ShortMessage noteOff = (ShortMessage) midNoteOff;
				if(noteOff.getCommand() == NOTE_OFF){
					return k;
				}
			}
		}
		return 0;
	}

	/**
	 * Adds pre positioning notes to the MIDI so MekBass can play it
	 * @param seq - The sequence to add prepositioning to.
	 * @param preTime - The time in ��s to add before the string MUST be prepositioned.
	 * @return
	 */
	public static Sequence prePos(Sequence seq, long preTime, MekString[] strings){
		//for each track
		for(int i = 0; i < seq.getTracks().length; i++){
			Track cur = seq.getTracks()[i];
			//add a prepos event for each note that is not consecutive
			for(int j = 0; j < cur.size(); j++){
				System.out.printf("Index %d\n ",j);
				MidiMessage midNoteOn = cur.get(j).getMessage();
				if (midNoteOn instanceof ShortMessage){
					ShortMessage noteOn = (ShortMessage) midNoteOn;
					if(noteOn.getCommand() == NOTE_ON){
						// check whether or not it is actually a note on...
						if (noteOn.getData2()!=1){
							int note1 = noteOn.getData1();
							//find the previous note off
							ShortMessage noteOff;
							int prevIndex = getPrev(j,cur);
							System.out.printf("prev index %d\n", prevIndex);
							if(prevIndex>0){
								noteOff = (ShortMessage) cur.get(prevIndex).getMessage();
								int note2 = noteOff.getData1();
								//if the note is different and they don't clash
								if(note2 != note1 && !strings[i].conflicting(note1, note2, cur.get(prevIndex).getTick() - cur.get(j).getTick() - preTime)){
									try {
										cur.add(new MidiEvent(new ShortMessage(NOTE_ON,0,noteOn.getData1(),1) , cur.get(j).getTick() - strings[i].difference(note1, note2) - preTime));
									} catch (ArrayIndexOutOfBoundsException e) {
										e.printStackTrace();
									} catch (InvalidMidiDataException e) {
										e.printStackTrace();
									}
									j++;
//									System.out.printf("Added prepos for");
								}
								else{
									System.out.printf("%d: note %d to close to preceeding note.", cur.get(j).getTick() ,note1);
								}
							}
							else if(prevIndex == 0){
								try {
									cur.add(new MidiEvent(new ShortMessage(NOTE_ON,0,noteOn.getData1(),1) , cur.get(j).getTick() - preTime));
								} catch (ArrayIndexOutOfBoundsException e) {
									e.printStackTrace();
								} catch (InvalidMidiDataException e) {
									e.printStackTrace();
								}
								j++;
							}
						}
					}
				}
			}
		}
		return seq;
	}

	/**
	 * delayNote should be used if a note conflict occurs and the user wishes to delay the note
	 *
	 * @param seq: The squence the note is in.
	 * @param str: The string the note is currently on.
	 * @param event: The MidiEvent representing the note
	 * @param delay: The amount to delay the note (s^-6)
	 */
	public static void delayNote(Sequence seq, MekString str, int event, long delay){

	}

	/**
	 * dropNote should be used if a note conflict occurs and the user wishes to drop the note.
	 * The note will note be played.
	 *
	 * @param seq: The squence the note is in.
	 * @param str: The string the note is currently on.
	 * @param event: The MidiEvent representing the note
	 * @param delay: The amount to delay the note (s^-6)
	 */
	public static void dropNote(Sequence seq, MekString str, int event){

	}
}
