package ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import com.bv_gruppe_d.imagej.ImageData;
import com.bv_gruppe_d.imagej.Lable;
import classification.Classificator;
import classification.FeatureExtractor;
import classification.FeatureVector;
import classification.HoughTransformation;
import ij.IJ;
import ij.process.ImageProcessor;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import preprocessing.Binarization;
import preprocessing.MorphologicalFiltering;

/**
 * Provides methods for user inputs from the UserInterfaceView generated
 * MainPage.fxml
 */
public class UserInterfaceControler {

	private ArrayList<ImageData> trainingsData;
	private ArrayList<ImageData> testData;
	private ImageData evalutationImage;
	private Classificator classificator;

	/**
	 * The ImageView object that is displayed on the MainPage.fxml
	 */
	@FXML
	private ImageView evaluationImageView;

	/**
	 * The ScatterChart object that is displayed on the MainPage.fxml
	 */
	@FXML
	private ScatterChart<Number, Number> scatterChart;

	/**
	 * Takes a directory from the user and maps the images in the subfolders to
	 * labled ImageData objects representing the trainings data for a classifier.
	 */
	@FXML
	private void readLabledTrainingsData() {
		File upperDirectory = getDirectoryFromUser();
		trainingsData = ImageDataCreator.getLabledImageData(upperDirectory);
	}

	/**
	 * Takes a directory from the user and maps the images in the subfolders to
	 * labled ImageData objects representing the test data for a classifier.
	 */
	@FXML
	private void readLabledTestData() {
		File upperDirectory = getDirectoryFromUser();
		testData = ImageDataCreator.getLabledImageData(upperDirectory);
	}

	/**
	 * Takes a file path from the user to map the image to an unlabled ImageData
	 * object for individual classification.
	 */
	@FXML
	private void evaluateImage() {
		File selectedFile = getImageFileFromUser();

		try {
			evalutationImage = ImageDataCreator.getImageData(selectedFile);

			URL url = selectedFile.toURI().toURL();
			evaluationImageView.setImage(new Image(url.toExternalForm()));

			if (classificator == null) {
				showNoClassifierDialog();
			} else {
				ArrayList<ImageData> images = new ArrayList<>(1);
				images.add(evalutationImage);
				FeatureVector[] testVectors = generateFeatureVectors(images);
				Lable result = classificator.testClassifier(testVectors[0]);
				IJ.showMessage("Ergebnis: " + result);
			}
		} catch (IOException e) {
			IJ.showMessage(e.getMessage());
		}
	}

	/**
	 * TODO: Add comment after all logic is implemented.
	 */
	@FXML
	private void testClassifier() {
		if (testData == null) {
			showNoTestDataDialog();
		} else if (classificator == null) {
			showNoClassifierDialog();
		} else {
			// TODO: Add code for testing
			FeatureVector[] testVectors = generateFeatureVectors(testData);
			StringBuilder sb = new StringBuilder();
			for (FeatureVector featureVector : testVectors) {
				Lable result = classificator.testClassifier(featureVector);
				sb.append("Vorgabe: " + featureVector.getLable().toString() + " - Ergebnis: " + result.toString()
						+ "\r\n");
			}
			IJ.showMessage(sb.toString());
		}
	}

	/**
	 * Displays a dialog to the user to notify him that no classifier exists yet.
	 */
	private void showNoClassifierDialog() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Keine Klassifizierer gefunden");
		alert.setHeaderText("Trainieren Sie zunächst einen Klassifizierer");
		alert.setContentText(
				"Um einen Klassifizierer zu Trainieren, lesen Sie im obigen Bereich Testdaten ein und betätigen "
						+ "anschließend die Schaltfläche zum Trainieren eines Klassifizierers.");

