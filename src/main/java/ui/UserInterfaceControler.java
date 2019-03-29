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
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
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
	/**
	 * How many votes the maximum of the accumulator needs to be considered a valid
	 * ellipsis. Increasing this number leads to fewer ellipses. Choosing a too
	 * small number will make almost anything like an ellipsis
	 */
	private static final int HOUGH_ACCUMULATOR_THRESHOLD = 2;

	/**
	 * How big the bin size of the accumulator is. The bigger it is the less
	 * accurate the ellipsis parameters, but if it is chosen too small no ellipsis
	 * will be found
	 */
	private static final double HOUGH_ACCUMULATOR_BIN_SIZE = 1.0;

	/**
	 * How small the minor (small) axis of an ellipsis may be. Smaller ellipses will
	 * be ignored
	 */
	private static final double HOUGH_ELLIPSIS_AXIS_MIN = 4;

	/**
	 * How big the major (big) axis of an ellipsis may be. Bigger ellipses will be
	 * ignored
	 */
	private static final double HOUGH_ELLIPSIS_AXIS_MAX = 100;
	
	/**
	 * Filenames to store and load feature vectors
	 */
	private static final String TEST_FILE_NAME = "StoffklassifizierungTestFeatures.txt";
	private static final String TRAINING_FILE_NAME = "StoffklassifizierungTrainingFeatures.txt";
	
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

	private final HoughTransformation houghTransformation = new HoughTransformation(HOUGH_ACCUMULATOR_THRESHOLD,
			HOUGH_ACCUMULATOR_BIN_SIZE, HOUGH_ELLIPSIS_AXIS_MIN, HOUGH_ELLIPSIS_AXIS_MAX);
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
	@FXML
	private Button trainClassifierBtn;
	@FXML
	private Button testClassifierBtn;
	@FXML
	private Button singleEvaluation;

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
			showDialog(AlertType.INFORMATION, "Bitte lesen Sie zunächst Testdaten ein.");
		} else if (classificator == null) {
			showDialog(AlertType.INFORMATION, "Trainieren Sie zunächst einen Klassifizierer");
		} else {
			showDialog(AlertType.ERROR, "Bei der Bearbeitung ist leider ein Fehler aufgetreten");
		}
	}

	/**
	 * Starts a new Thread to generate the Feature Vectors for testing and to
	 * classify them afterwards.
	 * 
	 * @param images The images to process and test.
	 */
	private void generateTestFeatures(ArrayList<ImageData> images) {
		new Thread("hsowl_executeImageProcessingPipe") {
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
		disableButtons();
		FeatureVector[] vectors = new FeatureVector[images.size()];

		for (int i = 0; i < vectors.length; ++i) {
			final double vectorNumber = (double) i;
			Platform.runLater(() -> progress.setProgress(vectorNumber / vectors.length));
			vectors[i] = generateFeatureVector(images.get(i));
		}
		enableButtons();
		return vectors;
	}

	/**
	 * Preserves the user from starting several time consuming tasks at once.
	 */
	private void disableButtons() {
		trainClassifierBtn.setDisable(true);
		testClassifierBtn.setDisable(true);
		singleEvaluation.setDisable(true);
	}
	
	/**
	 * Lifts the lock on starting time consuming task for the user.
	 */
	private void enableButtons() {
		trainClassifierBtn.setDisable(false);
		testClassifierBtn.setDisable(false);
		singleEvaluation.setDisable(false);
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

		processImage = new ImageData(preprocessing.execute(processImage).getImageProcessor(), processImage.getLable());
	
		evaluationImageView.setImage(SwingFXUtils.toFXImage(processImage.getImageProcessor().getBufferedImage(), null));
		
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
		classificator = new Classificator();
		new Thread("hs_owl.trainClassifier") {
			@Override
			public void run() {
				if (trainingsData != null) {
					generateTrainingsFeatures(trainingsData);
				} else if (trainingsFeatureVectors != null) {
					createClassificator();
				} else {
					showDialog(AlertType.INFORMATION, "Bitte lesen Sie zunächst Testdaten ein.");
				}
			}
		}.start();
	}

	private static void showDialog(AlertType type, String text) {
		System.out.println("[DIALOG] '" + text + "'");
		Platform.runLater(() -> new Alert(type, text, ButtonType.OK).show());
	}

	/**
	 * Starts a new Thread to generate the Feature Vectors for training and to
	 * create the classifier afterwards.
	 * 
	 * @param images The images to process and train with.
	 */
	private void generateTrainingsFeatures(ArrayList<ImageData> images) {
		trainingsFeatureVectors = executeImageProcessingPipe(images, trainingProgressBar);
		Platform.runLater(() -> initializeScatterPlot());
		createClassificator();
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
			classificator.learnClassifier(trainingsFeatureVectors);
			showDialog(AlertType.INFORMATION, "Training abgeschlossen" + System.lineSeparator() + "nu = "
					+ classificator.getNu() + "\r\nGamma = " + classificator.getGamma());
		} catch (Exception e) {
			showDialog(AlertType.ERROR,
					"Leider ist beim Lernen ein Fehler aufgetreten" + System.lineSeparator() + e.getMessage());
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
		saveFeatureVector(trainingsFeatureVectors, TRAINING_FILE_NAME);
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
			showDialog(AlertType.INFORMATION, "Feature-Vectoren gespeichert.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void saveTestFeatureVectors() {
		Runnable r = () -> saveFeatureVector(testFeatureVectors, TEST_FILE_NAME);
		new Thread(r, "hsowl_saveTestFeatureVectors").start();
	}

	
	
	@FXML
	private void loadTrainingFeatureVectors() {
		trainingsFeatureVectors = loadFeatureVector(TRAINING_FILE_NAME);
		
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
		try {
			String path = new File(System.getProperty("user.home"), filename).getAbsolutePath();
			vectors = CsvInputOutput.read(path);
			showDialog(AlertType.INFORMATION, "Feature Vektoren geladen.");
		} catch (Exception e) {
			showDialog(AlertType.ERROR, "Beim Laden der Datei ist ein Fehler aufgetreten. Prüfen Sie "
					+ "ob die Datei '" + filename + "' im Nutzerverzeichnis existiert.");
			e.printStackTrace();
		}
		return vectors;
	}

	@FXML
	private void loadTestFeatureVectors() {
		testFeatureVectors = loadFeatureVector(TEST_FILE_NAME);
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
				showDialog(AlertType.INFORMATION, "Trainieren Sie zunächst den Klassifizierer.");
			} else {
				evalutationImage = ImageDataCreator.getImageData(selectedFile);

				URL url = selectedFile.toURI().toURL();
				evaluationImageView.setImage(new Image(url.toExternalForm()));

				new Thread("hsowl_evaluateImage") {
					public void run() {
						FeatureVector evaluationFeatureVector = generateFeatureVector(evalutationImage);
						Lable result = classificator.testClassifier(evaluationFeatureVector);
						showDialog(AlertType.INFORMATION, "Ergebnis: " + result);
					}
				}.start();
			}
		} catch (IOException e) {
			showDialog(AlertType.ERROR, e.getMessage());
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
		FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Bilddateien", "*.jpg", "*.png");
		fileChooser.getExtensionFilters().add(imageFilter);
		return fileChooser.showOpenDialog(null);
	}
}