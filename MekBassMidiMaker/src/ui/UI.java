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
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
	double height = 1200;
	double width = 1600;

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
		console.setPrefColumnCount(100);
		console.setPrefRowCount(10);
		console.setWrapText(true);
		console.setPrefWidth(width-(width * .01));
		System.out.println(width-(width * .01));
		console.setPrefHeight(height/3);

		//window manager
		FlowPane root =  new FlowPane();
		root.setOrientation(Orientation.VERTICAL);
		root.setHgap(width * .01);
		root.setPadding(new Insets(1, 1, 1, 1));
		FlowPane buttonPanel =  new FlowPane();
		buttonPanel.getChildren().addAll(playBtn, stpBtn, loadBtn);
		FlowPane consolePanel = new FlowPane(Orientation.VERTICAL, console);
		consolePanel.setAlignment(Pos.CENTER);
		//add the window elements
		root.getChildren().addAll(buttonPanel,consolePanel);

	    Scene scene =  new Scene(root,width,height);
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