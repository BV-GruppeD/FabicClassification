package ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.bv_gruppe_d.imagej.CsvInputOutput;
import com.bv_gruppe_d.imagej.ImageData;
import com.bv_gruppe_d.imagej.Lable;
import classification.Classificator;
import classification.EllipsisData;
import classification.FeatureExtractor;
import classification.FeatureVector;
import classification.HoughTransformation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import preprocessing.PreProcessing;

/**
 * Provides methods for user inputs from the UserInterfaceView generated
 * MainPage.fxml
 */
public class UserInterfaceControler {

	// Session variables
	private ArrayList<ImageData> trainingsData;
	private FeatureVector[] trainingsFeatureVectors;
	private ArrayList<ImageData> testData;
	private FeatureVector[] testFeatureVectors;
	private ImageData evalutationImage;
	private Classificator classificator;
	private ObservableList<String> featureDimensions;

	// Initialize instances of the image processing pipeline
	private final PreProcessing preprocessing = new PreProcessing();
	private final HoughTransformation houghTransformation = new HoughTransformation(2, 1.0, 4, 100);
	private final FeatureExtractor featureExtractor = new FeatureExtractor();

	// Objects displayed on the user interface
	@FXML
	private ImageView evaluationImageView;
	@FXML
	private ScatterChart<Number, Number> scatterChart;
	@FXML
	private ProgressBar trainingProgressBar;
	@FXML
	private ProgressBar testProgressBar;
	@FXML
	private ComboBox<String> yValuesPicker;
	@FXML
	private ComboBox<String> xValuesPicker;

