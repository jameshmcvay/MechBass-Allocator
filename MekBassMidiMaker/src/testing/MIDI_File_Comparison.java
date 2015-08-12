package testing;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import main.Parser3;

import org.junit.Test;

import solver.Solver;
import solver.TrackSplitter;

public class MIDI_File_Comparison {

	// This is the file I'm inspecting.
	private final File MIDIFILE_TOTEST = new File("./resources/hysteria.mid");
	// This parser will have the Solver applied to its sequence.
	private Parser3 test_parser = new Parser3(MIDIFILE_TOTEST);
	// This parser will remain unmolested.
	private Parser3 parser = new Parser3(MIDIFILE_TOTEST);
	// The idea is the ticks for all of the short
	// messages in both parsers will be compared.
	
	@Test
	public void test() {
		Sequence testSequence = null;
		try {
			testSequence = TrackSplitter.split(test_parser.getSequence(), 4, 1);
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		test_parser.setSequence(Solver.solve(testSequence));
		test_parser.quietParse();
		parser.quietParse();
		ArrayList<Long> actualTicks = parser.getTicks();
		ArrayList<Long> testTicks = test_parser.getTicks();
		
		if (actualTicks.size() != testTicks.size())
			fail("The size of the timestamp lists aren't even equal!");
		int errorTolerance = (int) Math.floor((actualTicks.size()*0.05));
		int errors = 0;
		for (int i = 0; i < actualTicks.size(); i++){
			System.out.print("Original Hysteria Track Timestamp: " + 
					actualTicks.get(i));
			System.out.println("\tAllocated Hysteria Track Timestamp: " + 
					testTicks.get(i));
			if (!actualTicks.get(i).equals(testTicks.get(i))){
				errors++;
				System.out.println("\t\tERROR# " + errors);
			}
		}
		System.out.println("Errors: " + errors);
		if (errors > errorTolerance)
			fail("Too many errors (based on an acceptable error rate of 5%): " + errors);
	}
}
