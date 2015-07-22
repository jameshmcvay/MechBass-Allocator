package ui;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.*;
/**
 * Catch all class for activating UI on the fly, typically expect static fire and forget methods
 *
 * @author macdondyla1, oswaldgreg
 *
 */
public class UI {
	/**
	 * Opens the JFileChooser Dialog http://docs.oracle.com/javase/7/docs/api/javax/swing/JFileChooser.html from swing.
	 *
	 * @return The File object of the file selected
	 */
	public static File FileChooser(){
		JFileChooser fiChoo = new JFileChooser();
		fiChoo.showOpenDialog(null);
		return fiChoo.getSelectedFile();
	}

	public static void StartUpParams(boolean useGUI) throws IOException{
		if(useGUI){}
		else{
			System.out.println("How many Strings would you like to use?");
			Scanner sc;
			sc = new Scanner(System.in);
			System.err.println(sc.next());
			sc.close();
		}
	}

	public class Fret{
		public enum note {
			C, CSharp,
			DFlat, D, DSharp,
			EFlat, E, ESharp,
			F, FSharp,
			GFlat, G, GSharp,
			AFlat, A, ASharp,
			BFlat, B
		}
		public Fret(){

		}
	}

	public static void main(String args[]){
		try{
			StartUpParams(false);
		}
		catch(IOException e){
			System.err.println(e);
		}
	}

}