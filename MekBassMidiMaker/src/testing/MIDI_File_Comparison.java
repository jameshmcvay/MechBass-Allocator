package testing;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import main.Parser3;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import solver.Solver;

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
		test_parser.setSequence(Solver.solve(test_parser.getSequence()));
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
					actualTicks.get(i));
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
