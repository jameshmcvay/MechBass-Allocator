package ui;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.*;

import main.Parser;
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

	public static Fret[] StartUpParams(boolean useGUI) throws IOException{
		Fret[] fretArr = null;
		if(useGUI){}
		else{
			System.out.println("How many Strings would you like to use?");
			Scanner sc = new Scanner(System.in);

			int numFrets = sc.nextInt();

			fretArr = new Fret[numFrets];

			System.out.println("You will now be prompted for the details on each fret:");

			for(int i = 0; i < numFrets; i++){

				fretArr[i] = new Fret();

				System.out.printf("%s %d%s\n","Fret", i ,":");
				System.out.println("|\tInput Note:");
				fretArr[i].setNote(sc.next());

				System.out.println("|\tInput Delay:");
				fretArr[i].setDelay(sc.nextLong());

			}

			sc.close();
		}

		return fretArr;
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