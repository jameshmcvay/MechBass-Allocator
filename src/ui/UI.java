package ui;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;

import main.Parser3;
import solver.MekString;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * GUI for the mekbassSolver algorithm.
 * Written in javafx.
 *
 * @link http://docs.oracle.com/javase/8/javase-clienttechnologies.htm
 *
 * @author macdondyla1, oswaldgreg
 *
 */
public class UI extends Application{

	//4:3 screen ratio
	double width = 1200;
	double height = 900;

	//ConsoleArguments
	protected static String args[];

	//Console I/O fields
	TextArea textOutputConsole = null;
	TextField textInputConsole = null;

	//The main animated display
	Simulation sim;
	//Timer for redraw
	Timer timer;
	int timerTime = 1000/60;

	String saveFileName = " ";
	//dropdown box of tracks
	ComboBox<String> BassTrackComboBox;
	//default UI font
	Font defaultFont = new Font("FreeSans", 20);
	//Forced field
	Button setupNextBtn;

	//-----Temp fields for setting new config-------------
	String tempName = "";
	Long tempPrepositionLength;
	Long tempPrepositionDelay;
	int tempNumberOfStrings;
	//-----------------------------------------------------

	/**
	 * Primary method for the GUI performs all setup tasks
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		Slave.parse(new File("default.csv")); //Set Slave to have a default mekbass Setup

		primaryStage.setResizable(false);

		//-----Create the set of buttons to be added to the graphics pane-------
		FlowPane buttonPanel = buildRightPanel();

		//Layout manager
		GridPane gridPane = new GridPane();

		//Initialise console elements
		textInputConsole = new TextField();
		textOutputConsole =  new TextArea();

		//arbirtary width for animated canvas (3/4 of set width)
		double leftCanvasWidth = width * (3.0 / 4.0);
		//arbitrary height for animated canvas
		double canvasHeight = height * 0.667;


		//-----Setup methods for the window-----
		textOutputConsole.setPrefHeight(height * 0.332);
		textOutputConsole.setEditable(false);

		Canvas leftCanvas = new Canvas();
		leftCanvas.setWidth(leftCanvasWidth);
		leftCanvas.setHeight(canvasHeight);

		double rightCanvasWidth = width * (1.0 / 4.0);
		Canvas rightCanvas = new Canvas();

		rightCanvas.setWidth(rightCanvasWidth);
		rightCanvas.setHeight(canvasHeight);

		/*
		 * Deans menu bar stuff
		 *
		 * http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm
		 * (Starting Point)
		 *
		 * */
		MenuBar menuBar = setupMenuBar();
		buttonPanel.autosize();
		buttonPanel.alignmentProperty().setValue(Pos.TOP_RIGHT);
		//----------End of Deans menu bar stuff ------------


		//-----------GridPane-----------
			//add elems
				gridPane.add(menuBar,0,0);
				gridPane.add(leftCanvas, 0, 1);
				gridPane.add(textOutputConsole, 0, 2);
				gridPane.add(textInputConsole, 0, 3);
				gridPane.add(buttonPanel, 0, 1);
			//---------
			//GridPane Constraints
				GridPane.setHgrow(textOutputConsole, Priority.ALWAYS);
				GridPane.setHgrow(textInputConsole, Priority.ALWAYS);
			//---------
		//------------------------------

		//Initialise the scene
		Scene scene =  new Scene(gridPane,width,height);

