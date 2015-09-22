package solver;

import helperCode.Node;

import java.util.ArrayList;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class GraphSolver implements Solver {

	private static MekString[] strings;

	public GraphSolver(MekString[] str){
		strings = str;
	}

	/**
	 * Takes a Sequence, and splits it up into a pre-defined number of tracks,
	 * dropping notes that do not fit within the constraints of the tracks,
	 * specified previously by the user.
	 * HOWEVER, it will do this using my (Dean's) bastardised pseudocode, which
	 * will (try to) use a graph colouring algorithm.
	 *
	 * NOT FINISHED YET - THE SEQUENCE PUT IN IS COMPLETELY UNMOLESTED BY MY CODE.
	 *
	 * @param seq The Sequence to be bodged
	 * @return The New Sequence
	 */
	public Sequence solve(Sequence seq){
		// Initialise the nodes with the short messages
		ArrayList<Node> allNodes = new ArrayList<>();
		Track tr = seq.getTracks()[0];

		// loop through
		for (int i=0; i<tr.size(); ++i){
			MidiMessage midmsg = tr.get(i).getMessage();
			// check what type of message it is
			if (midmsg instanceof ShortMessage){
				ShortMessage shrtmsg = (ShortMessage) midmsg;
				// Add a new node, then set the recently created node�s ShortMessage to the recently created shrtmsg.
				Node newNode = new Node();
				allNodes.add(newNode);
				newNode.setShortMessage(shrtmsg);
				// Get the tick of the event attached to the message.
				newNode.setTick(tr.get(i).getTick());
				// Now go through the strings and check if the note can be played on any of them.
				int note = shrtmsg.getData1();
				for (int j = 0; j < strings.length; j++) {
					if (note >= strings[j].lowNote && note <= strings[j].highNote)
						newNode.addPossibleString(j);
				}
			}
		}
		for(int j = 1; j < allNodes.size(); j++){
			for(int n = 0; n < j; n++){
				Node node1 = allNodes.get(j);
				Node node2 = allNodes.get(n);
				for (int o = 0; o < strings.length; o++){
					if (node1.getStrings().contains(o) && node2.getStrings().contains(o)){
						if (strings[o].conflicting(node1.getNote(),node2.getNote(), node1.getTick() - 						node1.getTick()) && (!node1.getNeighbours().contains(node2) || 							!node2.getNeighbours().contains(node1)) ){
							node1.addNeighbour(node2);
							node2.addNeighbour(node1);
						}
					}
				}
			}
		}

		// Now that the nodes are initialised, we can move onto using this graph to assign strings.
		for(int j = 0; j < allNodes.size(); j++){
			Node node = allNodes.get(j);
			if (node.getStringToPlayOn() != Integer.MIN_VALUE || node.getStrings().isEmpty())
				continue;
			else{
				for (Node n : node.getNeighbours()){
					for (Integer i : node.getStrings()){
						if (!n.getStrings().contains(i) ||
							(n.getStrings().contains(i) && node.getStrings().size() == 1))
							node.setStringToPlayOn(i);
					}
				}
			}
		}

		return seq;
	}

}
