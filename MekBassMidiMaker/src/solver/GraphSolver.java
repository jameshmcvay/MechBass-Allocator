package solver;

import helperCode.Node;

import java.util.ArrayList;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class GraphSolver implements Solver {

	private static MekString[] strings;

	public GraphSolver(MekString[] str){
		strings = str;
	}
//#YOLOSWAG
	/**
	 * Takes a Sequence, and splits it up into a pre-defined number of tracks,
	 * dropping notes that do not fit within the constraints of the tracks,
	 * specified previously by the user.
	 * HOWEVER, it will do this using my (Dean's) bastardised pseudocode, which
	 * will (try to) use a graph colouring algorithm.
	 *
	 * NOT FINISHED YET
	 *
	 * @param seq The Sequence to be bodged
	 * @return The New Sequence
	 */
	public Sequence solve(Sequence seq){
		float tickScale = (float)seq.getMicrosecondLength()/seq.getTickLength()*1000;
		
		// Initialise the nodes with the short messages
		ArrayList<Node> allNodes = new ArrayList<>();
		ArrayList<Node> truncatedNodes = new ArrayList<>();
		Track tr = seq.getTracks()[0];

		// loop through
		for (int i=0; i<tr.size(); ++i){
			MidiMessage midmsg = tr.get(i).getMessage();
			// check what type of message it is
			if (midmsg instanceof ShortMessage){
				ShortMessage shrtmsg = (ShortMessage) midmsg;
				// Add a new node, then set the recently created node's ShortMessage to the recently created shrtmsg.
				Node newNode = new Node(shrtmsg, tr.get(i).getTick());
				allNodes.add(newNode);
				if (newNode.isHasNote()) truncatedNodes.add(newNode);
				int note = shrtmsg.getData1();
				for (int j = 0; j < strings.length; j++) {
					if (note >= strings[j].lowNote && note <= strings[j].highNote)
						newNode.addPossibleString(j);
				}
			}
			else{
				MetaMessage m = (MetaMessage) midmsg;
				if (m.getType() == 0x51){
					System.out.print(tr.get(i).getTick() + " tempo");
					for (int me: m.getMessage()){
						System.out.print(" 0x" + Integer.toHexString((int)(me & 0xFF)));
					}
					System.out.println();
				}
				if (m.getType() == 0x2f) break;
				System.out.printf("0x%x\n", m.getType());
				for (int j = 1; j <= strings.length; j++){
					seq.getTracks()[j].add(tr.get(i));
				}
			}
		}// End of for loop using "i" as the variable
		
		for (int j = 1; j < truncatedNodes.size(); j++){
			for (int n = 0; n < j; n++){
				Node node1 = truncatedNodes.get(j);
				Node node2 = truncatedNodes.get(n);
				for (int o = 0; o < strings.length; o++){
					if (node1.getStrings().contains(o) && node2.getStrings().contains(o)){
						// If the 2 nodes can be on the same string, they MIGHT be neighbours
						if (strings[o].conflicting(node1.getNote(),node2.getNote(), node1.getTick() - node2.getTick(), tickScale)){
							// If the 2 nodes can conflict, they ARE neighbours.
							if (!node1.getNeighbours().contains(node2)) // If node1 DOESN'T KNOW node2 is a neighbour...
								node1.addNeighbour(node2);				// Rectify this situation immediately.
							if (!node2.getNeighbours().contains(node1))	// If node2 DOESN'T KNOW node1 is a neighbour...
								node2.addNeighbour(node1);				// Rectify this situation immediately.
						}
					}
				}	// End of for loop using "o" as the variable
			}		// End of for loop using "n" as the variable
		}			// End of for loop using "j" as the variable

		
		// Now that the nodes are initialised, we can move onto using this graph to assign strings.
		for(int j = 0; j < truncatedNodes.size(); j++){
			Node node = truncatedNodes.get(j);
			
			if (node.getStringToPlayOn() != Integer.MIN_VALUE || node.getStrings().isEmpty())
				continue;
			
			else{
				ArrayList<Integer> stringNums = new ArrayList<>();
				for (Node n : node.getNeighbours()){
					for (Integer in : node.getStrings()){
						if (!n.getStrings().contains(in) ||
							(n.getStrings().contains(in) && node.getStrings().size() == 1)){
								stringNums.add(in);
						}
					}
				}
				stringNums.sort(null);
				int mode = -1; 			// Statistical measure for the "the most common"
				int modeIndex = -1;		// The index of the first occurrence of the mode
				int modeTotal = 0;		// The number of times the most available string (so far) is available
				
				int currentNum = -1;	// The number we're totaling now (may be the mode).
				int currentIndex = 0;	// The index of the first occurrence of the current number.
				int currentTotal = 0;	// The total number of times we found currentNum. 
				
				for (Integer in : stringNums){
					if (mode == -1){
						mode = in;
						modeIndex = currentIndex;
						modeTotal++;
						
						currentNum = in;
						currentTotal = 1;
					}
					else if (mode == in){
						modeTotal++;
						currentTotal++;
					}
					else if (mode != in){
						if (currentNum != in){
							currentNum = in;
							currentTotal = 1;
							currentIndex = stringNums.indexOf(in);
						}
						else{
							currentTotal++;
						}
						if (modeTotal < currentTotal){
							mode = currentNum;
							modeIndex = currentIndex;
							modeTotal = currentTotal;
						}
					}
				}// End of for each loop using "in" from stringNums as the variable
				node.setStringToPlayOn(mode);
			}// End of else clause.
		}// End of ANOTHER for loop using "j" as the variable
		
		int eventIndex=0;
		for (Node n : truncatedNodes){
			if (n.getStringToPlayOn() == Integer.MIN_VALUE){
				System.out.println("Missed String: " + n);
			}
			else{
				seq.getTracks()[n.getStringToPlayOn()+1].add(tr.get(eventIndex));
				if (!tr.remove(tr.get(eventIndex)))
					eventIndex++;
			}
		}		
		return seq;
	}
}
