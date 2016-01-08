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
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.ShortMessage.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.canvas.*;


import solver.MekString;

/**
 * This is a Combination Simulation/Visualisation for the MekBass and Midi sequence files.<br>
 * It switches from Visualisation to Simulation when the number of tracks in the current sequence is equal to the number of strings.
 * @author Elliot Wilde
 *
 */
public class Simulation {

	private static int note_tag_width = 2;
	private static int note_tag_height = 8;
	private static int note_len_height = 2;

	private Sequence seq=null;
	private List<Note>[] notes = null;
	private int resolution;
	private long drawStartTime;

	private long lastTickTime=0;

	private MekString[] strings = null;
	private double[] picks = null;
	private int[] pickTarget = null;

	private boolean playing = false;

	/**
	 * Creates a new Simulation object. This doesn't actually do anything
	 */
	public Simulation(){
	}

	/**
	 * Is the simulation currently set to be playing?
	 * @return
	 */
	public boolean isPlaying(){
		return playing;
	}

	/**
	 * Set the simulation to use a specific set of strings
	 * @param newStrings New Strings
	 */
	public void setStrings(MekString[] newStrings){
		if (newStrings==null) return;
		strings = newStrings;
		picks = new double[strings.length];
		pickTarget = new int[strings.length];
		// we cheat here and just set the pick position to the first note on each string (or the lowest note on the string)
		for (int i=0; i<Math.min(notes.length, strings.length); ++i){
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
						if (shrt.getData2()>0){ // if the note is actually a note off, skip the note on stuff
							progress.put(shrt.getData1(), i);
							break;
						}
					case NOTE_OFF: // if the note is a notes off, get the note on, make a Note, and add it the the notes list
						int last = shrt.getData1();
						Integer prog = progress.get(last);
						MidiEvent on = null;
						if(prog != null){
							on = tr.get(prog);
						}

						if (on == null){
							break;
						}
						Note n = new Note(shrt.getData1(), // the maths is to convert MIDI ticks to ms
										  (long) (on.getTick() * (60000. / (bpm * resolution))),
										  (long) (tr.get(i).getTick()* (60000. / (bpm * resolution))),
										  on.getMessage().getMessage()[2]);
						notes[j].add(n);
						break;

						default:
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
			if (notes.length==strings.length+1){
			notes = Arrays.copyOfRange(notes, 1, notes.length);
			}
			setStrings(strings);
		}
	}

	/**
	 * Sets the Simulation to begin playing, should normally be called in conjunction with Sequence.play
	 */
	public void play(){
		playing = true;
	}

	/**
	 * Halts simulation playback at the current time, should normally be called in conjunction with Sequence.pause
	 */
	public void pause(){
		playing = false;
		lastTickTime = 0;
	}

	/**
	 * Stops simulation playback resetting the time to zero, should normally be called in conjunction with Sequence.stop
	 */
	public void stop(){
		playing = false;
		drawStartTime = 0;
		lastTickTime = 0;
		setStrings(strings);
	}

	/**
	 * Does on simulation tick, with the tick time being the time since the last tick.<br>
	 * The simulation tick moves the picks between frets based upon timing information present in the strings.
	 */
	public void tick(){
		long time = System.currentTimeMillis();
		long diff = time-lastTickTime;
		if (lastTickTime != 0) {
			tick(diff);
			drawStartTime+=diff;
		}
		lastTickTime = time;
	}

