package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.ShortMessage.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.canvas.*;

import org.junit.Test;

import solver.GreedySolver;
import solver.MekString;

public class Simulation {

	private static int note_tag_width = 2;
	private static int note_tag_height = 8;
	private static int note_len_height = 2;

	private Sequence seq=null;
	List<Note>[] notes = null;
	int resolution;
	long position;
	long drawStartTime;

	long lastTickTime=0;

	private MekString[] strings = null;
	private double[] picks = null;
	private int[] pickTarget = null;

	private boolean playing = false;

	public Simulation(){
	}

	public boolean isPlaying(){
		return playing;
	}

	public void setStrings(MekString[] newStrings){
		if (newStrings==null) return;
		strings = newStrings;
//		notes = Arrays.copyOfRange(notes, 1, notes.length);
		picks = new double[strings.length];
		pickTarget = new int[strings.length];
		// we cheat here and just set the pick position to the first note on each string (or the lowest note on the string)
		for (int i=0; i<Math.min(notes.length, strings.length); ++i){
//			System.out.println(notes[i].size());
			if (notes[i].isEmpty()){
				picks[i] = strings[i].lowNote;
			} else {
				picks[i] = notes[i].get(0).note;
			}
			pickTarget[i] = -1;
		}
	}

	/**
	 *
	 * @param sequence
	 */
	@SuppressWarnings("unchecked")
	public void setSequence(Sequence sequence){
		if (sequence == null) return;
		this.resolution = sequence.getResolution();
		int bpm = 0;
		this.seq = sequence;
//		this.position = 0;
//		this.drawStartTime = 0;
		// for each track, convert note_on and note_off to Note(3)

		Track[] tracks = seq.getTracks();
		notes = new List[tracks.length];


		// for each track
		for (int j=0; j<tracks.length; ++j){
			// get each note_on, pair it with a note_off, then create a Note, and add it to notes
			Map<Integer, Integer> progress = new HashMap<Integer, Integer>();
			notes[j] = new ArrayList<Note>();
			Track tr = tracks[j];
			for (int i=0; i<tr.size() ; ++i){
				MidiMessage mid = tr.get(i).getMessage();
				if (mid instanceof ShortMessage){
					ShortMessage shrt = (ShortMessage) mid;
					switch (shrt.getCommand()){
					case NOTE_ON: // if the note is a note on, store its index, so we can get it back
						progress.put(shrt.getData1(), i);
						break;
					case NOTE_OFF: // if the note is a notes off, get the note on, make a Note, and add it the the notes list
						MidiEvent on = tr.get(progress.get(shrt.getData1()));
						if (on == null){
//							System.out.println("Note: " + shrt.getData1() + ", No corresponding note on");
							break;
						}
						Note n = new Note(shrt.getData1(), // the maths is to convert MIDI ticks to ms
										  (long) (on.getTick() * (60000. / (bpm * resolution))),
										  (long) (tr.get(i).getTick()* (60000. / (bpm * resolution))),
										  on.getMessage().getMessage()[2]);
//						System.out.print(on.getMessage().getMessage()[2] + "\t");
//						System.out.println(shrt.getData2());
//						System.out.println(n.velocity);
						notes[j].add(n);
						break;

						default:
//							System.out.println("other note");
					}
				} else if (mid instanceof MetaMessage){
					// get the bpm changes
					MetaMessage met = (MetaMessage) mid;
					if (met.getType() == 0x51){
						byte[] data = met.getData();
						bpm =  60000000 / ((data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff));
					}
				}
			}
		}

		strings = Slave.getMekStringArray();
		if (strings != null) {
//			notes = Arrays.copyOfRange(notes, 1, notes.length);
			setStrings(strings);
		}
	}

	// something something threads
	public void play(){
		playing = true;
//		lastTickTime = System.currentTimeMillis();
	}

	public void pause(){
		playing = false;
		lastTickTime = 0;
	}

	public void stop(){
		playing = false;
		position = 0;
		drawStartTime = 0;
		lastTickTime = 0;
		setStrings(strings);
	}

	public long addDrawStartTime(long add){
		return drawStartTime+=add;
	}

//	public long addTime(long add){
//		return tick(position+=add);
//	}

	public long setTime(long set){
		return position=set;
	}

	public void tick(){
		long time = System.currentTimeMillis();
		long diff = time-lastTickTime;
		if (lastTickTime != 0) {
//			addTime(diff);
			tick(diff);
			addDrawStartTime(diff);
		}
		lastTickTime = time;
	}

