package userinterface;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.bv_gruppe_d.imagej.Hyperparameter;
import com.bv_gruppe_d.imagej.ImageData;
import com.bv_gruppe_d.imagej.Label;
import com.bv_gruppe_d.imagej.Session;

import classification.Classifier;
import featureextraction.EllipsisData;
import featureextraction.FeatureExtractor;
import featureextraction.FeatureVector;
import featureextraction.HoughTransformation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import preprocessing.PreProcessing;
import utilities.DrawEllipses;
import utilities.ImageDataCreator;

/**
 * Provides methods for user inputs from the UserInterfaceView generated
 * MainPage.fxml
 */
public class UserInterfaceControler {
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
	@FXML
	private Button loadTrainingFeatures;
	@FXML
	private Button saveTrainingFeatures;
	@FXML
	private Button loadTestFeatures;
	@FXML
	private Button saveTestFeatures;
	
	private ArrayList<Button> buttons;
	private Session session;
	private UserDialogs dialogs;
	private FabricClassificationScatterChartPopulator populator;
	private ObservableList<String> featureDimensions;

	private final HoughTransformation houghTransformation;
	private final FeatureExtractor featureExtractor;
	
	
	public UserInterfaceControler() {
		this.buttons = new ArrayList<>();
		this.session = new Session();
		this.dialogs = new UserDialogs();
		this.houghTransformation = new HoughTransformation(
				Hyperparameter.HOUGH_ACCUMULATOR_THRESHOLD, Hyperparameter.HOUGH_ACCUMULATOR_BIN_SIZE, 
				Hyperparameter.HOUGH_ELLIPSIS_AXIS_MIN, Hyperparameter.HOUGH_ELLIPSIS_AXIS_MAX);
		this.featureExtractor = new FeatureExtractor();
	}

	/**
	 * Takes a directory from the user and maps the images in the subfolders to
	 * labeled ImageData objects representing the training data for a classifier.
	 * Stores the data in the current session.
	 */
	@FXML
	private void readLabeldTrainingsData() {
		File upperDirectory = dialogs.getDirectoryFromUser();
		session.setTrainingsData(ImageDataCreator.getLabeldImageData(upperDirectory));
	}

	/**
	 * Takes a directory from the user and maps the images in the subfolders to
	 * labeled ImageData objects representing the test data for a classifier.
	 * Stores the data in the current session.
	 */
	@FXML
	private void readLabeldTestData() {
		File upperDirectory = dialogs.getDirectoryFromUser();
		session.setTestData(ImageDataCreator.getLabeldImageData(upperDirectory));
	}

	
	
	/**
	 * Tests the current classifier in the session on the currently provided feature vectors for testing.
	 * 
	 * Prompts notifications if either is missing or initializes the preprocessing and feature extraction
	 * pipe when images are provided in the session.
	 * (Does not work on the UI-Thread because the calculation is not trivial, thereby the buttons to 
	 * start another competing process are disabled for the duration)
	 */
	@FXML
	private void testClassifier() {
		disableButtons();
		new Thread("hsowl_testClassifier") {
			public void run() {
				ArrayList<ImageData> images = session.getTestData();
				FeatureVector[] vectors = session.getTestFeatureVectors();
				Classifier classifier = session.getClassifier();
				
				if (classifier == null) {
					dialogs.showInformationLater("Trainieren Sie zunächst einen Klassifizierer");
				} else if (vectors == null && (images == null || images.size() == 0)) {
					dialogs.showInformationLater("Bitte lesen Sie zunächst Testdaten ein.");
				} else if (vectors == null) {
					session.setTestFeatureVectors(vectors = generateFeatureVectors(images, testProgressBar));
					classifiy(classifier, vectors);
				} else {
					classifiy(classifier, vectors);
				}
				
				Platform.runLater(() -> enableButtons());
			}
		}.start();
	}

	/**
	 * @return Returns a set of Feature Vectors corresponding to the given images.
	 */
	private FeatureVector[] generateFeatureVectors(ArrayList<ImageData> images, ProgressBar progress) {
		FeatureVector[] vectors = new FeatureVector[images.size()];

		for (int i = 0; i < vectors.length; ++i) {
			final double vectorNumber = (double) i;
			Platform.runLater(() -> progress.setProgress(vectorNumber / vectors.length));
			vectors[i] = generateFeatureVector(images.get(i));
		}
		Platform.runLater(() -> progress.setProgress(1));
		return vectors;
	}