	/**
	 * This is the actual simulation tick
	 * @param time
	 * @return
	 */
	private long tick(long time){
		if (picks==null) return time;
		// move the picks
		for (int i=0; i<picks.length; ++i){
			// if the pick has no target, go to the next string
			if (pickTarget[i] == -1) {
				int c = notes.length - strings.length;
				Note n;
				try{ // this was the cleanest way of checking whether or not there has been/will be a note
					n = notes[i+c].get(getIndexAtTime(drawStartTime, notes[i+c]));
				} catch (IndexOutOfBoundsException e){
					continue;
				}
				if (n.velocity==1){ // this means it is a preposition
					pickTarget[i] = n.note;
				} else {
					continue;
				}
			}
			// get the target
			int target = pickTarget[i];
			// get the distance
			double distance =  target - picks[i];
			// get the direction
			int dir = Long.signum(new Float(distance).longValue());
			distance = Math.abs(distance);

			// have float(pick), int(dest), int(target)
			// get the velocity (time between frets)
			int a = (int) Math.round(picks[i]), b = (int) (Math.round(picks[i])+dir);
			// get how far(time) we need to move
			long delta = strings[i].differenceTime(a, b);
			// get how quickly we  can move
			double move = 1./(delta/(float)time);
			if (Double.isInfinite(move)) { // if one of the values is zero, stop
				picks[i] = pickTarget[i];
				pickTarget[i] = -1;
				continue;
			}
			// use that to move the picks
			picks[i] = picks[i]+move*dir;
			//check if it has moved too far, then set the target to -1
			if (Math.signum(picks[i]-target) == Integer.signum(dir)){
				picks[i] = pickTarget[i];
				pickTarget[i] = -1;
			}
		} // we done here
		return time;
	}