		//-----keyhandler-----
	    textInputConsole.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				handleConsoleKeyEvent(event);
			}
		});
	    //--------------------

	    Slave.setUI(this);//give slave the UI

	    Console console = new Console(getConsoleTextInput(),getConsoleOutput()); //Initialise the console

		Slave.setConsole(console); //give Slave the console

		//-----Rebind printstream to Console-------
	    PrintStream ps = new PrintStream(console, true);
	    System.setOut(ps);
	    System.setErr(ps);
	    //-----------------------------------------

	    textInputConsole.requestFocus(); //start with input console selected

	    primaryStage.setTitle("Etemenanki");//title
	    primaryStage.setScene(scene); //add scene to stage

	    primaryStage.show(); //display

	    sim = new Simulation(); //initialise simulation
	    Slave.setSim(sim);

	 // set a run loop
	    timer = new java.util.Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
		    public void run() {
		         Platform.runLater(new Runnable() {
		            public void run() {
		            	if (sim.isPlaying()) {
		            		sim.tick();
//		            		sim.addTime(timerTime);
//		            		sim.addDrawStartTime(timerTime);
		            	}
		            	sim.draw(leftCanvas.getGraphicsContext2D(), 0.5);

//		            	sim.addDrawStartTime(14);
		            }
		        });
		    }
		}, timerTime, timerTime);
	}


	private MenuBar setupMenuBar(){
		/*
		 * DEAN MAKING A MENU BAR:
		 *
		 * http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm
		 * (Starting Point)
		 *
		 * */
		MenuBar menuBar = new MenuBar(); // The Bar where "| File | Edit | View |" will be
//		menuBar.setMinWidth(width * (2/3));
//		menuBar.setMaxWidth(width * (2/3));
        // --- Menu File
        Menu menuFile = new Menu("File");
        setupFileMenu(menuFile);
        // --- Menu Edit
        Menu menuPlay = new Menu("Playback");
        setupEditMenu(menuPlay);
        // --- Menu View
        Menu menuHelp = new Menu("Help");
        setupHelpMenu(menuHelp);

        menuBar.getMenus().addAll(menuFile, menuPlay, menuHelp);

//        VBox vbox = new VBox(menuBar);
        return menuBar;
//        ((GridPane) scene.getRoot()).getChildren().addAll(vbox);
	}

	private void setupFileMenu(Menu menuFile){
		MenuItem NC = new MenuItem("New Config");
	        NC.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		doSetupWindow();
	        	}
	        });
        MenuItem OC = new MenuItem("Open Config");
        OC.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent t) {
					try {
						File fi = fileChooser("Select a Configuration File");

						if(fi != null){
							Slave.parse(fi);
						}
					} catch (IOException | InvalidMidiDataException e) {
						e.printStackTrace();}}
        	});
        MenuItem SC = new MenuItem("Save Config");
        SC.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent t) {	saveConfig();	}
        	});
	    MenuItem OM = new MenuItem("Open MIDI File");
	        OM.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		setCurrentMIDI();
	    			//Remove and re-add the eventhandler, this is to avoid it
	        		//being called upon changing the contents of the combobox
	    			EventHandler<ActionEvent> temp = BassTrackComboBox.getOnAction();
	    			BassTrackComboBox.setOnAction(null);
	    			BassTrackComboBox.setItems(populateTrackNumberComboBox());
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
	    menuFile.getItems().addAll(NC, OC, SC, OM, SaM, SoM, Q);
	}

	private void setupEditMenu(Menu menuPlay) {
		MenuItem Pl = new MenuItem("Play");
	        Pl.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		play();
	        	}
	        });
	    MenuItem Pa = new MenuItem("Pause");
	        Pa.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent t) {
	        		pause();
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
	    menuPlay.getItems().addAll(Pl, Pa, St, OU, OD);
	}

	private void setupHelpMenu(Menu menuHelp) {
		MenuItem about = new MenuItem("About");
			about.setOnAction(new EventHandler<ActionEvent>() {
		    	public void handle(ActionEvent t) {
		    		helpPopUps(0);
		    	}
		    });
		MenuItem com = new MenuItem("Console Commands");
		    com.setOnAction(new EventHandler<ActionEvent>() {
		    	public void handle(ActionEvent t) {
		    		helpPopUps(1);
		    	}
		    });
		MenuItem FAQs = new MenuItem("FAQs");
		    FAQs.setOnAction(new EventHandler<ActionEvent>() {
		    	public void handle(ActionEvent t) {
		    		helpPopUps(2);
		    	}
		    });
		MenuItem controls = new MenuItem("Other Controls");
			controls.setOnAction(new EventHandler<ActionEvent>() {
		    	public void handle(ActionEvent t) {
		    		helpPopUps(3);
		    	}
		    });
		menuHelp.getItems().addAll(about, com, FAQs, controls);

	}

	private void helpPopUps(int type){
		// Set the Scene, and the stage.
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setOpacity(1);
		GridPane GPane = new GridPane();
		stage.setScene(new Scene(GPane));
		TextArea text = new TextArea();
		GPane.add(text, 0, 0);
		stage.setResizable(false);
		// Decide what help to give the user.
		switch (type){
		case 0:
			// Set text.
			stage.setTitle("About the MIDIAllocator");
			text.setText
			("This program was made at Victoria University for James, the "
		   + "Creator of the MechBass. It was made by Andrew Palmer, Elliot "
		   + "Wilde, Patrick Byers, Dean Newberry and Dylan Macdonald, with "
		   + "early input from Greg Oswold, over the course of 12 weeks as part"
		   + "of a course teaching Agile Programming Methodology.\n\n"
		   + "The Purpose of this program is to strip the bass track out of a "
		   + "MIDI file, then move the notes to different strings, such that it"
		   + "can be played by James's MechBass.");

			// Done.
			break;

		case 1:
			stage.setTitle("Console Commands");
			// Set text.
			text.setText
			("Console Commands:\nThese commands are case sensitive:\n\n"
		   + ">>> d: Sets the configuration to default - perfect for hysteria "
		   + "by Muse.\n"
	       + ">>> open: Opens a MIDI file (specify filepath and name of file "
	       + "after open).\n"
           + ">>> openConfig: Opens a config file (specify filepath and name "
           + "of file after openConfig).\n"
           + ">>> solve: Works the magic of the program to let the MIDI file "
           + "play on MechBass.\n"
           + ">>> play: Plays the current MIDI file.\n"
           + ">>> stop: Stops the current MIDI file.\n"
           + ">>> octUp: Shifts the Octave of all the notes in the current MIDI"
           + "file up by 3.\n"
           + ">>> octDown: Shifts the Octave of all the notes in the current "
           + "MIDI file down by 3.\n"
           + ">>> save: Saves the current MIDI file.\n"
           + ">>> saveConfig: Saves the current configuration.\n"
           + ">>> setup: ???\n"
           + ">>> config: ???\n"
           + ">>> help: Slightly less useful help.\n"
           + ">>> END: Ends the Program.");

			// Done.
			break;

		case 2:
			// Set text.
			stage.setTitle("FAQs");
			text.setText
			("Q: Why did you make this program?\nA: Because James, the Creator "
		   + "of MechBass has to painstakingly rearrange MIDI files by hand if "
		   + "he wants to make a song play on MechBass. This way, with this "
		   + "program he only has to alter a small number of notes to make the "
		   + "song play, leaving more time for James to do... James stuff.\n\n"

		   + "Q: What is MechBass?\nA: MechBass is a robot bass guitar. Yes, it"
		   + "is *exactly* as awesome as it sounds. It was created by James, "
		   + "and it is a fairly prominent fixture at most Open Days, used to "
		   + "show off the Faculty of Engineering.\n\n"

		   + "Q: What songs can MechBass play?\nA: Hysteria and Knights of "
		   + "Cydonia by Muse definitely - the videos for them are on Youtube. "
		   + "But with the creation of this program, we can (hopefully) expect "
		   + "a lot more songs!\n\n"

		   + "Q: Why is there there so many options to set Strings?\nA: THIS "
		   + "program was originally meant to strip out the Bass track of a "
		   + "MIDI file so MechBass could play it. However, in the future, "
		   + "James may decide to make a different Mech-String-Instrument; if "
		   + "this happens, the program should be ready to be used accordingly.");

			// Done.
			break;

		case 3:
			// Set text.
			stage.setTitle("Other Controls");
			text.setText
			("NOT THE MENU BAR:\n\n==========\n\n"
		   + "File Name: This is what your file will be called when you save "
		   + "it.\n\n"

		   + "Select Bass Track: The Track you will gut out of the MIDI file."
		   + "if there is no MIDI file loaded, there will only be track 0.\n\n "

		   + "Input Number of Strings: The number of Strings your "
		   + "Mech-Instrument has. This is '4' for MechBass.\n\n"

		   + "Play: Plays the current MIDI file.\n\n"

		   + "Stop: Stops the current MIDI file.\n\n"

		   + "Save: Saves the current MIDI file.\n\n"

		   + "Load: Loads a MIDI file.\n\n"

		   + "Solve: Guts the Selected Track out of the current MIDI file and "
		   + "distributes them across a number of tracks equal to the number of"
		   + "strings you have.\n\n==========\n\n"

		   + "THE MENU BAR:\n\n==========\n\n"

		   + "File ==> New Config: Allows you to more rigorously setup the "
		   + "program.\n\n"

		   + "File ==> Open MIDI File: Loads a MIDI file.\n\n"

		   + "File ==> Save MIDI File: Saves the current MIDI file.\n\n"

		   + "File ==> Solve MIDI File: Guts the Selected Track out of the "
		   + "current MIDI file and distributes them across a number of tracks "
		   + "equal to the number of strings you have.\n\n"

		   + "File ==> Quit: ...Quits the program..."

		   + "Playback ==> Play: Plays the current MIDI file.\n\n"

		   + "Playback ==> Stop: Stops the current MIDI file.\n\n"

		   + "Playback ==> Shift Octave Up: Shifts the Octave of all the notes "
		   + "in the current MIDI file up by 1.\n\n"

		   + "Help ==> About: A quick blurb about this program, and "
		   + "why it was made.\n\n"

		   + "Help ==> Console Commands: The commands you can use on"
		   + "the Console.\n\n"

		   + "Help ==> FAQs: Questions I think people might ask me "
		   + "a lot. THIS MEANS WE HAVEN'T BEEN ASKED ENOUGH QUESTIONS FOR AN "
		   + "FAQ.\n\n"

		   + "Help ==> Other Controls: ... You can't be SERIOUS...\n"
		   + "YOU ARE HERE.");

			// Done
			break;

		default:
			stage.setTitle("WHAT TH- HOW!? WHY?! WHAT DID YOU DO TO ME!?"); //#dean
			text.setText("Please forgive me. For you, there is no help. No hope.");
			//system.out.println("Please forgive me. For you, there is no help. No hope.");
			break;
		}
		text.setEditable(false);
		text.setWrapText(true);
		text.setVisible(true);
		stage.showAndWait();
	}

	/**
	 * Creates the configuation setup window
	 *
	 * From this window a new mekbass would be defined with a given number of strings, notes and prepositions
	 */
	private void doSetupWindow() {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setOpacity(1);

		GridPane gPane = new GridPane();
		stage.setScene(new Scene(gPane));
		//---------------Button, Label and textfield definitions---------------
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
					tempName              = (nameTxtFld.                             getText()  );
					tempPrepositionLength = (Long.parseLong  (prepositionTxtFld.     getText() ));
					tempPrepositionDelay  = (Long.parseLong  (PrepositionDelayTxtFld.getText() ));
					tempNumberOfStrings   = (Integer.parseInt(numberOfStringsTxtFld. getText() ));
					int remainingStrings = tempNumberOfStrings;

					if(remainingStrings > 0){
						defineMekStringWindow(remainingStrings); /*This was required ((Stage)((Button)event.getSource()).getScene().getWindow()),*/
					}
					stage.close();
				}
			});
			//Add to gPane
			gPane.add(setupNextBtn, 2, 5);
		//----------------------------------------------------------------------------

		stage.show();
	}

	/**
	 * Builds and handles the mekstring definition window
	 * @param remainingStrings The number of Strings on the mekbass
	 */
	private void defineMekStringWindow(int remainingStrings) {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setOpacity(1);
		stage.setTitle("Define Your Strings");


		GridPane mekStringGPane = new GridPane();
		ScrollPane sc = new ScrollPane(mekStringGPane);
		BorderPane bPane = new BorderPane(sc);
		sc.setFitToHeight(true);
		sc.setFitToWidth(false);

		List<TextField> textFields = new ArrayList<TextField>();
		Label titleLabel, lowNoteLabel, highNoteLabel, timingsLabel, endLineLabel;
		TextField highNoteTxtFld, lowNoteTxtFld, timingsTxtFld;
		String titleString = "Mekstring #";

		int gridYAxis = 0;
		int i = 0;
		while(remainingStrings > 0){
			titleLabel = new Label(titleString + i++);
			lowNoteLabel  = new Label("Lowest MIDI Note: "     );
			highNoteLabel = new Label("Highest MIDI Note: "    );
			timingsLabel  = new Label("Timings between notes: ");

			lowNoteTxtFld  =  new TextField();
			highNoteTxtFld =  new TextField();
			timingsTxtFld  =  new TextField("[1,2,2]");
			endLineLabel   = new Label("------------------");

			textFields.add(lowNoteTxtFld );
			textFields.add(highNoteTxtFld);
			textFields.add(timingsTxtFld );


			mekStringGPane.add(titleLabel    , 1 , gridYAxis++ );
			mekStringGPane.add(lowNoteLabel  , 1 , gridYAxis   );
			mekStringGPane.add(lowNoteTxtFld , 2 , gridYAxis++ );
			mekStringGPane.add(highNoteLabel , 1 , gridYAxis   );
			mekStringGPane.add(highNoteTxtFld, 2 , gridYAxis++ );
			mekStringGPane.add(timingsLabel  , 1 , gridYAxis   );
			mekStringGPane.add(timingsTxtFld , 2 , gridYAxis++ );
			mekStringGPane.add(endLineLabel  , 1 , gridYAxis++ );

			remainingStrings--;
		}

		Button but =  new Button();
		but.setText("Next");
		but.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				Slave.setName(tempName);
				Slave.setPrepositionDelay(tempPrepositionDelay);
				Slave.setPrepositionDelay(tempPrepositionLength);
				Slave.setNumberOfStrings(tempNumberOfStrings);

				int low = 0;
				int high = 0 ;
				long[] timings;
				for(int i = 0; i < textFields.size();i++){

					low = Integer.parseInt(textFields.get(i++).getText());
					high = Integer.parseInt(textFields.get(i++).getText());
					timings = new long[(high - low)];

					if(textFields.get(i).getText().length() != 0){
						int j = 0;
						for(String s : textFields.get(i).getText().split(",")){
							timings[j] = Long.parseLong(s);
						}

						Slave.addToMekString(new MekString(low, high, timings));
					}
					else{
						Slave.addToMekString(new MekString(low,high));
					}

				}
				stage.close();
			}
		});
		mekStringGPane.add(but, 2, gridYAxis);
		stage.setScene(new Scene(bPane,400, 300));
		stage.showAndWait();

	}


	/**
	 * Returns an Observable list of integers.
	 * Integers are derived from the current loaded MIDI file. Range is 0 - number of tracks.
	 * Forced externalisation of the population method for a combobox, due to the current MIDI being a changable property.
	 *
	 * @return
	 */
	private ObservableList<String> populateTrackNumberComboBox(){

		ObservableList<String> options;
		if(Slave.getSequence() != null){

			options = FXCollections.observableArrayList();
			Parser3 parser =  new Parser3(Slave.getSequence());
			for(int i = 0; i < Slave.getSequence().getTracks().length; i++){
				String out = "";
				out = parser.getTrackName(i);
				options.add(i + " - " + out);
			}
		}
		else{
			options = FXCollections.observableArrayList("Please upload a MIDI");
		}
		return options;
	}


	/**
	 * Creates the right hand panel and poplates it with buttons
	 * @return
	 */
	private FlowPane buildRightPanel(){
		double buttonMaxWidth = 300;
		double buttonMaxHeight = 100;
	    Button playBtn = new Button();//The play button
		playBtn.setText("Play");
		playBtn.setMaxWidth(buttonMaxWidth);
		playBtn.setMaxHeight(buttonMaxHeight);
		playBtn.setOnAction(new EventHandler<ActionEvent>() {//on push events
			//call to play
			@Override
			public void handle(ActionEvent event) {play();}});

		Button stpBtn = new Button();//The Stop Button
		stpBtn.setText("Stop");
		stpBtn.setMaxWidth(buttonMaxWidth);
		stpBtn.setMaxHeight(buttonMaxHeight);
		stpBtn.setOnAction(new EventHandler<ActionEvent>() {//when pushed
			//Call to playerStop
			@Override
			public void handle(ActionEvent event){playerStop();}});

		Button loadBtn = new Button();//The load Button
		loadBtn.setText("Load");
		loadBtn.setMaxWidth(buttonMaxWidth);
		loadBtn.setMaxHeight(buttonMaxHeight);
		loadBtn.setOnAction(new EventHandler<ActionEvent>() {//when pushed
			//call to setCurrMIDI
			@Override
			public void handle(ActionEvent event){load();}});

		Button saveBtn = new Button();//The Save Button
		saveBtn.setText("Save");
		saveBtn.setMaxWidth(buttonMaxWidth);
		saveBtn.setMaxHeight(buttonMaxHeight);
		saveBtn.setOnAction(new EventHandler<ActionEvent>() {//when pushed
			//call to saveCurMIDI
			@Override
			public void handle(ActionEvent event){save();}});

		Button solveBtn = new Button();//The Solve Button
		solveBtn.setText("Solve");
		solveBtn.setMaxWidth(buttonMaxWidth);
		solveBtn.setMaxHeight(buttonMaxHeight);
		solveBtn.setOnAction(new EventHandler<ActionEvent>() {
			//Call to solve
			@Override
			public void handle(ActionEvent event) {solve();}});

		Button pauseBtn = new Button();
		pauseBtn.setMaxWidth(buttonMaxWidth);
		pauseBtn.setText("Pause");
		pauseBtn.setMaxHeight(buttonMaxHeight);
		pauseBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {pause();}});

		Button octaveUpBtn = new Button();
		octaveUpBtn.setMaxWidth(buttonMaxWidth);
		octaveUpBtn.setText("Raise Octave");
		octaveUpBtn.setMaxHeight(buttonMaxHeight);
		octaveUpBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {octaveUp();}});

		Button octaveDownBtn = new Button();
		octaveDownBtn.setMaxWidth(buttonMaxWidth);
		octaveDownBtn.setText("Lower Octave");
		octaveDownBtn.setMaxHeight(buttonMaxHeight);
		octaveDownBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {octaveDown();}});

		Label bassTrackLabel = new Label("Select Bass Track: ");
		bassTrackLabel.setFont(defaultFont);
		//Add to GPane
		//The inputfield, a combobox. Box is enumerated with an observableList with each of the tracks available
		//If no file is loaded, only option is zero.
		//Due to changable loaded files, the comboBox must be externalised.
		BassTrackComboBox = new ComboBox<String>();
		BassTrackComboBox.setPromptText("Please Select a Bass Track");
		BassTrackComboBox.setMaxWidth(buttonMaxWidth);
		BassTrackComboBox.setMaxHeight(buttonMaxHeight);
		BassTrackComboBox.setItems(populateTrackNumberComboBox());
		BassTrackComboBox.setOnAction(new EventHandler<ActionEvent>() {
			@SuppressWarnings("unchecked")
			@Override
			public void handle(ActionEvent event) {
				// Auto-generated method stub
				if(event.getSource() instanceof ComboBox){
					try{
						Integer bassTrack = Integer.parseInt(((
								(ComboBox<String>) event.getSource()).getValue().charAt(0) + "")
								);
					Slave.setBassTrack(bassTrack);
					}
					catch(NumberFormatException e){
						load();
					}
				}
			}
		});

		Button cleanButton = new Button("Clean");
		cleanButton.setMaxWidth(buttonMaxWidth);
		cleanButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {clean();}});

		Button reloadBtn = new Button("Reload");
		reloadBtn.setMaxWidth(buttonMaxWidth);
		reloadBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				reload();

			}
		});


		FlowPane buttonPanel =  new FlowPane(Orientation.VERTICAL);
		buttonPanel.setColumnHalignment(HPos.LEFT);
		buttonPanel.setPadding(new Insets(3, 0, 0, 3));
		buttonPanel.getChildren().addAll(
				reloadBtn,
				playBtn,
				pauseBtn,
				stpBtn,
				saveBtn,
				loadBtn,
				solveBtn,
				cleanButton,
				octaveUpBtn,
				octaveDownBtn,
				BassTrackComboBox
				);

		return buttonPanel;
	}

	//--------------Button methods----------------------

	private void load() {
		setCurrentMIDI();
		//Remove and re-add the eventhandler, this is to avoid it being called upon changing the contents of the combobox
		EventHandler<ActionEvent> temp = BassTrackComboBox.getOnAction();
		BassTrackComboBox.setOnAction(null);
		BassTrackComboBox.setItems(populateTrackNumberComboBox());
		BassTrackComboBox.setOnAction(temp);
	}

	public void consoleLoad(String fileName) {
		currentFile = new File(fileName);

		try {
			Slave.setSequence(MidiSystem.getSequence(currentFile));
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}

		sim.setSequence(Slave.getSequence());
		//Remove and re-add the eventhandler, this is to avoid it being called upon changing the contents of the combobox
		EventHandler<ActionEvent> temp = BassTrackComboBox.getOnAction();
		BassTrackComboBox.setOnAction(null);
		BassTrackComboBox.setItems(populateTrackNumberComboBox());
		BassTrackComboBox.setOnAction(temp);
	}

	File lastFileLocation;
	File currentFile;

	//Load a new Sequence
	private void setCurrentMIDI(){
		try {
			currentFile = fileChooser("Select a MIDI file to open");

			if(currentFile != null){
				Slave.setSequence(MidiSystem.getSequence(currentFile));
				sim.setSequence(Slave.getSequence());
			}

		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}
	}

	private void solve() {
		Slave.solve();
	}

	private void pause() {
		Slave.pause();
	}

	private void playerStop() {
		Slave.playerStop();
	}

	private void play() {
		Slave.play();
	}

	private void octaveDown() {
		Slave.octaveDown();
		sim.setSequence(Slave.getSequence());
	}


	private void octaveUp() {
		Slave.octaveUp();
		sim.setSequence(Slave.getSequence());

	}

	private void clean() {
		Slave.clean();
	}


	private void save(){
		try {
			FileChooser dirChoo = new FileChooser();
			dirChoo.setTitle("Select Save Location");

			if(lastFileLocation != null){
				dirChoo.setInitialDirectory(lastFileLocation);
			}

			File fi = dirChoo.showSaveDialog(null);
			if(fi != null){
				lastFileLocation = fi.getCanonicalFile().getParentFile();
				Slave.save(fi);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//File selection menu pop up and handler
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


	private void reload() {
		try {
			playerStop();
			Slave.setSequence(MidiSystem.getSequence(currentFile));
			sim.setSequence(Slave.getSequence());
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}
	}

	protected void saveConfig(){
		File saveFile =  new FileChooser().showSaveDialog(null);
		if(saveFile != null){
			Slave.saveConfig(saveFile);
		}
	}

	//-------------------------------------------------------------------

	public TextField getConsoleTextInput(){
		return textInputConsole;
	}

	private TextArea getConsoleOutput() {
		return textOutputConsole;
	}

	//Javafx method, kills the thread, anything that you want to be disabled on closing the GUI *MUST* be in here
	@Override
	public void stop(){
		Slave.playerStop();
		Slave.playerRelease();
		timer.cancel();
	}


	private void handleConsoleKeyEvent(KeyEvent event){
			switch (event.getCode() +"") { //added to the empty string for implicit conversion
			case "ENTER":
				Slave.getConsole().read(textInputConsole.getText());
	            break;
			case "UP":
				Slave.getConsole().CallPrevious();
				break;
			case "DOWN":
				Slave.getConsole().callNext();
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
			Console console = new Console();
		}
	}
}