		alert.showAndWait();
	}

	/**
	 * Displays a dialog to the user to notify him that no data for testing exists
	 * yet.
	 */
	private void showNoTestDataDialog() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Keine Testdaten gefunden");
		alert.setHeaderText("Bitte lesen Sie zunächst Testdaten ein.");
		alert.setContentText("Um Testdaten einzulesen schließen Sie bitte diesen Dialog "
				+ "und betätigen Sie die Schaltfläche oberhalb dieses Buttons.");

		alert.showAndWait();
	}

	/**
	 * TODO: Add comment after all logic is added
	 */
	@FXML
	private void trainClassifier() {
		if (trainingsData == null) {
			showNoTrainingsDataDialog();
		} else {
			FeatureVector[] vectors = generateFeatureVectors(trainingsData);
			classificator = new Classificator();
			classificator.learnClassifier(vectors);
			new FabricClassificationScatterChartPopulator(scatterChart).populateScatterChartWithData(vectors);
		}
	}

	/**
	 * Displays a dialog to the user to notify him that no data for training exists
	 * yet.
	 */
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
	 * 
	 * @return The path to the selected directory.
	 */
	private File getDirectoryFromUser() {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		return directoryChooser.showDialog(null);
	}

	/**
	 * Displays a file chooser dialog for the user where only .jpg and .png files
	 * can be selected.
	 * 
	 * @return The path to the selected file
	 */
	private File getImageFileFromUser() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png");
		fileChooser.getExtensionFilters().add(imageFilter);
		return fileChooser.showOpenDialog(null);
	}

	/**
	 * @return Returns an set of feature vectors corresponding to the given images
	 */
	private static FeatureVector[] generateFeatureVectors(ArrayList<ImageData> images) {
		HoughTransformation ht = new HoughTransformation(2, 1.0, 4, 100);
		FeatureExtractor fe = new FeatureExtractor();
		Binarization bin = new Binarization();
		MorphologicalFiltering mf = new MorphologicalFiltering();

		FeatureVector[] vectors = new FeatureVector[images.size()];
		for (int i = 0; i < vectors.length; ++i) {
			ImageData image = images.get(i).duplicate();// do not modify the original
			// Processing the whole image takes waaaay toooo looong for testing, so we just
			// use a part
			// this might screw up the error detection
			image.getImageProcessor().setRoi(0, 0, 200, 200);
			image = new ImageData(image.getImageProcessor().crop(), image.getLable());

			// TODO @Daniel your code here: is this correct?
			// Preprocessing
			bin.execute(image);
			mf.execute(image);

			// HoughTransformation and feature extraction
			vectors[i] = fe.execute(image, ht.execute(image));
		}
		return vectors;

		/*
		 * // FOR DEBUGGING PURPOSE ONLY // Returns an example set of feature vectors //
		 * TODO: Remove after application is sufficiently tested.
		 * 
		 * ArrayList<FeatureVector> exampleVectors = new ArrayList<>( Arrays.asList(new
		 * FeatureVector(new double[] { 1, 1 }, Lable.NO_STRETCH), new FeatureVector(new
		 * double[] { 1.05, 0.95 }, Lable.NO_STRETCH), new FeatureVector(new double[] {
		 * 2, 1 }, Lable.MEDIUM_STRETCH), new FeatureVector(new double[] { 2.05, 0.95 },
		 * Lable.MEDIUM_STRETCH), new FeatureVector(new double[] { 3, 1 },
		 * Lable.MAXIMUM_STRECH), new FeatureVector(new double[] { 3.05, 0.95 },
		 * Lable.MAXIMUM_STRECH), new FeatureVector(new double[] { 1.05, 0.2 },
		 * Lable.DISTURBANCE), new FeatureVector(new double[] { 1, 0.1 },
		 * Lable.DISTURBANCE), new FeatureVector(new double[] { 0.5, 1.5 },
		 * Lable.SHEARD), new FeatureVector(new double[] { 0.5, 1.95 }, Lable.SHEARD)
		 * 
		 * )); return exampleVectors.toArray(new FeatureVector[] {});//
		 */
	}
}