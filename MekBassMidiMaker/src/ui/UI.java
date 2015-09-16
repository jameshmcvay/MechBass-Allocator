package ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

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
import javafx.scene.layout.GridPane;
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
	protected static String args[];

	Slave slave;
	TextArea textConsole = null; //the console

	//Contains launches the application, for all intents and purposes, this is the contructor
	@Override
	public void start(Stage primaryStage) throws Exception {

		//-----Create the set of buttons to be added to the graphics pane-------
		FlowPane buttonPanel = BuildButtons();
		GridPane gridPane = new GridPane();

		//Initialise the console
		textConsole = new TextArea();

		//Canvas
		double canvasWidth = width * (3.0 / 5.0);
		double canvasHeight = height * 0.667;

		Canvas leftCanvas = new Canvas();
		leftCanvas.setWidth(canvasWidth);
		leftCanvas.setHeight(canvasHeight); //2/3

		Canvas rightCanvas = new Canvas();
		rightCanvas.setWidth(canvasWidth);
		rightCanvas.setHeight(canvasHeight);

		GraphicsContext gc = rightCanvas.getGraphicsContext2D();
		drawShapes(gc);

		textConsole.setPrefColumnCount(100);
		textConsole.setPrefRowCount(10);
		textConsole.setWrapText(true);
		textConsole.setPrefWidth(canvasWidth);
		textConsole.setPrefHeight(height * 0.332);

		GridPane rightLowPanel = new GridPane();

		gc = leftCanvas.getGraphicsContext2D();
		drawShapes(gc);

		//Add elems to the gridPane
		gridPane.add(leftCanvas, 0, 0);
		gridPane.add(textConsole, 0, 1);
		gridPane.add(rightCanvas, 1, 0);
		gridPane.add(buttonPanel, 1, 1);
	    Scene scene =  new Scene(gridPane,width,height);

	    scene.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				handleKeyEvent(event);

			}
		});

	    //TODO Make GUILISE
	    slave = new Slave();
	    slave.setUI(this);
	    Console console = new Console(getConsoleTextArea(),slave);
		slave.setConsole(console);

	    PrintStream ps = new PrintStream(console, true);
	    System.setOut(ps);
	    System.setErr(ps);

	    primaryStage.setTitle("Bing");
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}


	protected void solve() {
		slave.solve();

	}

	protected void playerStop() {
		slave.playerStop();

	}

	protected void play() {
		slave.play();
	}

	private FlowPane BuildButtons(){
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

		Button saveBtn = new Button();//The Save Button
		saveBtn.setText("Save");

		saveBtn.setOnAction(new EventHandler<ActionEvent>() {//when pushed
			//call to saveCurMIDI
			@Override
			public void handle(ActionEvent event){saveCurMIDI();}});

		Button TsaveBtn = new Button();//The Save Button
		TsaveBtn.setText("TSave");

		TsaveBtn.setOnAction(new EventHandler<ActionEvent>() {//when pushed
			//call to saveCurMIDI
			@Override
			public void handle(ActionEvent event){testSave();}});

		Button solveBtn = new Button();//The Solve Button
		solveBtn.setText("Solve");

		solveBtn.setOnAction(new EventHandler<ActionEvent>() {
			//Call to solve
			@Override
			public void handle(ActionEvent event) {solve();}});


		FlowPane buttonPanel =  new FlowPane();
		buttonPanel.setPadding(new Insets(3, 0, 0, 3));
		buttonPanel.getChildren().addAll( playBtn, stpBtn, loadBtn, saveBtn, TsaveBtn, solveBtn);

		return buttonPanel;
	}

	private void drawShapes(GraphicsContext gc) {
			Random rand =  new Random();

            gc.setFill(Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
            gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

//            gc.setFill(Color.GREEN);
//            gc.setStroke(Color.BLUE);
//            gc.setLineWidth(5);
//            gc.strokeLine(40, 10, 10, 40);
//            gc.fillOval(10, 60, 30, 30);
//            gc.strokeOval(60, 60, 30, 30);
//            gc.fillRoundRect(110, 60, 30, 30, 10, 10);
//            gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
//            gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
//            gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
//            gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
//            gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
//            gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
//            gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
//            gc.fillPolygon(new double[]{10, 40, 10, 40},
//                             new double[]{210, 210, 240, 240}, 4);
//            gc.strokePolygon(new double[]{60, 90, 60, 90},
//                               new double[]{210, 210, 240, 240}, 4);
//            gc.strokePolyline(new double[]{110, 140, 110, 140},
//                                new double[]{210, 210, 240, 240}, 4);
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
				Slave.curMIDI = MidiSystem.getSequence(fi);
			}

		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}
	}

	protected void saveCurMIDI(){
		try {
			FileChooser fiChoo = new FileChooser();
			fiChoo.setTitle("Select the location to save the file");
			if(lastFileLocation != null){
				fiChoo.setInitialDirectory(lastFileLocation);
			}
			File retrival = fiChoo.showSaveDialog(null);
			if (retrival != null && Slave.curMIDI != null){
			MidiSystem.write(Slave.curMIDI, 1, retrival);
			}
		}
		catch (IOException e){
			System.out.print("error saving the file");
		}
	}

	protected void testSave(){
		solver.tests.Sequence seq = (solver.tests.Sequence) Slave.curMIDI;
		setCurMIDI();
		if(seq.equals(Slave.curMIDI)){
			System.out.print("It succeded");
		}
	}

	public static void main(String args[]){
		if(args.length == 0){
			args = new String[1];
			args[0] = "true";
		}
		if(Boolean.parseBoolean(args[0])){
			launch(args);
		}
		else{
			//TODO Make sure this works, get it a job if you have to
			Slave slave = new Slave();
			Console console = new Console(slave);
			slave.setConsole(console);

		}
	}

	@Override
	public void stop(){
		slave.playerStop();
		slave.playerRelease();
	}



	private void handleKeyEvent(KeyEvent event){
		switch (event.getCode() +"") { //added to the empty string for implicit conversion
		case "ENTER":
			slave.getConsole().Parse(textConsole.getText());
            break;

		default:
			break;
		}
	}

	public TextArea getConsoleTextArea(){
		return textConsole;
	}

}
