package ui;

import java.io.File;

import javax.swing.*;
/**
 * Catchall class for activating UI on the fly, typically expect static fire and forget methods
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

}