	/**
	 * Takes a directory from the user and maps the images in the subfolders to
	 * labeled ImageData objects representing the training data for a classifier.
	 */
	@FXML
	private void readLabledTrainingsData() {
		File upperDirectory = getDirectoryFromUser();
		trainingsData = ImageDataCreator.getLabledImageData(upperDirectory);
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
	 * Takes a directory from the user and maps the images in the subfolders to
	 * labeled ImageData objects representing the test data for a classifier.
	 */
	@FXML
	private void readLabledTestData() {
		File upperDirectory = getDirectoryFromUser();
		testData = ImageDataCreator.getLabledImageData(upperDirectory);
	}

	/**
	 * Initializes the preprocessing of the provided Test Data. If no is provided
	 * checks if Feature Vectors for testing were loaded and provides notifications
	 * otherwise.
	 */
	@FXML
	private void testClassifier() {
		if (testData != null && classificator != null) {
			generateTestFeatures(testData);
		} else if (testFeatureVectors != null && classificator != null) {
			classifiyTestFeatureVectors();
		} else if (testData == null) {
			new Alert(AlertType.INFORMATION, "Bitte lesen Sie zunächst Testdaten ein.", ButtonType.OK).showAndWait();
		} else if (classificator == null) {
			new Alert(AlertType.INFORMATION, "Trainieren Sie zunächst einen Klassifizierer", ButtonType.OK)
					.showAndWait();
		} else {
			new Alert(AlertType.ERROR, "Bei der Bearbeitung ist leider ein Fehler aufgetreten", ButtonType.OK)
					.showAndWait();
		}
	}

	/**
	 * Starts a new Thread to generate the Feature Vectors for testing and to
	 * classify them afterwards.
	 * 
	 * @param images The images to process and test.
	 */
	private void generateTestFeatures(ArrayList<ImageData> images) {

		new Thread() {
			public void run() {
				testFeatureVectors = executeImageProcessingPipe(images, testProgressBar);
				Platform.runLater(() -> classifiyTestFeatureVectors());
			}
		}.start();
	}

	/**
	 * @return Returns a set of Feature Vectors corresponding to the given images.
	 */
	private FeatureVector[] executeImageProcessingPipe(ArrayList<ImageData> images, ProgressBar progress) {
		FeatureVector[] vectors = new FeatureVector[images.size()];

		for (int i = 0; i < vectors.length; ++i) {
			final double vectorNumber = (double) i;
			Platform.runLater(() -> progress.setProgress(vectorNumber / vectors.length));
			vectors[i] = generateFeatureVector(images.get(i));
		}
		return vectors;
	}

	/**
	 * Applies the image processing chain up to and including the feature extractor
	 * and returns the resulting FeatureVector
	 */
	private FeatureVector generateFeatureVector(ImageData image) {
		// do not modify the original
		ImageData processImage = image.duplicate();
		// Processing the whole image takes way too long for testing, so we just
		// use a part. This might negatively affect up the error detection
		processImage.getImageProcessor().setRoi(0, 0, 200, 200);
		processImage = new ImageData(processImage.getImageProcessor().crop(), processImage.getLable());

		preprocessing.execute(processImage);
		
		List<EllipsisData> ellipses = houghTransformation.execute(processImage);
		return featureExtractor.execute(processImage, ellipses);
	}
	
	/**
	 * Classifies all Feature Vectors for testing. Prompts the ratio of correct
	 * classification afterwards.
	 */
	private void classifiyTestFeatureVectors() {
		
		Lable[] results = new Lable[testFeatureVectors.length];
		for (int i = 0; i < testFeatureVectors.length; i++) {
			results[i] = classificator.testClassifier(testFeatureVectors[i]);
		}
		
		ResultAnalysis analysis = new ResultAnalysis();
		String formatedResults = analysis.getFormatedResultAnalysis(testFeatureVectors, results);
		
		Alert resultsBox = new Alert(AlertType.INFORMATION, formatedResults, ButtonType.OK);
		resultsBox.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		resultsBox.show();
	}

	/**
	 * Initializes the preprocessing of the provided Training Data. If no is
	 * provided checks if Feature Vectors for training were loaded and provides a
	 * notifications otherwise.
	 */
	@FXML
	private void trainClassifier() {
		if (trainingsData != null) {
			generateTrainingsFeatures(trainingsData);
		} else if (trainingsFeatureVectors != null) {
			createClassificator();
		} else {
			new Alert(AlertType.INFORMATION, "Bitte lesen Sie zunächst Testdaten ein.", ButtonType.OK).showAndWait();
		}
	}

	/**
	 * Starts a new Thread to generate the Feature Vectors for training and to
	 * create the classifier afterwards.
	 * 
	 * @param images The images to process and train with.
	 */
	private void generateTrainingsFeatures(ArrayList<ImageData> images) {
		new Thread() {
			public void run() {
				trainingsFeatureVectors = executeImageProcessingPipe(images, trainingProgressBar);
				Platform.runLater(() -> initializeScatterPlot());
				Platform.runLater(() -> createClassificator());
			}
		}.start();
	}

	/**
	 * Fills the DropDown menus for Feature selection and populates the scatter
	 * chart.
	 */
	private void initializeScatterPlot() {
		String[] dimensionNames = trainingsFeatureVectors[0].getFeatureNames();
		featureDimensions = FXCollections.observableArrayList(dimensionNames);

		xValuesPicker.setItems(featureDimensions);
		xValuesPicker.setValue(dimensionNames[0]);
		yValuesPicker.setItems(featureDimensions);
		yValuesPicker.setValue(dimensionNames[1]);

		FabricClassificationScatterChartPopulator populator = new FabricClassificationScatterChartPopulator(
				scatterChart);
		populator.setXIndex(0);
		populator.setYIndex(1);
		populator.populateScatterChart(trainingsFeatureVectors);
	}

	/**
	 * Creates and trains the classifier with the Training Feature Vectors.
	 */
	private final void createClassificator() {
		
		try {
			classificator = new Classificator();
			classificator.learnClassifier(trainingsFeatureVectors);
			new Alert(AlertType.INFORMATION, "Training abgeschlossen" + System.lineSeparator() 
			+ "nu = " + classificator.getNu() + "\r\nGamma = " + classificator.getGamma(), ButtonType.OK)
			.showAndWait();
		} catch (Exception e) {
			new Alert(AlertType.ERROR, "Leider ist beim lernen ein Fehler aufgetreten" + 
					System.lineSeparator() + e.getMessage(), ButtonType.OK).showAndWait();
			e.printStackTrace();
		}
	}

	/**
	 * Refreshes the Scatter Chart
	 */
	@FXML
	private void updateScatterChart() {
		FabricClassificationScatterChartPopulator populator = new FabricClassificationScatterChartPopulator(
				scatterChart);
		populator.setXIndex(dimensionNameToIndex(xValuesPicker.getValue(), 0));
		populator.setYIndex(dimensionNameToIndex(yValuesPicker.getValue(), 1));
		populator.populateScatterChart(trainingsFeatureVectors);

	}

	private int dimensionNameToIndex(String name, int defaultIndex) {
		int index = featureDimensions.indexOf(name);
		return (index >= 0) ? index : defaultIndex;
	}

	@FXML
	private void saveTrainingFeatureVectors() {
		saveFeatureVector(trainingsFeatureVectors, "StoffklassifizierungTrainingFeatures.txt");
	}

	/**
	 * Stores the FeatureVector array in the home directory (which depends on the
	 * operating system) with the given filename. Overwrites previous files that
	 * were stored in this way.
	 * 
	 * @param vectors  The object to save.
	 * @param filename The name of the created file.
	 */
	private void saveFeatureVector(FeatureVector[] vectors, String filename) {
		try {
			String path = new File(System.getProperty("user.home"), filename).getAbsolutePath();
			CsvInputOutput.write(path, vectors);
			new Alert(AlertType.INFORMATION, "Feature Vectors gespeichert.", ButtonType.OK).showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void saveTestFeatureVectors() {
		saveFeatureVector(testFeatureVectors, "StoffklassifizierungTestFeatures.txt");
	}

	@FXML
	private void loadTrainingFeatureVectors() {
		trainingsFeatureVectors = loadFeatureVector("StoffklassifizierungTrainingFeatures.txt");
		if (trainingsFeatureVectors != null) {
			initializeScatterPlot();
		}
	}

	/**
	 * Loads the stored FeatureVector array from the home directory (which depends
	 * on the operating system) saved under the given filename.
	 * 
	 * @param filename The name of the file in which the Feature Vectors are stored.
	 * @return The array created from the file content.
	 */
	private FeatureVector[] loadFeatureVector(String filename) {
		FeatureVector[] vectors = null;
		try  {
			String path = new File(System.getProperty("user.home"), filename).getAbsolutePath();
			vectors = CsvInputOutput.read(path);
			new Alert(AlertType.INFORMATION, "Feature Vektoren geladen.", ButtonType.OK).showAndWait();
		} catch (Exception e) {
			new Alert(AlertType.ERROR, "Beim Laden der Datei ist ein Fehler aufgetreten. Prüfen Sie "
					+ "ob eine Datei im Nutzerverzeichnis existiert.", ButtonType.OK).showAndWait();
			e.printStackTrace();
		}
		return vectors;
	}

	@FXML
	private void loadTestFeatureVectors() {
		testFeatureVectors = loadFeatureVector("StoffklassifizierungTestFeatures.txt");
	}

	/**
	 * Takes a file path from the user to map the image to an unlabeled ImageData
	 * object for individual classification.
	 */
	@FXML
	private void evaluateImage() {
		File selectedFile = getImageFileFromUser();

		try {
			if (classificator == null) {
				new Alert(AlertType.INFORMATION, "Trainieren Sie zunächst einen Klassifizierer.", ButtonType.OK)
						.showAndWait();
			} else {
				evalutationImage = ImageDataCreator.getImageData(selectedFile);

				URL url = selectedFile.toURI().toURL();
				evaluationImageView.setImage(new Image(url.toExternalForm()));
				
				new Thread() {
					public void run() {
						FeatureVector evaluationFeatureVector = generateFeatureVector(evalutationImage);
						Lable result = classificator.testClassifier(evaluationFeatureVector);
						new Alert(AlertType.INFORMATION, "Ergebnis: " + result, ButtonType.OK).showAndWait();
					}
				}.start();
			}
		} catch (IOException e) {
			new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
		}
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
}