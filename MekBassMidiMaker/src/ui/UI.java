package ui;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Container class for the UI
 *
 * @author macdondyla1, oswaldgreg
 *
 */
public class UI extends Application{

//	Sequence curMIDI;//the File we're currently modifying

	//4:3 screen ratio
	double width = 1200;
	double height = 900;
	protected static String args[];

	Slave slave;
	TextArea textConsole = null; //the console

	//Contains launches the application, for all intents and purposes, this is the contructor
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setResizable(false);
		//-----Create the set of buttons to be added to the graphics pane-------
		FlowPane buttonPanel = BuildButtons();
		GridPane gridPane = new GridPane();

		//Initialise the console
		textConsole = new TextArea();
		leftCanvasWidth = width * (3.0 / 5.0);
		double canvasHeight = height * 0.667;


		Canvas leftCanvas = new Canvas();
		leftCanvas.setWidth(leftCanvasWidth);
		leftCanvas.setHeight(canvasHeight);

		double rightCanvasWidth = width * (2.0 / 5.0);
		Canvas rightCanvas = new Canvas();
		rightCanvas.setWidth(rightCanvasWidth);
		rightCanvas.setHeight(canvasHeight);

		GridPane leftGUIGridPane = buildLeftGUI();


		textConsole.setPrefColumnCount(100);
		textConsole.setPrefRowCount(10);
		textConsole.setWrapText(true);
		textConsole.setPrefWidth(leftCanvasWidth);
		textConsole.setPrefHeight(height * 0.332);
		textConsole.setUserData("TextConsole");

//		GridPane rightLowPanel = new GridPane();

		GraphicsContext gc = leftCanvas.getGraphicsContext2D();
		drawShapes(gc);

		//Add elems to the gridPane
		gridPane.add(leftCanvas, 0, 0);
		gridPane.add(textConsole, 0, 1);
		gridPane.add(leftGUIGridPane, 1, 0);
		gridPane.add(buttonPanel, 1, 1);

		Scene scene =  new Scene(gridPane,width,height);

	    textConsole.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				handleConsoleKeyEvent(event);
			}
		});

	    //TODO Make GUILISE
	    slave = new Slave();
	    slave.setUI(this);
	    Console console = new Console(true,getConsoleTextArea(),slave);
		slave.setConsole(console);
	    PrintStream ps = new PrintStream(console, true);

	    System.setOut(ps);
	    System.setErr(ps);

	    primaryStage.setTitle("MSNSearch");
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}

	int stringsToDefine;
	int stringsNumber;
	String saveFileName = " ";
	private GridPane buildLeftGUI() {
		GridPane GPane = new GridPane();

		//Input a name
		Label nameLabel = new Label("File Name: ");
		nameLabel.setFont(new Font("FreeSans", 20));

		TextField nameTextField =  new TextField();
		nameTextField.setUserData("TextName");
		nameTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				handleNameKeyEvent(event);}});

		//Input the number of strings, this will notify the main pane that Strings still need to be defined
		Label stringLabel = new Label("Input Number of Strings: ");
		stringLabel.setFont(new Font("FreeSans", 20));

		TextField stringsTextField = new TextField();
		stringsTextField.setUserData("TextString");
		stringsTextField.setOnKeyReleased(new EventHandler<KeyEvent>(){
			@Override
			public void handle(KeyEvent event) {
				handleNameKeyEvent(event);}});

		//Add all above elems to the GridPane
		GPane.add(nameLabel, 0, 0);
		GPane.add(nameTextField, 1, 0);
		GPane.add(stringLabel, 0, 1);
		GPane.add(stringsTextField,1,1);


//		canvas.
		/*
		 * name
		 * strings
		 * octaves to shift
		 * preposition
		 * solver
		 */
		return GPane;
	}

	protected void handleNameKeyEvent(KeyEvent event) {
		// Get the source of the event (Object) cast to TextArea get the userdata, cast it to String and check if the name is valid for this method
		String checkName = (
								(
									(String)(
										(TextField)event.getSource()
									).getUserData()
								)
							);
		System.out.println("it is" + checkName);
		if(checkName.equals("TextString"))
		switch (event.getCode() +"") {
		case "ENTER":
			stringsNumber = stringsToDefine = Integer.parseInt(((TextField) event.getSource()).getText());
			break;

		default:
			break;
		}
		System.out.println(stringsNumber);
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
			public void handle(ActionEvent event){setCurrentMIDI();}});

		Button solveBtn = new Button();//The Solve Button
		solveBtn.setText("Solve");

		solveBtn.setOnAction(new EventHandler<ActionEvent>() {
			//Call to solve
			@Override
			public void handle(ActionEvent event) {solve();}});

		Button saveBtn = new Button();
		saveBtn.setText("Save");
		saveBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {save();}});

		FlowPane buttonPanel =  new FlowPane();
		buttonPanel.setPadding(new Insets(3, 0, 0, 3));
		buttonPanel.getChildren().addAll( playBtn, stpBtn, saveBtn, loadBtn, solveBtn);

		return buttonPanel;
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

	File lastFileLocation;
	private double leftCanvasWidth;

	//Load a new Sequence
	protected void setCurrentMIDI(){
		try {
			File fi = fileChooser("Select a MIDI file to open");

			if(fi != null){
				Slave.curMIDI = MidiSystem.getSequence(fi);
			}

		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}
	}

	protected void save(){
		try {
			DirectoryChooser dirChoo = new DirectoryChooser();
			dirChoo.setTitle("Select Save Location");

			if(lastFileLocation != null){
				dirChoo.setInitialDirectory(lastFileLocation);
			}

			File fi = dirChoo.showDialog(null);
			if(fi != null){
				lastFileLocation = fi.getCanonicalFile().getParentFile();
				slave.save(fi);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File fileChooser(String string) throws IOException, InvalidMidiDataException {
		FileChooser fiChoo = new FileChooser();
		fiChoo.setTitle(string);
		if(lastFileLocation != null){
			fiChoo.setInitialDirectory(lastFileLocation);
		}

		File fi = fiChoo.showOpenDialog(null);

		if(fi!= null){
			lastFileLocation = fi.getCanonicalFile().getParentFile();
		}
		return fi;
	}

	@Override
	public void stop(){
		slave.playerStop();
		slave.playerRelease();
	}


	public TextArea getConsoleTextArea(){
		return textConsole;
	}


	private void handleConsoleKeyEvent(KeyEvent event){
			System.out.println("truth");
			switch (event.getCode() +"") { //added to the empty string for implicit conversion
			case "ENTER":
				slave.getConsole().Parse(textConsole.getText());
	            break;

			default:
				break;
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
			Console console = new Console();
			slave.setConsole(console);
		}
	}


}
