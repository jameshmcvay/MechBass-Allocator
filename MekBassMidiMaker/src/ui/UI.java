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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
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

	//4:3 screen ratio
	double width = 1200;
	double height = 900;


	TextArea textConsole = null; //the console

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
			public void handle(ActionEvent event){playerStop();}});

		Button loadBtn = new Button();
		loadBtn.setText("Load");

		loadBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event){
					setCurMIDI();
			}
		});

		//Initialise the console
		textConsole =  new TextArea();
		textConsole.setPrefColumnCount(100);
		textConsole.setPrefRowCount(10);
		textConsole.setWrapText(true);
		textConsole.setPrefWidth(width-(width * .01));
//		System.out.println(width-(width * .01));
		textConsole.setPrefHeight(height/2.1);

		//window manager
		FlowPane root =  new FlowPane();
		root.setOrientation(Orientation.HORIZONTAL);
		root.setHgap(width * .01);
		root.setPadding(new Insets(1, 1, 1, 1));


		FlowPane buttonPanel =  new FlowPane();
		buttonPanel.setPadding(new Insets(3, 0, 0, 3));
		buttonPanel.getChildren().addAll(playBtn, stpBtn, loadBtn);

		//Canvas
		Canvas canvas = new Canvas();
		canvas.setWidth(width-(width *.01));
		canvas.setHeight(height/2.1);


		GraphicsContext gc = canvas.getGraphicsContext2D();
		drawShapes(gc);

		FlowPane consolePanel = new FlowPane(Orientation.HORIZONTAL, canvas, textConsole);
		consolePanel.setVgap(height/200);
		consolePanel.setPadding(new Insets(height/200, 0, 0, width/200));

		//add the window elements
		root.getChildren().addAll(buttonPanel,consolePanel);

	    Scene scene =  new Scene(root,width,height);

	    scene.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				handleKeyEvent(event);

			}
		});


	    primaryStage.setTitle("Google");
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}

    private void drawShapes(GraphicsContext gc) {
    	gc.setFill(Color.BLANCHEDALMOND);
    	gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    	gc.setFill(Color.GREEN);
		gc.setStroke(Color.BLUE);
		gc.setLineWidth(5);
		gc.strokeLine(40, 10, 10, 40);
		gc.fillOval(10, 60, 30, 30);
		gc.strokeOval(60, 60, 30, 30);
		gc.fillRoundRect(110, 60, 30, 30, 10, 10);
		gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
		gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
		gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
		gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
		gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
		gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
		gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
		gc.fillPolygon(new double[]{10, 40, 10, 40},
		                 new double[]{210, 210, 240, 240}, 4);
		gc.strokePolygon(new double[]{60, 90, 60, 90},
		                   new double[]{210, 210, 240, 240}, 4);
		gc.strokePolyline(new double[]{110, 140, 110, 140},
		                    new double[]{210, 210, 240, 240}, 4);
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

			//Assign the selected file to fi and convert it to a MIDI
			File fi = fiChoo.showOpenDialog(null);
			if(fi != null)
				curMIDI = MidiSystem.getSequence(fi);

		} catch (InvalidMidiDataException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]){
		launch(args);
	}

	@Override
	public void stop(){
		playerStop();
		playerRelease();

	}

	public void playerRelease(){
		Player.release();
	}

	public void playerStop(){
		Player.stop();
	}

	private void handleKeyEvent(KeyEvent event){
		switch (event.getCode() +"") { //added to the empty string for implicit conversion
		case "ENTER":

			break;

		default:
			break;
		}
	}

}