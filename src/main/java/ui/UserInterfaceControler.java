package ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import com.bv_gruppe_d.imagej.ImageData;
import com.bv_gruppe_d.imagej.Lable;
import classification.Classificator;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import preprocessing.Binarization;
import preprocessing.MorphologicalFiltering;

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
	private ComboBox<Integer> yValuesPicker;
	@FXML
	private ComboBox<Integer> xValuesPicker;
	
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
	 * Initializes the preprocessing of the provided Test Data. If no is provided checks if Feature Vectors 
	 * for testing were loaded and provides notifications otherwise.
	 */
	@FXML
	private void testClassifier() {
		if (testData != null && classificator != null) {
			generateTestFeatures(testData);
		} else if (testFeatureVectors != null && classificator != null) {
			classifiyTestFeatureVectors();
		} else if (testData == null) {
			new Alert(AlertType.INFORMATION, "Bitte lesen Sie zunächst Testdaten ein.", ButtonType.OK)
				.showAndWait();
		} else if (classificator == null) {
			new Alert(AlertType.INFORMATION, "Trainieren Sie zunächst einen Klassifizierer", ButtonType.OK)
				.showAndWait();
		} else {
			new Alert(AlertType.ERROR, "Bei der Bearbeitung ist leider ein Fehler aufgetreten", ButtonType.OK)
				.showAndWait();
		}
	}

	/**
	 * Starts a new Thread to generate the Feature Vectors for testing and to classify them afterwards.
	 * @param images The images to process and test.
	 */
	private void generateTestFeatures(ArrayList<ImageData> images) {
		
		new Thread(){
            public void run() {
            	testFeatureVectors = executeImageProcessingPipe(images, testProgressBar);
        		Platform.runLater(() -> testProgressBar.setProgress(0));
        		Platform.runLater(()-> classifiyTestFeatureVectors());
            }
        }.start();
	}
	
	/**
	 * @return Returns a set of Feature Vectors corresponding to the given images.
	 */
	private FeatureVector[] executeImageProcessingPipe(ArrayList<ImageData> images, ProgressBar progress) {
		FeatureVector[] vectors = new FeatureVector[images.size()];
    	HoughTransformation ht = new HoughTransformation(2, 1.0, 4, 100);
		FeatureExtractor fe = new FeatureExtractor();
		Binarization bin = new Binarization();
		MorphologicalFiltering mf = new MorphologicalFiltering();

		
		for (int i = 0; i < vectors.length; ++i) {
			final double vectorNumber = (double)i;
			Platform.runLater(() -> progress.setProgress(vectorNumber/vectors.length));
			
			//TODO: Kann man das noch ausgliedern, damit es keinen redundanten code mehr gibt?
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
	}

	/**
	 * Classifies all Feature Vectors for testing. Prompts the ratio of correct classification afterwards.
	 */
	private void classifiyTestFeatureVectors() {
		StringBuilder sb = new StringBuilder();
		int countCorrectlyClassified = 0;
		for (FeatureVector featureVector : testFeatureVectors) {
			Lable result = classificator.testClassifier(featureVector);
			sb.append("Vorgabe: " + featureVector.getLable().toString() + " - Ergebnis: "
						+ result.toString() + "\r\n");
			if (featureVector.getLable() == result) {
				countCorrectlyClassified++;
			}
		}
		sb.append("\r\nKorrekt Klassifiziert: " + countCorrectlyClassified +" von "
					+ testFeatureVectors.length + "\r\n" + "Klassifikationsrate: " + 
					((double)testFeatureVectors.length)/countCorrectlyClassified * 100 + "%");
		new Alert(AlertType.INFORMATION, sb.toString(), ButtonType.OK)
			.showAndWait();
	}

	/**
	 * Initializes the preprocessing of the provided Training Data. If no is provided checks if Feature Vectors 
	 * for training were loaded and provides a notifications otherwise.
	 */
	@FXML
	private void trainClassifier() {
		if (trainingsData != null) {
			generateTrainingsFeatures(trainingsData);			
		} else if(trainingsFeatureVectors != null) {
			createClassificator();
		} else {
			new Alert(AlertType.INFORMATION, "Bitte lesen Sie zunächst Testdaten ein.", ButtonType.OK)
				.showAndWait();
		}
	}
	
	/**
	 * Starts a new Thread to generate the Feature Vectors for training and to create the classifier afterwards.
	 * @param images The images to process and train with.
	 */
	private void generateTrainingsFeatures(ArrayList<ImageData> images) {
		new Thread(){
            public void run() {
        		trainingsFeatureVectors = executeImageProcessingPipe(images, trainingProgressBar);
        		Platform.runLater(() -> initializeScatterPlot());
        		Platform.runLater(() -> trainingProgressBar.setProgress(0));
        		Platform.runLater(()-> createClassificator());
            }
        }.start();
	}
	
	/**
	 * Fills the DropDown menus for Feature selection and populates the scatter chart.
	 */
	private void initializeScatterPlot() {
		ObservableList<Integer> featureDimensions = FXCollections.observableArrayList();
		for (int i = 0; i < trainingsFeatureVectors[0].getFeatureValues().length; i++) {
			featureDimensions.add(i);
		}
		xValuesPicker.setItems(featureDimensions);
		xValuesPicker.setValue(0);
		yValuesPicker.setItems(featureDimensions);
		yValuesPicker.setValue(1);
		
		FabricClassificationScatterChartPopulator populator = new FabricClassificationScatterChartPopulator(scatterChart);
		populator.setXIndex(0);
		populator.setYIndex(1);
		populator.populateScatterChart(trainingsFeatureVectors);
	}
	
	/**
	 * Creates and trains the classifier with the Training Feature Vectors.
	 */
	private final void createClassificator() {
		classificator = new Classificator();
		try {
			classificator.learnClassifier(trainingsFeatureVectors);
			
			new Alert(AlertType.INFORMATION, "Training abgeschlossen", ButtonType.OK).showAndWait();
		} catch (Exception e) {
			new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
			e.printStackTrace();
		}
	}
	
	/**
	 * Refreshes the Scatter Chart
	 */
	@FXML
	private void updateScatterChart() {
		FabricClassificationScatterChartPopulator populator = new FabricClassificationScatterChartPopulator(scatterChart);
		populator.setXIndex((xValuesPicker.getValue() != null)? xValuesPicker.getValue():0);
		populator.setYIndex((yValuesPicker.getValue() != null)? yValuesPicker.getValue():1);
		populator.populateScatterChart(trainingsFeatureVectors);

	}
	
	@FXML
	private void saveTrainingFeatureVectors() {
		saveFeatureVector(trainingsFeatureVectors, "StoffklassifizierungTrainingFeatures.txt");
	}

	/**
	 * Stores the FeatureVector array in the home directory (which depends on the operating system)
	 * with the given filename. Overwrites previous files that were stored in this way.
	 * @param vectors The object to save.
	 * @param filename The name of the created file.
	 */
	private void saveFeatureVector(FeatureVector[] vectors, String filename) {
		try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(
				new File(System.getProperty("user.home"), filename)))){
			stream.writeObject(vectors);
			new Alert(AlertType.INFORMATION, "Feature Vectors gespeichert.", ButtonType.OK)
				.showAndWait();
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
	 * Loads the stored FeatureVector array from the home directory (which depends on the operating system) 
	 * saved under the given filename.
	 * @param filename The name of the file in which the Feature Vectors are stored.
	 * @return The array created from the file content.
	 */
	private FeatureVector[] loadFeatureVector(String filename) {
		FeatureVector[] vectors = null;
		try (ObjectInputStream stream = new ObjectInputStream(
				new FileInputStream(new File(System.getProperty("user.home"), filename)))){
			vectors = (FeatureVector[]) stream.readObject();
			new Alert(AlertType.INFORMATION, "Feature Vektoren geladen.", ButtonType.OK)
				.showAndWait();
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
				new Alert(AlertType.INFORMATION, "Trainieren Sie zunächst einen Klassifizierer", ButtonType.OK)
					.showAndWait();
			} else {
				evalutationImage = ImageDataCreator.getImageData(selectedFile);

				URL url = selectedFile.toURI().toURL();
				evaluationImageView.setImage(new Image(url.toExternalForm()));

				FeatureVector evaluationFeatureVector = generateEvaluationFeatureVector(evalutationImage);
				Lable result = classificator.testClassifier(evaluationFeatureVector);
				new Alert(AlertType.INFORMATION, "Ergebnis: " + result, ButtonType.OK).showAndWait();
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

	private FeatureVector generateEvaluationFeatureVector(ImageData image) {
		HoughTransformation ht = new HoughTransformation(2, 1.0, 4, 100);
		FeatureExtractor fe = new FeatureExtractor();
		Binarization bin = new Binarization();
		MorphologicalFiltering mf = new MorphologicalFiltering();
		
		ImageData processImage = image.duplicate();// do not modify the original
		// Processing the whole image takes waaaay toooo looong for testing, so we just
		// use a part
		// this might screw up the error detection
		processImage.getImageProcessor().setRoi(0, 0, 200, 200);
		processImage = new ImageData(processImage.getImageProcessor().crop(), processImage.getLable());
					
		// Preprocessing
		bin.execute(processImage);
		mf.execute(processImage);

		return fe.execute(image, ht.execute(image));	
	}
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