	public long tick(long time){
		if (picks==null) return time;
		// move the picks
		for (int i=0; i<picks.length; ++i){
			// if the pick has no target, go to the next string
			//ystem.out.printf("%.1f\tt:%d\t", picks[i], pickTarget[i]);
			if (pickTarget[i] == -1) {
				int c = notes.length - strings.length;
				Note n;
				try{
					n = notes[i+c].get(getIndexAtTime(drawStartTime, notes[i+c]));
				} catch (IndexOutOfBoundsException e){
					//ystem.out.println("ex");
					continue;
				}
				if (n.velocity==1){ // this means it is a preposition
					pickTarget[i] = n.note;
				} else {
					//ystem.out.println("nan");
					continue;
				}
			}
			// get the target
			int target = pickTarget[i];
			//ystem.out.printf("t:%d\t", pickTarget[i]);
			// get the distance
			double distance =  target - picks[i];
			// get the direction
			int dir = Long.signum(new Float(distance).longValue());
			distance = Math.abs(distance);

			//ystem.out.printf("loc:%.2f\ttar:%d\tdist:%.1f\tdir:%d\t", picks[i], target, distance, dir);
			// have float(pick), int(dest), int(target)
			// get the velocity (time between frets)
			int a = (int) Math.round(picks[i]), b = (int) (Math.round(picks[i])+dir);
			//ystem.out.printf("a:%d\tb:%d\t%d\t", a, b, strings[i].lowNote);
//			long delta = strings[i].interval[Math.min(a,b)-strings[i].lowNote];// +
//					strings[i].interval[Math.max(a,b)-strings[i].lowNote];

			long delta = strings[i].differenceTime(Math.min(a,b), Math.max(a, b));
			//ystem.out.printf("delta:%d\t", delta);
			double move = 1./(delta/(float)time);
			//ystem.out.printf("mov:%f\t", move);
			//ystem.out.println("\ttime:[" + time + "]");
			if (Double.isInfinite(move)) {
				picks[i] = pickTarget[i];
				pickTarget[i] = -1;
				continue;
			}
			// use that to move the picks
			picks[i] = picks[i]+move*dir;
			//check if it has moved too far, then set the target to -1
			//ystem.out.println(picks[i]);
			if (Math.signum(picks[i]-target) == Integer.signum(dir)){
				//ystem.out.println("stahp");
				picks[i] = pickTarget[i];
				pickTarget[i] = -1;
			}

		}
		//ystem.out.println();


		return time;
	}

	public void draw(GraphicsContext gc, double hscale){
//		long time = System.currentTimeMillis();
		// get the dimensions of the canvas
		double top = 15.0;
		double left = 15.0;
		double width = gc.getCanvas().getWidth();
		double height= gc.getCanvas().getHeight()-top;
		long drawEnd = drawStartTime + (long) (width * hscale); // need to calculate this (width and hscale?)
		// clear the draw area
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		// draw a vertical line on the right to seperate the pane from the settings
		gc.setFill(Color.GREY);
		gc.fillRect(width-1, 1, width, height+top);
		gc.setFont(Font.font(gc.getFont().getFamily(), 9));;

		////////////////////////////////////////
		// Draw timing marks
		////////////////////////////////////////
		long u= 0;
		gc.setFill(Color.GRAY);
		do {
			// this is the increment in ms for the display
			u+=200;
			double pos = u;
			if ((pos-drawStartTime)*hscale < width && (pos-drawStartTime)*hscale > left-100){
				gc.fillRect(left + (pos-drawStartTime)*hscale, height+top - 2, 2, height);
				gc.fillText(String.format("%#.2f", u/1000.0),
						left+(pos-drawStartTime-10)*hscale, height-10+top-(u%100)/20);
			}
		} while ((u-drawStartTime)*hscale < width);
		// then, if we don't have a sequence to display, we don't have anything to display
		if (seq==null) return;
		strings = Slave.getMekStringArray();

		if (strings !=null){
			// get information from strings
			int totalnotes = 0;
			for (int i=0; i<strings.length; ++i){
				totalnotes += strings[i].noteRange +1;
			}
			int offsetNotes = 0;
			double noteDiv = height / totalnotes;
			double offset = noteDiv;


			////////////////////////////////////////
			// Draw the notes
			////////////////////////////////////////
			Note n;
			int loopEnd = Math.min(notes.length, strings.length);
			int c = 0; // this is a correction for if track0 has not yet been removed
			if (notes.length == strings.length+1){
				c = 1;
			}
			for (int t=0; t<loopEnd; ++t){

				int length = notes[t+c].size();
	//			 get the first (fully) visible note
				int startIndex = 0;
				// and start at that point
				// set how far down we start
				if (t>0) offset += noteDiv*(strings[t-1].noteRange+1);
				for (int i=startIndex; i<length; ++i){
					n = notes[t+c].get(i);

					double start = (n.start-drawStartTime)*hscale;
					double end = (n.end-drawStartTime)*hscale;
//					System.out.println(start + "\t" + end);
					int lowNote = strings[t].lowNote;

					if (n.velocity==1) {
						gc.setFill(Color.BLANCHEDALMOND);
					} else {
						gc.setFill(Color.BLACK);
					}
					gc.setFill(Color.grayRgb(n.velocity));
					gc.fillRect(left + start, offset+(n.note-lowNote)*noteDiv-noteDiv/2 ,n.duration*hscale, note_tag_width);
					gc.setFill(Color.GREEN);
					gc.fillRect(left + start, offset+(n.note-lowNote)*noteDiv-(noteDiv/2+note_tag_height/2), note_tag_width, note_tag_height);
					gc.setFill(Color.RED);
					gc.fillRect(left + end-note_tag_width, offset+(n.note-lowNote)*noteDiv-(noteDiv/2+note_tag_height/2), note_tag_width, note_tag_height);
					gc.fillText(String.format("%d:%d",t, n.note), left+start, offset+(n.note-lowNote)*noteDiv-noteDiv);
				}

			}
			////////////////////////////////////////
			// Draw the note values on the left
			////////////////////////////////////////
			gc.clearRect(0, 0, left, height);
			gc.setFill(Color.GRAY);
			gc.fillRect(left, 0, 1, height+top);
			offset = noteDiv;
			// go through each string
			for (int i=0; i<strings.length; ++i){
				// and draw the top playable note, and the bottom playable note
				for (int s=strings[i].lowNote, j=0; s<=strings[i].highNote; ++s, ++j){
					// s is the note value, j is just a loop counter
					gc.fillText(String.format("%d", s), 1, offset + (noteDiv)*j);
				}
				offsetNotes += strings[i].noteRange+1;
				offset = ((double)offsetNotes+1)*(noteDiv);
				// draw a divider between the strings
				if (offsetNotes < totalnotes) gc.fillRect(0, offset+1-noteDiv, width, 1);
			}

			//////////////////////////////////////////
			// Draw the picks
			//////////////////////////////////////////
			offset = noteDiv;
			offsetNotes = 0;
			if (picks!=null){
				for (int i=0; i<strings.length; ++i){
					gc.setFill(Color.TURQUOISE.darker());
					gc.fillRect(left, offset - noteDiv/2 + (noteDiv) * (picks[i]-strings[i].lowNote), 4, 4);



					offsetNotes += strings[i].noteRange+1;
					offset = ((double)offsetNotes+1)*(noteDiv);

				}
			}


		} else {

		// archive for visualisation
			Note n;
			for (int t=0; t<notes.length; ++t){
				int length = notes[t].size();
	//			 get the first (fully) visible note
				int startIndex = 0;
				// and start at that point
				for (int i=startIndex; i<length; ++i){
					n = notes[t].get(i);

					double start = (n.start-drawStartTime)*hscale;
					double end = (n.end-drawStartTime)*hscale;

					gc.setFill(Color.color(1.0/(t+1), 1.0-1.0/(t+1), 0.5));
					gc.fillRect(left + start, note_tag_height*n.note + t*note_tag_height/(double)notes.length ,n.duration*hscale, note_tag_width);
					gc.setFill(Color.GREEN);
					gc.fillRect(left + start, note_tag_height*n.note, note_tag_width, note_tag_height);
					gc.setFill(Color.RED);
					gc.fillRect(left + end-2, note_tag_height*n.note, note_tag_width, note_tag_height);
	//				}
				}

			}
		}
	}

