package ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import com.bv_gruppe_d.imagej.ImageData;
import ij.IJ;
import javafx.fxml.FXML;
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