	/**
	 * Preserves the user from starting several time consuming tasks at once.
	 */
	private void disableButtons() {
		for (Button b : getAllButtons()) {
			b.setDisable(true);
		}
	}
	
	/**
	 * Lifts the lock on starting time consuming task for the user.
	 */
	private void enableButtons() {
		for (Button b : getAllButtons()) {
			b.setDisable(false);
		}
	}
	
	/**
	 * Lazy initialized to prevent adding a bunch of null objects to the button list 
	 * (which would happen if executed in the constructor)
	 */
	private ArrayList<Button> getAllButtons() {
		if (buttons.isEmpty()) {
			buttons.add(trainClassifierBtn);
			buttons.add(testClassifierBtn);
			buttons.add(singleEvaluation);
			buttons.add(loadTrainingFeatures);
			buttons.add(saveTrainingFeatures);
			buttons.add(loadTestFeatures);
			buttons.add(saveTestFeatures);
		}
		return buttons;
	}

	/**
	 * Applies the image processing chain including the feature extractor
	 * and returns the resulting FeatureVector
	 */
	private FeatureVector generateFeatureVector(ImageData image) {
		// do not modify the original
		ImageData processImage = image.duplicate();
		// Processing the whole image takes way too long for testing, so we just
		// use a part. This might negatively affect up the error detection
		processImage.getImageProcessor().setRoi(0, 0, Math.min(512, image.getImageProcessor().getWidth()), 
				Math.min(512, image.getImageProcessor().getHeight()));
		processImage = new ImageData(processImage.getImageProcessor().crop(), processImage.getLabel());
		
		processImage = new ImageData(PreProcessing.execute(processImage).getImageProcessor(), processImage.getLabel());

		List<EllipsisData> ellipses = houghTransformation.execute(processImage);
		
		BufferedImage bi = DrawEllipses.drawEllipses(processImage.getImageProcessor(), ellipses);
		evaluationImageView.setImage(SwingFXUtils.toFXImage(bi, null));
		return featureExtractor.execute(processImage, ellipses);
	}

	/**
	 * Classifies all provided feature vectors and prompts a detailed description of the results.
	 */
	private void classifiy(Classifier classifier, FeatureVector[] testVectors) {
		
		Label[] results = new Label[testVectors.length];
		for (int i = 0; i < testVectors.length; i++) {
			results[i] = classifier.testClassifier(testVectors[i]);
		}

		ResultAnalysis analysis = new ResultAnalysis();
		String formatedResults = analysis.getFormatedResultAnalysis(testVectors, results);

		dialogs.showInformationLater(formatedResults);
	}

	
	
	/**
	 * Trains a new classifier for the session on the currently provided feature vectors for training.
	 * 
	 * Initializes the preprocessing and feature extraction pipe when no feature vectors but images are 
	 * provided in the session. Otherwise prompts notifications
	 * (Does not work on the UI-Thread because the calculation is not trivial, thereby the buttons to 
	 * start another competing process are disabled for the duration)
	 */
	@FXML
	private void trainClassifier() {
		disableButtons();
		new Thread("hsowl_trainClassifier") {
			public void run() {
				ArrayList<ImageData> images = session.getTrainingData();
				FeatureVector[] vectors = session.getTrainingFeatureVectors();
				Classifier classifier = new Classifier();
				
				if (vectors == null && (images == null || images.size() == 0)) {
					dialogs.showInformationLater("Bitte lesen Sie zunächst Trainingsdaten ein.");
				} else {
					if (vectors == null) {
						session.setTrainingFeatureVectors(
								vectors = generateFeatureVectors(images, trainingProgressBar));
						Platform.runLater(() -> initializeScatterPlot());
					}
					trainClassifier(classifier, vectors);
				}
				session.setClassifier(classifier);
				Platform.runLater(() -> enableButtons());
			}
		}.start();
	}
	
