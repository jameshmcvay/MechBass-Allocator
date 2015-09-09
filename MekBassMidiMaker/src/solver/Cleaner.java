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
				for(int j = 0; j < cur.size(); j++){
					MidiMessage midmsg = cur.get(j).getMessage();
					if (midmsg instanceof ShortMessage){
						ShortMessage shrtmsg = (ShortMessage) midmsg;
						if(shrtmsg.getCommand() == NOTE_ON && shrtmsg.getData2() == 0){
//							System.out.printf("found false note off");
							ShortMessage noteOff = new ShortMessage(NOTE_OFF, shrtmsg.getChannel(), shrtmsg.getData1(), shrtmsg.getData2());
							MidiEvent event = new MidiEvent(noteOff,cur.get(j).getTick());
							cur.remove(cur.get(j));
							cur.add(event);
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
	 * Returns true if conflicts exist in the sequence.
	 * @param seq
	 * @param strings
	 * @return
	 */
	public boolean hasConflicts(Sequence seq, MekString[] strings){
		if(scanTimings(seq,strings) > 0) return true;
		return false;
	}
	
	private static int getPrev(int index, Track cur){
		return getPrev(index, cur, NOTE_OFF);
	}
	
	/**
	 * Gets the previous note 
	 */
	private static int getPrev(int index, Track cur, int Command){
//		System.out.printf("getPrev initial index %d\n",index);
		int prev = 0;
		for(int k = index; k > 0; k--){
//			System.out.printf("found note at index %d\n",k);
			MidiMessage midNoteOff = cur.get(k).getMessage();			
			if(midNoteOff instanceof ShortMessage){
				ShortMessage noteOff = (ShortMessage) midNoteOff;
				if(noteOff.getCommand() == Command){
//					System.out.printf("found previous note at index %d\n",k);
					prev = k;
				}
			}
		}
		return prev;
	}

	/**
	 * Adds pre positioning notes to the MIDI so MekBass can play it. Does no conflict finding
	 * @param seq - The sequence to add prepositioning to.
	 * @param preTime - The time in ��s to add before the string MUST be prepositioned. 
	 * @return The prepositioned sequence.
	 */
	public static Sequence prePos(Sequence seq, long preTime, MekString[] strings){
		//for each track
		for(int i = 0; i < seq.getTracks().length; i++){
			Track cur = seq.getTracks()[i];
			//add a prepos event for each note that is not consecutive
			for(int j = 0; j < cur.size(); j++){
//				System.out.printf("Index %d\n ",j);
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
//							System.out.printf("current index: %d, prev index %d\n", j,prevIndex);
							if(prevIndex>0){
								noteOff = (ShortMessage) cur.get(prevIndex).getMessage();
								int note2 = noteOff.getData1();
								//if the note is different add the prepositioning
								if(note2 != note1){
//									System.out.printf("Added prepos at %d for note at %d\n" , cur.get(j).getTick() - strings[i].difference(note1, note2) - preTime, cur.get(j).getTick());
//									System.out.printf("Note1: %d Note2: %d Difference: %d\n", note1, note2, strings[i].difference(note1, note2));
									try {
										if(cur.get(j).getTick() - strings[i].difference(note1, note2) - preTime < cur.get(prevIndex).getTick()){
											System.out.printf("Warning: note overlap detected, Dropping note");
											seq.getTracks()[0].add(cur.get(j));
											cur.remove(cur.get(j));
										}
										else{
											cur.add(new MidiEvent(new ShortMessage(NOTE_ON,0,noteOn.getData1(),1) , cur.get(j).getTick() - strings[i].difference(note1, note2) - preTime));
											j++;
										}
									} catch (ArrayIndexOutOfBoundsException e) {
										e.printStackTrace();
									} catch (InvalidMidiDataException e) {
										e.printStackTrace();
									}
//									
								}
							}
							else if(prevIndex == 0){
								try {
//									System.out.printf("Added prepos at %d for note at %d\n" , cur.get(j).getTick() - preTime, cur.get(j).getTick());
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
	 * Finds the next MidiEvent after a specific tick with a specific command.
	 * This method assumes there is one to find, and returns null if not.
	 * @param tr
	 * @param tick
	 * @param Command
	 * @return
	 */
	public static MidiEvent findEvent(Track tr, long tick, int Command){
		MidiEvent evnt = null;
		for(int i = 0; i < tr.size(); i++){
			MidiEvent cur = tr.get(i);
			if(cur.getTick()>tick){
				if(cur.getMessage() instanceof ShortMessage){
					ShortMessage msg = (ShortMessage) cur.getMessage();
					if(msg.getCommand() == Command){
						evnt = cur;
					}
				}
			}
		}
		return evnt;
	}
	
	
	public static List<Conflict> nextConflict(Sequence seq, long tick, MekString[] strings){
		MidiEvent droppedOn = findEvent(seq.getTracks()[0], tick, NOTE_ON);
		MidiEvent droppedOff = findEvent(seq.getTracks()[0], droppedOn.getTick(), NOTE_OFF);
		List<Conflict> conflicts = new ArrayList<Conflict>();
		Track dropTrack = seq.getTracks()[0];
		for(int i = 1; i < seq.getTracks().length; i++){
			MidiEvent playedOn = null;
			MidiEvent playedOff = null;
			for(int j = 0; j < seq.getTracks()[i].size(); j++ ){
				if(seq.getTracks()[i].get(j).getMessage() instanceof ShortMessage){
					ShortMessage shrt = (ShortMessage) seq.getTracks()[i].get(j).getMessage();
					if(shrt.getCommand() == NOTE_OFF){
						ShortMessage sh = (ShortMessage) droppedOn.getMessage();
						if(strings[i].conflicting(sh.getData1(), shrt.getData1(), seq.getTracks()[i].get(j).getTick() - droppedOn.getTick())){
							//there is a conflict, add it to list
							playedOff = seq.getTracks()[i].get(j);
							playedOn = seq.getTracks()[i].get(getPrev(j,seq.getTracks()[i],NOTE_ON));
							conflicts.add(new Conflict(droppedOn, droppedOff, playedOn, playedOff, i));
						}
					}
				}
			}
		}
		return conflicts;
	}
	
	
	/**
	 * delayNote should be used if a note conflict occurs and the user wishes to delay the note
	 * 
	 * @param seq: The sequence the note is in.
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
