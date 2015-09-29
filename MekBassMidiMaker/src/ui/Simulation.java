package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.ShortMessage.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;

import org.junit.Test;

public class Simulation {

	private Sequence seq=null;
	List<Note>[] notes = null;
	long position;
	long drawStartTime;

	public Simulation(){
	}

	public void setSequence(Sequence sequence){
		if (sequence == null) return;
		this.seq = sequence;
		this.position = 0;
		this.drawStartTime = 0;
		// for each track, convert note_on and note_off to Note(3)

		Track[] tracks = seq.getTracks();
		notes = new List[tracks.length];

		// for each track
		for (int j=0; j<tracks.length; ++j){
			// get each note_on, pair it with a note_off, then create a Note, and add it to notes
			Map<Integer, Integer> progress = new HashMap<Integer, Integer>();
			notes[j] = new ArrayList<Note>();
			Track tr = tracks[j];
			for (int i=0; i<tr.size(); ++i){
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
						Note n = new Note(shrt.getData1(), on.getTick() , tr.get(i).getTick());
						notes[j].add(n);
						break;

						default:
//							System.out.println("other note");
					}
				}
			}
		}
	}

	// something something threads
	public void play(){

	}

	public void pause(){

	}

	public void stop(){

	}

	public long addDrawStartTime(long add){
		return drawStartTime+=add;
	}

	public long addTime(long add){
		return position+=add;
	}

	public long setTime(long set){
		return position=set;
	}

	public void draw(GraphicsContext gc, double hscale){
		// get the dimensions of the canvas
		double width = gc.getCanvas().getWidth();
		double height= gc.getCanvas().getHeight();
		long drawEnd = drawStartTime + (long) (width * hscale); // need to calculate this (width and hscale?)
		// clear the draw area
		gc.clearRect(0, 0, width, height);
		// put timing marks on the bottom
//		System.out.println(1.0/usPerTick);
//		System.out.println("[" + (double)seq.getTickLength()/seq.getMicrosecondLength() + "]");

		// then, if we don't have a sequence to display, we don't have anything to display
		if (seq==null) return;

		// correction to make the size of seqments timinig independent
		double usPerTick = (double)seq.getMicrosecondLength()/seq.getTickLength();
		hscale /= 2.0/usPerTick*1000.0; // 2.0 is magic

		long u= 0;



		gc.setFill(Color.GRAY);
		do {
			u+=50;
			double pos = u/usPerTick*1000.0;
//			System.out.println(pos);
			if ((pos-drawStartTime)*hscale < width && (pos-drawStartTime)*hscale > 0-100){
				gc.fillRect((pos-drawStartTime)*hscale, height - 2, 2, height);
				gc.fillText(String.format("%#.2f", u/1000.0),
						(pos-drawStartTime-10)*hscale, height-10-(u%100)/20);
			}
		} while (u/usPerTick < width);

//		System.out.println((u/usPerTick*1000.0)*hscale);


		Note n;
		for (int t=0; t<notes.length; ++t){
			int length = notes[t].size();
//			 get the first (fully) visible note
//			int startIndex = getIndexAtTime(drawStartTime, notes[t]);
			int startIndex = 0;
//			if (startIndex < 0) startIndex = -(startIndex+1);
			// and start at that point
			for (int i=startIndex; i<length; ++i){
				n = notes[t].get(i);

				double start = (n.start-drawStartTime)*hscale;
				double end = (n.end-drawStartTime)*hscale;

//				if ((start >= 0 && (
//						end <= width || start <= width)) ||
//						(start <= 0 && end >= 0)){

					gc.setFill(Color.GREEN);
					gc.fillRect(start, 8*n.note, 2, 8);
					gc.setFill(Color.RED);
					gc.fillRect(end-2, 8*n.note, 2, 8);
					gc.setFill(Color.color(1.0/(t+1), 1.0-1.0/(t+1), 0.5));
					gc.fillRect(start, 8*n.note + t*8.0/(double)notes.length ,n.duration*hscale, 2);
//				}
			}

		}
	}

	/**
	 * get the note before?
	 * or get the note after?
	 * @param time
	 * @param track
	 * @return
	 */
	private int getIndexAtTime(long time, List<Note> track){
		return Arrays.binarySearch(track.toArray(), new Note(0, time, 0));
	}

	private class Note implements Comparable<Note>{
		final int note;
		final long start;
		final long end;
		final long duration;

		private Note(int note, long start, long end){
			this.note = note;
			this.start = start;
			this.end = end;
			this.duration = end-start;
		}

		@Override
		public int compareTo(Note other) {
			return Long.compare(this.start, other.start);
		}
	}

//	@Test
//	public void getIndexTest(){
//		List<Note> notes = new ArrayList<Note>();
//		notes.add(new Note(1, 10, 10));
//		notes.add(new Note(1, 20, 20));
//		notes.add(new Note(1, 30, 10));
//		notes.add(new Note(1, 60, 10));
//
//		for (int i=1; i<70; i+=10){
//			System.out.println(i + " - " + getIndexAtTime(i, notes));
//		}
//
//	}


}

//


/**
javafx.scene.canvas.Canvas


Canvas is an image that can be drawn on using a set of graphics commands provided by a GraphicsContext.
A Canvas node is constructed with a width and height that specifies the size of the image into which the
 canvas drawing commands are rendered. All drawing operations are clipped to the bounds of that image.
Example:

import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;

Group root = new Group();
Scene s = new Scene(root, 300, 300, Color.BLACK);

final Canvas canvas = new Canvas(250,250);
GraphicsContext gc = canvas.getGraphicsContext2D();

gc.setFill(Color.BLUE);
gc.fillRect(75,75,100,100);

root.getChildren().add(canvas);

Since:
	 JavaFX 2.2
**/