	/**
	 * Fills the DropDown menus for feature selection in the scatter chart with the current feature names and sets 
	 * the data on the scatter chart to the current training data.
	 */
	private void initializeScatterPlot() {
		if (populator == null) {
			populator = new FabricClassificationScatterChartPopulator(scatterChart);
		}
		FeatureVector[] vectors = session.getTrainingFeatureVectors();
		String[] dimensionNames = vectors[0].getFeatureNames();
		featureDimensions = FXCollections.observableArrayList(dimensionNames);
		xValuesPicker.setItems(featureDimensions);
		yValuesPicker.setItems(featureDimensions);
		
		populator.setPlottedDataSet(vectors);
	}

	/**
	 * Trains the classifier with provided feature vectors and prompts the parameter used for the classifier.
	 * @param classifier An initialized classifier to train.
	 * @param trainingVectors The data on which the classifier is trained.
	 */
	private final void trainClassifier(Classifier classifier, FeatureVector[] trainingVectors) {
		try {
			classifier.learnClassifier(trainingVectors);
			String result = "Training abgeschlossen" + System.lineSeparator() 
				+ "nu = " + classifier.getNu() + System.lineSeparator() +"Gamma = " + classifier.getGamma();
			dialogs.showInformationLater(result);
		} catch (Exception e) {
			dialogs.showErrorLater("Leider ist beim Lernen ein Fehler aufgetreten" 
					+ System.lineSeparator() + e.getMessage());
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Refreshes the Scatter Chart after new axes were selected.
	 */
	@FXML
	private void updateScatterChart() {
		populator.setXIndex(dimensionNameToIndex(xValuesPicker.getValue(), 0));
		populator.setYIndex(dimensionNameToIndex(yValuesPicker.getValue(), 1));
	}

	private int dimensionNameToIndex(String name, int defaultIndex) {
		int index = featureDimensions.indexOf(name);
		return (index >= 0) ? index : defaultIndex;
	}

	
	
	@FXML
	private void saveTrainingFeatureVectors() {
		try {
			session.persistTrainingFeatures();
		} catch (IOException e) {
			e.printStackTrace();
			dialogs.showError("Beim Speichern der Trainingsdaten ist ein Fehler aufgetreten");
		}
	}

	@FXML
	private void saveTestFeatureVectors() {
		try {
			session.persistTestFeatures();
		} catch (IOException e) {
			e.printStackTrace();
			dialogs.showError("Beim Speichern der Testdaten ist ein Fehler aufgetreten");
		}
	}
	
	@FXML
	private void loadTrainingFeatureVectors() {
		try {
			session.loadTrainingFeatures();
			initializeScatterPlot();
		} catch (IOException e) {
			e.printStackTrace();
			dialogs.showError("Beim Laden der Trainingsdaten ist ein Fehler aufgetreten");
		}
	}

	@FXML
	private void loadTestFeatureVectors() {
		try {
			session.loadTestFeatures();
		} catch (IOException e) {
			e.printStackTrace();
			dialogs.showError("Beim Laden der Testdaten ist ein Fehler aufgetreten");
		}
	}

	
	
	/**
	 * Takes a file path from the user to map the image to an unlabeled ImageData
	 * object for individual classification.
	 */
	@FXML
	private void evaluateImage() {
		disableButtons();
		File selectedFile = dialogs.getImageFileFromUser();
		
		if (selectedFile != null) {
			new Thread("hsowl_evaluateImage") {
				public void run() {
					Classifier classifier = session.getClassifier();
					
					if (classifier == null) {
						dialogs.showInformationLater("Trainieren Sie zunächst einen Klassifizierer");
					} else {
						try {
							ImageData evalutationImage = ImageDataCreator.getImageData(selectedFile);
							URL url = selectedFile.toURI().toURL();
							evaluationImageView.setImage(new Image(url.toExternalForm()));

						
							FeatureVector evaluationFeatureVector = generateFeatureVector(evalutationImage);
							Label result = classifier.testClassifier(evaluationFeatureVector);
							Platform.runLater(() -> 
								populator.includeInScatterChartTemporarily(evaluationFeatureVector));
							dialogs.showInformationLater("Ergebnis: " + result);
						} catch (IOException e) {
							e.printStackTrace();
							dialogs.showErrorLater("Beim Evaluieren des Bildes ist leider ein Fehler aufgetreten.");
						}
					}
					
					Platform.runLater(() -> enableButtons());
				}
			}.start();
		}
	}
}