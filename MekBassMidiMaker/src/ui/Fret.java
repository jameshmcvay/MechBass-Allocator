package ui;

import main.Parser;

public class Fret{
	private Parser.note note;
	private long delay;
	public Fret(){}

	public Parser.note getNote(){
		return note;
	}

	public void setNote(String n){
		//TODO
	}

	public long getDelay(){
		return delay;
	}

	public void setDelay(long d){
		delay = d;
	}
}