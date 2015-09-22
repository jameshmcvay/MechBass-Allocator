package helperCode;

import java.util.ArrayList;

import javax.sound.midi.ShortMessage;

/**
 * This class exists solely to be used with the GraphSolver.<p>
 * 
 * Every Node stores the information needed to help decide which string the  
 * note it contains should be played on.
 * 
 * @author Dean Newberry
 * */
public class Node {
	
	// The ShortMessage the Node was created to accommodate.
	private ShortMessage shortMessage = null;
	
	/* 
	 * WHEN the event containing the ShortMessage occurred. This is used to 
	 * identify which notes (if any) conflict.
	 */
	private long tick;
	
	// A list of the Nodes (notes) that could conflict with this one.
	private ArrayList<Node> neighbours = new ArrayList<>();

	/*
	 * A list of integers ranging from 0 – n-1, representing the number of the 
	 * strings the note can fit on (e.g. if this node has a note of 40, the 
	 * list will be [2, 3, 4], assuming default bass string settings. If this 
	 * node has a note of 52, the list will be [1]).
	 */
	private ArrayList<Integer> strings = new ArrayList<>();
	
	/*
	 * An integer ranging from 0 – n-1, representing the string the note will 
	 * be played on ("n" being the number of strings, unless it's on a dummy 
	 * string). This starting value is a flag that the string is unassigned.
	 */
	private int stringToPlayOn = Integer.MIN_VALUE;

	/**
	 * A full constructor - this one is unlikely to be used as it includes the 
	 * string it will be assigned, rather than this being solved using the 
	 * graph solver. Also contains two lists that should be populated via the 
	 * graph solver.
	 * 
	 * @param sm The ShortMessage.
	 * @param time The tick.
	 * @param ns The ArrayList of neighbouring Nodes.
	 * @param ss The ArrayList of Integers (representing Strings).
	 * @param string The String this Node (and therefore, the note inside it will be assigned.
	 * */
	public Node(ShortMessage sm, long time, ArrayList<Node> ns, ArrayList<Integer> ss, int string){
		shortMessage = sm;
		tick = time;
		for (Node n : ns)
			neighbours.add(n);
		for (Integer i : ss)
			strings.add(i);
		stringToPlayOn = string;
	}
	
	/**
	 * A constructor VERY similar to the one above - this one is also unlikely 
	 * to be used because, although the string is rightly left unknown, the 
	 * two lists are still assigned, when they should be populated via the 
	 * graph solver.
	 * 
	 * @param sm The ShortMessage.
	 * @param time The tick.
	 * @param ns The ArrayList of neighbouring Nodes.
	 * @param ss The ArrayList of Integers (representing Strings).
	 * @param string The String this Node (and therefore, the note inside it will be assigned.
	 * */
	public Node(ShortMessage sm, long time, ArrayList<Node> ns, ArrayList<Integer> ss){
		shortMessage = sm;
		tick = time;
		for (Node n : ns)
			neighbours.add(n);
		for (Integer i : ss)
			strings.add(i);
	}
	
	/**
	 * A more minimal constructor - this one is most likely to be used, as it 
	 * takes information that is most likely to be known upon creation.
	 * 
	 * @param sm The ShortMessage.
	 * @param time The tick.
	 * */
	public Node(ShortMessage sm, long time){
		shortMessage = sm;
		tick = time;
	}
	
	/**
	 * An empty constructor - this is for when you want to make a Node without 
	 * worrying about information. That said, you should use the setters to 
	 * assign fields as soon as possible.
	 * */
	public Node(){}

	/**
	 * A getter that returns the ShortMessage this Node was created to hold.
	 * @return The ShortMessage assigned to this Node.
	 * */
	public ShortMessage getShortMessage() {
		return shortMessage;
	}

	/**
	 * A setter that reassigns the ShortMessage this Node was created to hold.
	 * @param shortMessage The ShortMessage to be assigned to this Node.
	 * */
	public void setShortMessage(ShortMessage shortMessage) {
		this.shortMessage = shortMessage;
	}

	/**
	 * A getter that returns the tick of this Node - when the event that created the
	 * ShortMessage occurred.
	 * @return When the ShortMessage occurred.
	 * */
	public long getTick() {
		return tick;
	}

	/**
	 * A setter that assigns the tick of this Node. Because this indicates when
	 * the event that created the ShortMessage occurred, this *should* only be 
	 * assigned ONCE (however, I won't enforce this as mistakes could easily 
	 * happen).
	 * @param tick When the ShortMessage occurred.
	 * */
	public void setTick(long tick) {
		this.tick = tick;
	}

	/**
	 * A getter that returns the list of Nodes that could conflict with this 
	 * Node.
	 * @return The list of Nodes that could conflict with this node.
	 * */
	public ArrayList<Node> getNeighbours() {
		return neighbours;
	}

	/**
	 * A setter that assigns the list of Nodes that could conflict with this 
	 * Node. It is within your best interests to use the addNeighbour method to
	 * add neighbours rather than this method. This method should only be used 
	 * if you have a list ahead of time, if at all.
	 * @param The list of Nodes that could conflict with this node to be assigned.
	 * */
	public void setNeighbours(ArrayList<Node> neighbours) {
		this.neighbours = neighbours;
	}

	/**
	 * A getter that returns the list of all the Integers that represent the 
	 * strings this Node could be played on.
	 * @return The list of Integers that represent the strings that can play this Node.
	 * */
	public ArrayList<Integer> getStrings() {
		return strings;
	}

	/**
	 * A setter that assigns the list of Integers that represent the strings 
	 * this Node could be played on. Again, there exists another method, 
	 * AddPossibleString you should use to add strings as come on them, rather 
	 * than this method.
	 * @param strings The list of Integers that represent the strings that can play this Node to be assigned.
	 * */
	public void setStrings(ArrayList<Integer> strings) {
		this.strings = strings;
	}

	public int getStringToPlayOn() {
		return stringToPlayOn;
	}

	public void setStringToPlayOn(int stringToPlayOn) {
		this.stringToPlayOn = stringToPlayOn;
	}

	/**
	 * A convenience method to add a single string to the list of strings the
	 * Node could use.
	 * @param j The number of the string to add to the list.
	 * */
	public void addPossibleString(int j) {
		strings.add(j);	
	}

	/**
	 * A convenience method to add a Node to the list of neighbouring Nodes.
	 * @param node The Node to add to the list.
	 * */
	public void addNeighbour(Node node) {
		neighbours.add(node);
	}

	/**
	 * A convenience method to extract the note from this Node's shortMessage.
	 * REQUIRES: This Node's shortMessage MUST have a NOTE_ON or NOTE_OFF 
	 * 			command, otherwise an IllegalArgumentException is thrown.
	 * ENSURES: "Data1" is extracted this Node's shortMessage.
	 * @return The Note in this Node's shortMessage.
	 * */
	public int getNote() {
		if (shortMessage.getCommand() == ShortMessage.NOTE_ON || shortMessage.getCommand() == ShortMessage.NOTE_OFF)
			return shortMessage.getData1();
		else throw new IllegalArgumentException("This Node shouldn't have been used - it doesn't have a 'note'.");
	}
}
