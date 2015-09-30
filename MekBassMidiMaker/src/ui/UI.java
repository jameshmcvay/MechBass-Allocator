package ui;

import helperCode.OctaveShifter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Container class for the UI
 *
 * @author macdondyla1, oswaldgreg
 *
 */
public class UI extends Application{

	//4:3 screen ratio
	double width = 1200;
	double height = 900;
	protected static String args[];

	Slave slave;
	TextArea textConsole = null; //the console
	Simulation sim;
	Timer timer;
	int timerTime = 1000/60;

	//Contains launches the application, for all intents and purposes, this is the contructor
	@Override
	public void start(Stage primaryStage) throws Exception {
		//---------------------
		//Popup window at the start
//		doPopUp();
		//
		//--------------------------




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
	    Console console = new Console(getConsoleTextArea(),slave);
		slave.setConsole(console);

//	    PrintStream ps = new PrintStream(console, true);
//	    System.setOut(ps);
//	    System.setErr(ps);

	    primaryStage.setTitle("Blackle");
	    primaryStage.setScene(scene);
	    primaryStage.show();

	    sim = new Simulation();
	    slave.setSim(sim);

	 // set a run loop
	    timer = new java.util.Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
		    public void run() {
		         Platform.runLater(new Runnable() {
		            public void run() {
//		                label.update();
//		                javafxcomponent.doSomething();
//		            	System.out.println("test");
		            	if (sim.isPlaying()) {
		            		sim.addTime(timerTime);
		            		sim.addDrawStartTime(timerTime);
		            	}
		            	sim.draw(leftCanvas.getGraphicsContext2D(), 1);

//		            	sim.addDrawStartTime(14);
		            }
		        });
		    }
		}, timerTime, timerTime);


		/*
		 * DEAN MAKING A MENU BAR:
		 *
		 * http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm
		 * (Starting Point)
		 *
		 * */
		setupMenuBar(scene);
	}

	private void setupMenuBar(Scene scene){
		/*
		 * DEAN MAKING A MENU BAR:
		 *
		 * http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm
		 * (Starting Point)
		 *
		 * */
		MenuBar menuBar = new MenuBar(); // The Bar where "| File | Edit | View |" will be
        // --- Menu File
        Menu menuFile = new Menu("File");
        setupFileMenu(menuFile);
        // --- Menu Edit
        Menu menuPlay = new Menu("Playback");
        setupEditMenu(menuPlay);
        // --- Menu View
        Menu menuHelp = new Menu("HALP! WOT DOO!?");
        setupHelpMenu(menuHelp);

        menuBar.getMenus().addAll(menuFile, menuPlay, menuHelp);

        VBox vbox = new VBox(menuBar);

        ((GridPane) scene.getRoot()).getChildren().addAll(vbox);
	}

	private void setupFileMenu(Menu menuFile){
		MenuItem NC = new MenuItem("New Config");
	        NC.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		doPopUp();
	        	}
	        });
	    MenuItem OM = new MenuItem("Open MIDI File");
	        OM.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		setCurrentMIDI();
	    			//Remove and re-add the eventhandler, this is to avoid it
	        		//being called upon changing the contents of the combobox
	    			EventHandler<ActionEvent> temp = BassTrackComboBox.getOnAction();
	    			BassTrackComboBox.setOnAction(null);
	    			BassTrackComboBox.setItems(populateComboBox());
	    			BassTrackComboBox.setOnAction(temp);
	    		}
	        });
	    MenuItem SaM = new MenuItem("Save MIDI File");
	        SaM.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		save();
	        	}
	        });
	    MenuItem SoM = new MenuItem("Solve MIDI File");
	        SoM.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		solve();
	        	}
	        });
	    MenuItem Q = new MenuItem("Quit");
	        Q.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		System.exit(0);
	        	}
	        });
	    menuFile.getItems().addAll(NC, OM, SaM, SoM, Q);
	}

	private void setupEditMenu(Menu menuPlay) {
		MenuItem Pl = new MenuItem("Play");
	        Pl.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		play();
	        	}
	        });
	    MenuItem St = new MenuItem("Stop");
	        St.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		playerStop();
	        	}
	        });
	    MenuItem OU = new MenuItem("Shift Octave Up");
	        OU.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		Slave.shiftOctave(1);
	        	}
	        });
	    MenuItem OD = new MenuItem("Shift Octave Down");
	    	OD.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		Slave.shiftOctave(-1);
	        	}
	        });
	    menuPlay.getItems().addAll(Pl, St, OU, OD);
	}

	private void setupHelpMenu(Menu menuHelp) {
		MenuItem about = new MenuItem("About");
			about.setOnAction(new EventHandler<ActionEvent>() {
		    	public void handle(ActionEvent t) {		}
		    });
		MenuItem com = new MenuItem("Console Commands");
		    com.setOnAction(new EventHandler<ActionEvent>() {
		    	public void handle(ActionEvent t) {		}
		    });
		MenuItem FAQs = new MenuItem("FAQs");
		    FAQs.setOnAction(new EventHandler<ActionEvent>() {
		    	public void handle(ActionEvent t) {		}
		    });
		MenuItem controls = new MenuItem("Controls");
			controls.setOnAction(new EventHandler<ActionEvent>() {
		    	public void handle(ActionEvent t) {		}
		    });
		menuHelp.getItems().addAll(about, com, FAQs, controls);

	}

	private void doPopUp(){

		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setOpacity(1);
		stage.setTitle("MIDIAllocator");
		GridPane GPane = new GridPane();
		stage.setScene(new Scene(GPane));
			//#################
			//Internal elems
			Label openingLabel = new Label("Welcome to MIDIAllocator!");
			//
			//Button for creating a new configuration
			Button newConfigBtn = new Button("New Configuration");
			newConfigBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					doSetupWindow();}});
			//
			//Button for loading an existing config
			Button loadConfigBtn = new Button("Load Configuration");
			loadConfigBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						File fi = fileChooser("Select a Configuration File");

						if(fi != null){
							Slave.parse(fi);
							stage.close();
						}
					} catch (IOException | InvalidMidiDataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();}}});
			//
			//################
		GPane.add(openingLabel, 3, 0);
		GPane.add(newConfigBtn,2,3);
		GPane.add(loadConfigBtn,4,3);
		stage.showAndWait();
	}
	Button setupNextBtn;
	private int remainingStrings = 0;
	private void doSetupWindow() {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setOpacity(1);

		GridPane gPane = new GridPane();
		stage.setScene(new Scene(gPane));
		//----------------------
		//Name Label
		Label nameLbl = new Label("Config Name: ");
		//
		//Name TextInput
		TextField nameTxtFld =  new TextField();
		//Assign to GPane
		gPane.add(nameLbl, 1, 1);
		gPane.add(nameTxtFld, 2, 1);
		//
		//-----------------------

		//-----------------------
		//Preposition Label
		Label prepositionLabel =  new Label("Preposition Length: ");
		//
		//Preposition TextField
		TextField prepositionTxtFld = new TextField();
		//Assign the gPane
		gPane.add(prepositionLabel, 1, 2);
		gPane.add(prepositionTxtFld, 2, 2);
		//
		//----------------------

		//----------------------
		//Preposition delay
		Label prepositionDelayLabel =  new Label("Preposition Delay: ");
		//
		//Preposition delay TextField
		TextField PrepositionDelayTxtFld = new TextField();
		//Assign to the gPane
		gPane.add(prepositionDelayLabel, 1, 3);
		gPane.add(PrepositionDelayTxtFld, 2, 3);
		//
		//----------------------

		//----------------------
		//Number of Strings
		Label numberOfStringsLbl = new Label("Number of Strings: ");
		//
		//Number of strings textField
		TextField numberOfStringsTxtFld =  new TextField();
		//
		//Add to gPane
		gPane.add(numberOfStringsLbl, 1, 4);
		gPane.add(numberOfStringsTxtFld,2,4);
		//
		//---------------------

		//---------------------
		//NextButton
		setupNextBtn = new Button("Next");
		setupNextBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Slave.setName(             nameTxtFld.                             getText()  );
				Slave.setPrepositionLength(Long.parseLong  (prepositionTxtFld.     getText() ));
				Slave.setPrepositionDelay( Long.parseLong  (PrepositionDelayTxtFld.getText() ));
				Slave.setNumberOfStrings(  Integer.parseInt(numberOfStringsTxtFld. getText() ));
				remainingStrings = Integer.parseInt(numberOfStringsTxtFld.getText());
				if(remainingStrings > 0){
					MekStringWindow();
					((Stage)((Button)event.getSource()).getScene().getWindow()).close();
				}
			}
		});
		//Add to gPane
		gPane.add(setupNextBtn, 2, 5);
		stage.showAndWait();
	}

	private void MekStringWindow() {


	}
	int stringsToDefine;
	int stringsNumber;
	String saveFileName = " ";
	ComboBox<Integer> BassTrackComboBox;

	private GridPane buildLeftGUI() {
		GridPane GPane = new GridPane();

		//---
		//Default Font for text input
		Font defaultFont = new Font("FreeSans", 20);
		//---


		//----------------------------------
		//Define name input entry
		//
		Label nameLabel = new Label("File Name: ");
		nameLabel.setFont(defaultFont);
		//
		//The input Field (String Based)
		TextField nameTextField =  new TextField();
		nameTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				handleNameKeyEvent(event);}});
		//
		//--------------------------------


		//--------------------------------
		//Define BassTrack selection entry
		//
		Label bassTrackLabel = new Label("Select Bass Track: ");
		bassTrackLabel.setFont(defaultFont);
		//TODO Add to GPane
		//The inputfield, a combobox. Box is enumerated with an observableList with each of the tracks available
		//If no file is loaded, only option is zero.
		//Due to changable loaded files, the comboBox must be externalised.
		BassTrackComboBox = new ComboBox<Integer>();
		BassTrackComboBox.setItems(populateComboBox());
		BassTrackComboBox.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				if(event.getSource() instanceof ComboBox){
					Slave.setBassTrack(((ComboBox<Integer>) event.getSource()).getValue());
				}
			}
		});
		//
		//--------------------------------




		//Input the number of strings, this will notify the main pane that Strings still need to be defined
		Label stringLabel = new Label("Input Number of Strings: ");
		stringLabel.setFont(defaultFont);

		TextField stringsTextField = new TextField();
		stringsTextField.setUserData("TextString");
		stringsTextField.setOnKeyReleased(new EventHandler<KeyEvent>(){
			@Override
			public void handle(KeyEvent event) {
				handleNameKeyEvent(event);}});

		//Add all above elems to the GridPane
		GPane.add(nameLabel, 0, 0);
		GPane.add(nameTextField, 1, 0);
		GPane.add(bassTrackLabel,0,1);
		GPane.add(BassTrackComboBox, 1, 1);
		GPane.add(stringLabel, 0, 2);
		GPane.add(stringsTextField,1,2);



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

	/**
	 * Returns an Observable list of integers.
	 * Integers are derived from the current loaded MIDI file. Range is 0 - number of tracks.
	 * Forced externalisation of the population method for a combobox, due to the current MIDI being a changable property.
	 *
	 * @return
	 */
	private ObservableList<Integer> populateComboBox(){
		ObservableList<Integer> options;
		if(Slave.getSequence() != null){

			options = FXCollections.observableArrayList();

			for(int i = 0; i < Slave.getSequence().getTracks().length; i++){
				options.add(i);
			}
		}
		else{

			options = FXCollections.observableArrayList(0);
		}
		return options;
	}

	protected void handleNameKeyEvent(KeyEvent event) {
		//TODO
//		// Get the source of the event (Object) cast to TextArea get the userdata, cast it to String and check if the name is valid for this method
//		String checkName = (
//								(
//									(String)(
//										(TextField)event.getSource()
//									).getUserData()
//								)
//							);
//		if(checkName.equals("TextString"))
//		switch (event.getCode() +"") {
//		case "ENTER":
//			stringsNumber = stringsToDefine = Integer.parseInt(((TextField) event.getSource()).getText());
//			break;
//
//		default:
//			break;
//		}
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
			public void handle(ActionEvent event){setCurrentMIDI();
			//Remove and re-add the eventhandler, this is to avoid it being called upon changing the contents of the combobox
			EventHandler<ActionEvent> temp = BassTrackComboBox.getOnAction();
			BassTrackComboBox.setOnAction(null);
			BassTrackComboBox.setItems(populateComboBox());
			BassTrackComboBox.setOnAction(temp);}});

		Button saveBtn = new Button();//The Save Button
		saveBtn.setText("Save");

		saveBtn.setOnAction(new EventHandler<ActionEvent>() {//when pushed
			//call to saveCurMIDI
			@Override
			public void handle(ActionEvent event){save();}});

		Button TsaveBtn = new Button();//The Save Button
		TsaveBtn.setText("TSave");

		Button solveBtn = new Button();//The Solve Button
		solveBtn.setText("Solve");

		solveBtn.setOnAction(new EventHandler<ActionEvent>() {
			//Call to solve
			@Override
			public void handle(ActionEvent event) {solve();}});

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
				slave.setSequence(MidiSystem.getSequence(fi));
				sim.setSequence(slave.getSequence());
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
		timer.cancel();
	}


	public TextArea getConsoleTextArea(){
		return textConsole;
	}


	private void handleConsoleKeyEvent(KeyEvent event){
			switch (event.getCode() +"") { //added to the empty string for implicit conversion
			case "ENTER":
				slave.getConsole().read(textConsole.getText());
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
			Console console = new Console(slave);
			slave.setConsole(console);
		}
	}


}
