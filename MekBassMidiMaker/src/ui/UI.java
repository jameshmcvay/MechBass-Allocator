package ui;

import java.io.File;
import java.util.List;
import java.util.logging.FileHandler;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Catch all class for activating UI on the fly, typically expect static fire and forget methods
 *
 * @author macdondyla1, oswaldgreg
 *
 */
public class UI extends Application {
	//The file to be modified
	private File currentMIDI = null;
	@Override
	public void start(Stage stage) throws Exception {

		//the outside bounds of the screen
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

		//set the stages bounds to that of the screen
		stage.setX(screenBounds.getMinX());
		stage.setY(screenBounds.getMinY());
		stage.setWidth(screenBounds.getWidth());
		stage.setHeight(screenBounds.getHeight());

		//a layout manager
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT); //bind the layout to top left
		grid.setVgap(10); //10px vertical gap between elems
		grid.setHgap(10); //10px horizontal gap between elems
		//Grid edge padding
		grid.setPadding(new Insets(10, 10, 10, 10));

		//Contains scene graph, in this case it essentially acts as an
		//interface between grid and stage
		Scene scene = new Scene(grid);

		//Create a new button
		Button selectFile = new Button("Open MIDI");
		//On being clicked, the button opens a file selection dialog
		selectFile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//new file choice dialog
				FileChooser chooseFileDialog =  new FileChooser();
				//set a title for the dialog
				chooseFileDialog.setTitle("Select MIDI File");
				//assign the chosen file to be the MIDI we're working on.
				currentMIDI = chooseFileDialog.showOpenDialog(stage);
				System.out.println(currentMIDI.getName());
			}
		});

		grid.add(selectFile, 0, 0);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args){
		launch(args);
	}

}