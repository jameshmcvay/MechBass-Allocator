package helperCode;

import java.util.ArrayList;

import javax.sound.midi.ShortMessage;

public class Node {
	
	private ShortMessage shortMessage = null;
	private long tick;
	private ArrayList<Node> neighbours = new ArrayList<>();

	/*
	 * a list of integers ranging from 0 – n-1, representing the number of 
	 * strings the note can fit on (e.g. if this node has a note of 40, the 
	 * list will be [2, 3, 4]. if this node has a note of 52, the list will be
	 * [1]. Refer to the table above for the note ranges that fit on which 
	 * string)
	 */
	private ArrayList<Integer> strings = new ArrayList<>();
	
	/*
	 * an integer ranging from 0 – n-1, representing the string the note will 
	 * be played on (n being the number of strings, unless it's on a dummy 
	 * string). This starting value is a flag that the string is unassigned.
	 */
	private int stringToPlayOn = Integer.MIN_VALUE;

	public Node(ShortMessage sm, long time, ArrayList<Node> ns, ArrayList<Integer> ss, int string){
		shortMessage = sm;
		tick = time;
		for (Node n : ns)
			neighbours.add(n);
		for (Integer i : ss)
			strings.add(i);
		stringToPlayOn = string;
	}
	
	public Node(ShortMessage sm, long time, ArrayList<Node> ns, ArrayList<Integer> ss){
		shortMessage = sm;
		tick = time;
		for (Node n : ns)
			neighbours.add(n);
		for (Integer i : ss)
			strings.add(i);
	}
	
	public Node(){}

	public ShortMessage getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(ShortMessage shortMessage) {
		this.shortMessage = shortMessage;
	}

	public long getTick() {
		return tick;
	}

	public void setTick(long tick) {
		this.tick = tick;
	}

	public ArrayList<Node> getNeighbours() {
		return neighbours;
	}

	public void setNeighbours(ArrayList<Node> neighbours) {
		this.neighbours = neighbours;
	}

	public ArrayList<Integer> getStrings() {
		return strings;
	}

	public void setStrings(ArrayList<Integer> strings) {
		this.strings = strings;
	}

	public int getStringToPlayOn() {
		return stringToPlayOn;
	}

	public void setStringToPlayOn(int stringToPlayOn) {
		this.stringToPlayOn = stringToPlayOn;
	}

	public void addPossibleString(int j) {
		strings.add(j);	
	}

	public void addNeighbour(Node node) {
		neighbours.add(node);
	}

	public int getNote() {
		if (shortMessage.getCommand() == ShortMessage.NOTE_ON || shortMessage.getCommand() == ShortMessage.NOTE_OFF)
			return shortMessage.getData1();
		else throw new IllegalArgumentException("This Node shouldn't have been used - it doesn't have a 'note'.");
	}
}
