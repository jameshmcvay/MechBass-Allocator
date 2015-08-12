package ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import solver.TrackSplitter;
import tools.Player;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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

	Console console;
	TextArea textConsole = null; //the console

    //Contains launches the application, for all intents and purposes, this is the contructor
	@Override
	public void start(Stage primaryStage) throws Exception {
		//-----Create the set of buttons to be added to the graphics pane-------
	    Button playBtn = new Button();//The play button
		playBtn.setText("Play");

		playBtn.setOnAction(new EventHandler<ActionEvent>() {//on push events
			//call to play
			@Override
			public void handle(ActionEvent event) {play();}});

		Button stpBtn = new Button();//The Stop Button
		stpBtn.setText("Stop");

		stpBtn.setOnAction(new EventHandler<ActionEvent>() {//when pushed
			//Call to playerStop
			@Override
			public void handle(ActionEvent event){playerStop();}});

		Button loadBtn = new Button();//The load Button
		loadBtn.setText("Load");

		loadBtn.setOnAction(new EventHandler<ActionEvent>() {//when pushed
			//call to setCurrMIDI
			@Override
			public void handle(ActionEvent event){setCurMIDI();}});

		Button solveBtn = new Button();//The Solve Button
		solveBtn.setText("Solve");

		solveBtn.setOnAction(new EventHandler<ActionEvent>() {
			//Call to solve
			@Override
			public void handle(ActionEvent event) {solve();}});

		//Initialise the console
		textConsole = new TextArea();
		textConsole.setPrefColumnCount(100);
		textConsole.setPrefRowCount(10);
		textConsole.setWrapText(true);
		textConsole.setPrefWidth(width - (width * .01));
		textConsole.setPrefHeight(height / 2.1);

		//window manager
		FlowPane root =  new FlowPane();
		root.setOrientation(Orientation.HORIZONTAL);
		root.setHgap(width * .01);
		root.setPadding(new Insets(1, 1, 1, 1));


		FlowPane buttonPanel =  new FlowPane();
		buttonPanel.setPadding(new Insets(3, 0, 0, 3));
		buttonPanel.getChildren().addAll(playBtn, stpBtn, loadBtn, solveBtn);

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

	    //TODO Make GUILISE
	    console =  new Console(true, textConsole, this);

	    primaryStage.setTitle("Google");
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}

	protected void solve() {
		console.solve();

	}

	protected void playerStop() {
		console.playerStop();

	}

	protected void play() {
		console.play();

	}



	private void drawShapes(GraphicsContext gc) {
			Random rand =  new Random();

            gc.setFill(Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
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

	//--------------Button methods----------
	File lastFileLocation;
	//Load a new Sequence
	protected void setCurMIDI(){
		try {
			FileChooser fiChoo =  new FileChooser();
			fiChoo.setTitle("Select the MIDI File to be converted.");

			//Assign the selected file to fi and convert it to a MIDI
			if(lastFileLocation != null){
				fiChoo.setInitialDirectory(lastFileLocation);
			}

			File fi = fiChoo.showOpenDialog(null);

			if(fi != null){
				lastFileLocation = fi.getCanonicalFile().getParentFile();
				console.curMIDI = MidiSystem.getSequence(fi);
			}

		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]){
		launch(args);
	}

	@Override
	public void stop(){
		console.playerStop();
		console.playerRelease();
	}



	private void handleKeyEvent(KeyEvent event){
		switch (event.getCode() +"") { //added to the empty string for implicit conversion
		case "ENTER":
			console.Parse(textConsole.getText());
            break;

		default:
			break;
		}
	}

}