	/**
	 * Gets the index of the note at or the next note after the specified time, or track.size
	 * @param time
	 * @param track
	 * @return
	 */
	private int getIndexAtTime(long time, List<Note> track){
		int ind = Arrays.binarySearch(track.toArray(), new Note(0, time, 0, 0));
		if (ind < 0) ind =  -ind-1;
		// check the note at the index to see if 'time' fits in, if not, get the index before and check that
		Note n = track.get(ind);
		if (time < n.start){
			Note n2 = track.get(ind-1);
			if (time >= n2.start && time < n2.end){
				ind--;
			}
		}
		return ind;
	}

	private class Note implements Comparable<Note>{
		final int note;
		final long start;
		final long end;
		final long duration;
		final int velocity;

		private Note(int note, long start, long end, int velocity){
			this.note = note;
			this.start = start;
			this.end = end;
			this.duration = end-start;
			this.velocity = velocity;
		}

		@Override
		public int compareTo(Note other) {
			return Long.compare(this.start, other.start);
		}
	}

//	@Test
//	public void getIndexTest(){
//		List<Note> notes = new ArrayList<Note>();
//		notes.add(new Note(1, 10, 10, 0));
//		notes.add(new Note(2, 20, 20, 0));
//		notes.add(new Note(3, 30, 10, 0));
//		notes.add(new Note(6, 60, 10, 0));
//
//		for (int i=0; i<70; i+=10){
//			System.out.print(i + "\t" + getIndexAtTime(i, notes) + "\t");
//			if (getIndexAtTime(i, notes) < 0){
//				System.out.println(notes.get((-getIndexAtTime(i, notes))-1).start);
//			} else {
//				System.out.println(notes.get(getIndexAtTime(i, notes)).start);
//			}
//			System.out.print(i+1 + "\t" + getIndexAtTime(i+1, notes) + "\t");
//			if (getIndexAtTime(i+1, notes) < 0){
//				System.out.println(notes.get((-getIndexAtTime(i+1, notes))-1).start);
//			} else {
//				System.out.println(notes.get(getIndexAtTime(i+1, notes)).start);
//			}
//		}
//
//	}


}