	/**
	 * Draws the currently stored sequence, the currently playable note, and the strings.
	 * @param gc The graphics panel to draw onto
	 * @param hscale Horizonatal scaling factor
	 */
	public void draw(GraphicsContext gc, double hscale){
		// get the dimensions of the canvas
		double top = 15.0;
		double left = 15.0;
		double width = gc.getCanvas().getWidth();
		double height= gc.getCanvas().getHeight()-top;
		// clear the draw area
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		// draw a vertical line on the right to seperate the pane from the settings
		gc.setFill(Color.GREY);
		gc.fillRect(width-1, 1, width, height+top);
		// change the font size to fit better
		gc.setFont(Font.font(gc.getFont().getFamily(), 9));

		////////////////////////////////////////
		// Draw timing marks
		////////////////////////////////////////
		long increment= 0;
		gc.setFill(Color.GRAY);
		do {
			// this is the increment in ms for the display
			increment+=200;
			double pos = increment;
			if ((pos-drawStartTime)*hscale < width && (pos-drawStartTime)*hscale > left-100){
				// the lines on the bottom
				gc.fillRect(left + (pos-drawStartTime)*hscale, height+top - 2, 2, height);
				// the numbers above them
				gc.fillText(String.format("%#.2f", increment/1000.0),
						left+(pos-drawStartTime-10)*hscale, height-10+top-(increment%100)/20);
			}
		} while ((increment-drawStartTime)*hscale < width);
		// then, if we don't have a sequence to display, we don't have anything to display
		if (seq==null) return;

		// if for some reason we don't have any strings loaded, revert to a fallback display (else)
		// if we do have strings loaded, draw normally
		if (strings.length >= notes.length){
			// get information from strings
			int totalnotes = 0;
			for (int i=0; i<strings.length; ++i){
				totalnotes += strings[i].noteRange +1;
			}
			int offsetNotes = 0;
			// set the height allocation for each note
			final double noteDiv = height / totalnotes;
			double offset = noteDiv;


			////////////////////////////////////////
			// Draw the notes
			////////////////////////////////////////
			Note n;
			for (int t=0; t<Math.min(notes.length,strings.length); ++t){

				int length = notes[t].size();
	//			get the first (fully) visible note
				int startIndex = 0;
				// and start at that point
				// set how far down we start
				if (t>0) offset += noteDiv*(strings[t].noteRange+1);
				for (int i=startIndex; i<length; ++i){
					n = notes[t].get(i);
					// cache some of the maths done more than once
					double start = (n.start-drawStartTime)*hscale;
					double end = (n.end-drawStartTime)*hscale;
					// if the note would note be displayed, don't do anything else, could probably break the loop here, but meh
					if (end < left || start > width) continue;
					int lowNote = strings[t].lowNote;
					double voff = offset+(n.note-lowNote)*noteDiv;	// vertical offset
					double noff = (noteDiv/2+note_tag_height/2);	// note offset

					// draw stuff
					// the length of the note
					gc.setFill(Color.BLACK);
					gc.fillRect(left + start, voff-noteDiv/2 ,n.duration*hscale, note_tag_width);
					// the start tag for the note
					gc.setFill(Color.GREEN);
					gc.fillRect(left + start, voff-noff, note_tag_width, note_tag_height);
					// the end tag for the note
					gc.setFill(Color.RED);
					gc.fillRect(left + end-note_tag_width, voff-noff, note_tag_width, note_tag_height);
					// the string, and note being played
					gc.fillText(String.format("%d:%d:%d",t, n.note, n.velocity), left+start, voff-noteDiv);
				}
			}


			////////////////////////////////////////
			// Draw the note values on the left
			////////////////////////////////////////
			// clear a box on the left
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


		} else { /** endif (strings.length >= notes.length) **/

			// get information from strings
			int lowNote = Integer.MAX_VALUE;
			int highNote = Integer.MIN_VALUE;
			for (List<Note> l: notes){
				for (Note n: l){
					lowNote = Math.min(lowNote, n.note);
					highNote = Math.max(highNote, n.note);
				}
			}
			int totalnotes = highNote - lowNote;
			// set the height allocation for each note
			final double noteDiv = height / totalnotes;
			final double ntop = - (lowNote * noteDiv);

			////////////////////////////////////////
			// Draw the notes
			////////////////////////////////////////
			Note n;
			for (int t=0; t<strings.length; ++t){

				int length = notes[t].size();
				//			get the first (fully) visible note
				int startIndex = 0;
				// and start at that point
				// set how far down we start
				for (int i=startIndex; i<length; ++i){
					n = notes[t].get(i);
					// cache some of the maths done more than once
					double start = (n.start-drawStartTime)*hscale;
					double end = (n.end-drawStartTime)*hscale;
					// if the note would note be displayed, don't do anything else, could probably break the loop here, but meh
					if (end < left || start > width) continue;
					double voff = n.note*noteDiv + ntop;	// vertical offset
					double noff = (noteDiv/2+note_tag_height/2);	// note offset

					// draw stuff
					// the length of the note
					gc.setFill(Color.BLACK);
					gc.fillRect(left + start, voff-noteDiv/2 ,n.duration*hscale, note_tag_width);
					// the start tag for the note
					gc.setFill(Color.GREEN);
					gc.fillRect(left + start, voff-noff, note_tag_width, note_tag_height);
					// the end tag for the note
					gc.setFill(Color.RED);
					gc.fillRect(left + end-note_tag_width, voff-noff, note_tag_width, note_tag_height);
					// the string, and note being played
					gc.fillText(String.format("%d:%d:%d",t, n.note, n.velocity), left+start, voff-noteDiv);
				}
			}

			////////////////////////////////////////
			// Draw the note values on the left
			////////////////////////////////////////
			// clear a box on the left
			gc.clearRect(0, 0, left, height);
			gc.setFill(Color.GRAY);
			gc.fillRect(left, 0, 1, height+top);
			// go through each string
			for (int i=lowNote; i<=highNote; ++i){
				gc.fillText(String.format("%d", i), 1, (noteDiv)* i + ntop);
			}
		}
	}

	/**
	 * Gets the index of the note at or the next note after the specified time, or track.size
	 * @param time
	 * @param track
	 * @return Index of the last note to start playing
	 */
	private int getIndexAtTime(long time, List<Note> track){
		int ind = Arrays.binarySearch(track.toArray(), new Note(0, time, 0, 0));
		if (ind < 0) ind =  -ind-1;
		// check the note at the index to see if 'time' fits in, if not, use the index before
//		Note n = track.get(ind);
		if (time < track.get(ind).start){
				ind--;
		}
		return ind;
	}

	/**
	 * Tuple representing a note from a midi sequence.<br><br>
	 * Stores:<br>
	 * The note (in range 0-255)<br>
	 * The start and end times (in ms)<br>
	 * The duration of the note (end-start)<br>
	 * The Velocity of the note<br><br>
	 *
	 * The note is also comparable with other notes on their start time
	 *
	 * @author Elliot Wilde
	 *
	 */
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
}
