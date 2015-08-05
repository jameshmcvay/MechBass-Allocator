package ui;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import tools.Player;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
/**
 * Container class for the UI
 *
 * @author macdondyla1, oswaldgreg
 *
 */
public class UI extends Application {

	Sequence curMIDI;//the File we're currently modifying


	@Override
	public void start(Stage primaryStage) throws Exception {
		//-----Create the set of buttons to be added to the graphics pane-------
		Button playBtn = new Button();
		playBtn.setText("Play");

		playBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {play();}});

		Button stpBtn = new Button();
		stpBtn.setText("Stop");

		stpBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event){Player.stop();}});

		Button loadBtn = new Button();
		loadBtn.setText("Load");

		loadBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event){
					setCurMIDI();
			}
		});
		//Initialise the console
		TextArea console =  new TextArea();
//		console.

		//window manager
		GridPane root = new GridPane();
		//Spacing
		root.setHgap(3);
		root.setVgap(3);
		//add the window elements
		root.add(playBtn,1,1);
	    root.add(stpBtn,2,1);
	    root.add(loadBtn, 3, 1);

	    //window bounds
	    Scene scene =  new Scene(root,1600,1200);
	    primaryStage.setTitle("Google");
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}

//	protected void

	//--------------Button methods----------
	//Play the MIDI
	protected void play() {
			if(curMIDI != null)
				Player.play(curMIDI);
	}

	//Load a new Sequence
	protected void setCurMIDI(){
		try {
			FileChooser fiChoo =  new FileChooser();
			fiChoo.setTitle("Select the MIDI File to be converted.");
			File fi = fiChoo.showOpenDialog(null);
			curMIDI = MidiSystem.getSequence(fi);
		} catch (InvalidMidiDataException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]){
		launch(args);
	}



}