package ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import com.bv_gruppe_d.imagej.ImageData;
import ij.IJ;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * Provides methods for user inputs from the UserInterfaceView generated MainPage.fxml
 */
public class UserInterfaceControler {
	
	private ArrayList<ImageData> trainingsData;
	private ArrayList<ImageData> testData;
	private ImageData evalutationImage;
	
	@FXML
	private ImageView evaluationImageView;
	
	/**
	 * Takes a directory from the user and maps the images in the subfolders to labled 
	 * ImageData objects representing the trainings data for a classifier.
	 */
	@FXML
	private void readLabledTrainingsData() {
		File upperDirectory = getDirectoryFromUser();		
		trainingsData = ImageDataCreator.getLabledImageData(upperDirectory);
	}
	
	/**
	 * Takes a directory from the user and maps the images in the subfolders to labled 
	 * ImageData objects representing the test data for a classifier.
	 */
	@FXML
	private void readLabledTestData() {
		File upperDirectory = getDirectoryFromUser();		
		testData = ImageDataCreator.getLabledImageData(upperDirectory);
	}
	
	/**
	 * Takes a file path from the user to map the image to an unlabled ImageData object
	 * for individual classification.
	 */
	@FXML
	private void evaluateImage() {
		File selectedFile = getImageFileFromUser();
		
		try {
			evalutationImage = ImageDataCreator.getImageData(selectedFile);
			
			URL url = selectedFile.toURI().toURL();
			evaluationImageView.setImage(new Image(url.toExternalForm()));
		} catch (IOException e) {
			IJ.showMessage(e.getMessage());
		}
	}
	
	
	@FXML
	private void testClassifier() {
		if (testData == null) {
			showNoTestDataDialog();
		} else {
			showNoClassifierDialog();
		}
	}

	private void showNoClassifierDialog() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Keine Klassifizierer gefunden");
		alert.setHeaderText("Trainieren Sie zunächst einen Klassifizierer");
		alert.setContentText("Um einen Klassifizierer zu Trainieren, lesen Sie im obigen Bereich Testdaten ein und betätigen "
				+ "anschließend die Schaltfläche zum Trainieren eines Klassifizierers.");

		alert.showAndWait();
	}

	private void showNoTestDataDialog() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Keine Testdaten gefunden");
		alert.setHeaderText("Bitte lesen Sie zunächst Testdaten ein.");
		alert.setContentText("Um Testdaten einzulesen schließen Sie bitte diesen Dialog "
				+ "und betätigen Sie die Schaltfläche oberhalb dieses Buttons.");

		alert.showAndWait();
	}

	
	
	@FXML
	private void trainClassifier() {
		showNoTrainingsDataDialog();
	}

	private void showNoTrainingsDataDialog() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Keine Trainingsdaten gefunden.");
		alert.setHeaderText("Bitte lesen Sie zunächst Testdaten ein.");
		alert.setContentText("Um Trainingsdaten einzulesen schließen Sie bitte diesen Dialog "
				+ "und betätigen Sie die Schaltfläche oberhalb dieses Buttons.");

		alert.showAndWait();
	}
	
	/**
	 * Displays a directory chooser dialog for the user.
	 * @return	The path to the selected directory.
	 */
	private File getDirectoryFromUser() {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
	    return directoryChooser.showDialog(null);
	}
	
	/**
	 * Displays a file chooser dialog for the user where only .jpg and .png files can be selected.
	 * @return The path to the selected file
	 */
	private File getImageFileFromUser() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png");
		fileChooser.getExtensionFilters().add(imageFilter);
		return fileChooser.showOpenDialog(null);
	}
}