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
				for(int j = cur.size()-1; j >= 0; --j){
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
			for(int j = cur.size()-1; j >= 0; --j){
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
	

	/**
	 * Gets the previous note off
	 */
	private static int getPrev(int index, Track cur){
		return getPrev(index, cur, NOTE_OFF);
	}
	
	/**
	 * Gets the previous note of a specified command
	 */
	private static int getPrev(int index, Track cur, int Command){
		for(int k = index-1; k >= 0; --k){
			MidiMessage midNoteOff = cur.get(k).getMessage();
			if(midNoteOff instanceof ShortMessage){
				ShortMessage noteOff = (ShortMessage) midNoteOff;
				if(noteOff.getCommand() == Command){
					return k;
				}
			}
		}
		return 0;
	}

	/**
	 * Adds pre positioning notes to the MIDI so MekBass can play it.
	 * 
	 * This assumes that track 0 exists as the dropped note area.
	 * It also makes the prepositioning notes on the same channel as the
	 * note it is prepositioning for.	
	 *  
	 * @param seq - The sequence to add prepositioning to.
	 * @param preTime - The time in ?¿½?¿½s to add before the string MUST be prepositioned.
	 * @param strings - The data for the strings it is to be played on.
	 * @param length - How long the prepositioning note should be.
	 * @return
	 */
	public static Sequence prePos(Sequence seq, long preTime, MekString[] strings, long length){
		//for each track
		for(int i = 1; i < seq.getTracks().length; i++){
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
//							System.out.printf("prev index %d\n", prevIndex);
							if(prevIndex>0){
								noteOff = (ShortMessage) cur.get(prevIndex).getMessage();
								int note2 = noteOff.getData1();
								//if the note is different and they don't clash
								if(note2 != note1 ){
									if( !strings[i-1].conflicting(note1, note2,  cur.get(j).getTick() - cur.get(prevIndex).getTick())){									
										try {
											if((cur.get(j).getTick() - strings[i-1].difference(note1, note2)) - preTime < cur.get(prevIndex).getTick()){
												System.out.printf("Warning: note overlap detected, %d previous note tick, %d prepos tick, Dropping note \n",cur.get(prevIndex).getTick(),(cur.get(j).getTick() - strings[i-1].difference(note1, note2)) - preTime);
												seq.getTracks()[0].add(cur.get(j));
												cur.remove(cur.get(j));
												if(cur.get(j).getMessage() instanceof ShortMessage){
													ShortMessage off = (ShortMessage) cur.get(j).getMessage();
													if(off.getCommand() == NOTE_OFF){
														seq.getTracks()[0].add(cur.get(j));
														cur.remove(cur.get(j));
													}
												}
											}
											else{
												//for some reason adding these on the same line ends up with the wrong note off tim
												long time = cur.get(j).getTick();
												time -= strings[i-1].difference(note1, note2);
												time -= preTime;
//												System.out.printf("Added Prepos for %d at: Note On %d note Off %d\n", note2, time, time + length);
												cur.add(new MidiEvent(new ShortMessage(NOTE_ON,noteOn.getChannel(),noteOn.getData1(),1) , time));
												cur.add(new MidiEvent(new ShortMessage(NOTE_OFF,noteOn.getChannel(),noteOn.getData1(),0) , time +  length));
												j++;
											}
										} catch (ArrayIndexOutOfBoundsException e) {
											System.out.println(e + "\n");
											e.printStackTrace();
										} catch (InvalidMidiDataException e) {
											System.out.println(e + "\n");
											e.printStackTrace();
										}
//									System.out.printf("Added prepos for");
									} else {
										System.out.println("Conflict found.");
									}
								}
							}
							else if(prevIndex == 0){
								try {
									cur.add(new MidiEvent(new ShortMessage(NOTE_ON,0,noteOn.getData1(),1) , 0));
									cur.add(new MidiEvent(new ShortMessage(NOTE_OFF,0,noteOn.getData1(),0) , length));
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
	 * Finds the index of the MidiEvent before a specific tick with a specific command.
	 * This method assumes there is one to find, and returns 0 if not.
	 * @param tr
	 * @param tick
	 * @param Command
	 * @return
	 */
	public static int findEvent(Track tr, long tick, int Command){
		int curIndex = -1;
		for(int i = 0; i < tr.size(); i++){
			MidiEvent cur = tr.get(i);
			if(cur.getMessage() instanceof ShortMessage){
				ShortMessage msg = (ShortMessage) cur.getMessage();
				if(msg.getCommand() == Command){
					if(cur.getTick() < tick) curIndex = i;
				}
			}
		}
		return curIndex;
	}
	
	
	public static List<Conflict> getConflicts(Sequence seq, MekString[] strings){
		Track dropTrack = seq.getTracks()[0];
		List<Conflict> conflicts = new ArrayList<Conflict>();
		//For each note in the dropped note track
		for(int i = 0; i < dropTrack.size(); i++){
			MidiEvent current = dropTrack.get(i);
			if(current.getMessage() instanceof ShortMessage){
				ShortMessage note = (ShortMessage) current.getMessage();
				if(note.getCommand() == NOTE_ON){
					Conflict con = new Conflict(current);
//					System.out.printf("DropTrack event %d conflict building\n", i);
					//if there is a note on add it and all related events to a list
					ArrayList<MidiEvent> dropped = new ArrayList<MidiEvent>();
					dropped.add(current);
					addEvents:
					for(int j = i+1; j < dropTrack.size(); j++){
						//add every event on the same channel that relates to the first note
						MidiEvent next = dropTrack.get(j);
						if(next.getMessage() instanceof ShortMessage){
							ShortMessage nextNote = (ShortMessage) next.getMessage();
							if(nextNote.getData1() == note.getData1() && nextNote.getChannel() == note.getChannel()){
								dropped.add(next);
//								System.out.printf("Adding event %d to conflict\n", j);
								if(nextNote.getCommand() == NOTE_OFF){
//									System.out.printf("NoteOff added\n");
									break addEvents;
								}
							}
						}
					}
					for(int k = 1; k < seq.getTracks().length; k++){
//						System.out.printf("Looking for conflicts on string %d\n",k-1);
						Track track = seq.getTracks()[k];
						//look for conficts on each string that can play the note.
						if(strings[k-1].playable(note.getData1())){
//							System.out.printf("Playable on string %d\n", k-1);
							//this adds all related events of the note before the conflicting note to a list
							int conflictIndex = findEvent(track, current.getTick(), NOTE_ON);
//							System.out.printf("Conflicting with event number %d\n",conflictIndex);
							if(conflictIndex >= 0){
								ShortMessage conflict1 = (ShortMessage) track.get(conflictIndex).getMessage();
								List<MidiEvent> play1 = new ArrayList<MidiEvent>();
								addConf1:
								for(int j = conflictIndex; j < seq.getTracks()[k].size(); j++){
									MidiEvent next = track.get(j);
									if(next.getMessage() instanceof ShortMessage){
										ShortMessage nextMessage = (ShortMessage) next.getMessage();
										if(nextMessage.getData1() == conflict1.getData1() && nextMessage.getChannel() == conflict1.getChannel()){
											play1.add(next);
//											System.out.printf("Added event %d on to conflict\n",j);
											if(nextMessage.getCommand() == NOTE_OFF){
												conflictIndex = j;
//												System.out.printf("First note of conflict complete\n");
												break addConf1;
											}
										}
									}
								}
								List<MidiEvent> play2 = new ArrayList<MidiEvent>();
								addConf2:
								for(int j = conflictIndex; j < seq.getTracks()[k].size(); j++){
//									System.out.printf("Started last note of conflict");
									MidiEvent next = track.get(j);
									if(next.getMessage() instanceof ShortMessage){
										ShortMessage nextMessage = (ShortMessage) next.getMessage();
										if(nextMessage.getData1() == conflict1.getData1() && nextMessage.getChannel() == conflict1.getChannel()){
											play2.add(next);
											if(nextMessage.getCommand() == NOTE_OFF){
												break addConf2;
											}
										}
									}
								}
								
								//add the note notes to the conflict
								NoteConflict conflict = new NoteConflict(dropped, play1, play2, track, dropTrack, k-1);
//								System.out.print(conflict.toString());
								con.addConf(conflict, k-1);
//								System.out.print(con.toString());
							}
						}
					}
					conflicts.add(con);
				}
			}
		}
		return conflicts;
	}
}