package ui;

import java.rmi.UnexpectedException;

import main.Parser;

public class Fret{
	private Parser.note note;
	private long delay;

	public Fret(){}

	public Parser.note getNote(){
		return note;
	}

	public void setNote(String n) throws UnexpectedException{
		if(n.equals("a") || n.equals("A")){
			note = Parser.note.A;
		}
		else if(n.equals("G") || n.equals("g")){
			note = Parser.note.G;
		}
		else if(n.equals("D") || n.equals("d")){
			note = Parser.note.D;
		}
		else if(n.equals("E") || n.equals("e")){
			note = Parser.note.E;
		}
		else{
			throw new UnexpectedException("Invalid Input!");
		}
	}

	public long getDelay(){
		return delay;
	}

	public void setDelay(long d){
		delay = d;
	}

	public String toString(){
		String out = "{Fret:";
		out += note.name() + " " + delay;
		return (out + "}");
	}
}