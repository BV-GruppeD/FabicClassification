package ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.bv_gruppe_d.imagej.CsvInputOutput;
import com.bv_gruppe_d.imagej.ImageData;

import classification.Classifier;
import classification.FeatureVector;

/**
 * Data structure to manage one session of the PlugIn. 
 * It contains fields and methods to manage data and behavior throughout the session. 
 */
public class Session  {

	//Filenames to store and load feature vectors
	private static final String TEST_FILE_NAME = "StoffklassifizierungTestFeatures.txt";
	private static final String TRAINING_FILE_NAME = "StoffklassifizierungTrainingFeatures.txt";
	
	
	private ArrayList<ImageData> trainingData;
	private FeatureVector[] trainingFeatureVectors;
	
	private ArrayList<ImageData> testData;
	private FeatureVector[] testFeatureVectors;
	
	private Classifier classifier;
	
	
	public ArrayList<ImageData> getTrainingData() {
		return trainingData;
	}

	public void setTrainingsData(ArrayList<ImageData> trainingsData) {
		this.trainingData = trainingsData;
		this.trainingFeatureVectors = null;
	}

	public FeatureVector[] getTrainingFeatureVectors() {
		return trainingFeatureVectors;
	}

	public void setTrainingFeatureVectors(FeatureVector[] trainingsFeatureVectors) {
		this.trainingFeatureVectors = trainingsFeatureVectors;
	}

	public ArrayList<ImageData> getTestData() {
		return testData;
	}

	public void setTestData(ArrayList<ImageData> testData) {
		this.testData = testData;
		this.testFeatureVectors = null;
	}

	public FeatureVector[] getTestFeatureVectors() {
		return testFeatureVectors;
	}

	public void setTestFeatureVectors(FeatureVector[] testFeatureVectors) {
		this.testFeatureVectors = testFeatureVectors;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * Writes the data used to train a classifier into a file on the system.
	 * (Overrides previously saved data in the process)
	 * @throws IOException Thrown when the process failed
	 */
	public void persistTrainingFeatures() throws IOException {
		String path = new File(System.getProperty("user.home"), TRAINING_FILE_NAME).getAbsolutePath();
		CsvInputOutput.write(path, getTrainingFeatureVectors());
	}
	
	/**
	 * Writes the data used to test a classifier into a file on the system.
	 * (Overrides previously saved data in the process)
	 * @throws IOException Thrown when the process failed
	 */
	public void persistTestFeatures() throws IOException {
		String path = new File(System.getProperty("user.home"), TEST_FILE_NAME).getAbsolutePath();
		CsvInputOutput.write(path, getTestFeatureVectors());
	}
	
	/**
	 * Reads previously persisted data to train a classifier from a file on the system.
	 * (Overrides current session data)
	 * @throws IOException Thrown when the process failed
	 */
	public void loadTrainingFeatures() throws IOException {
		String path = new File(System.getProperty("user.home"), TRAINING_FILE_NAME).getAbsolutePath();
		setTrainingFeatureVectors(CsvInputOutput.read(path));
	}
	
	/**
	 * Reads previously persisted data to test a classifier from a file on the system.
	 * (Overrides current session data)
	 * @throws IOException Thrown when the process failed
	 */
	public void loadTestFeatures() throws IOException {
		String path = new File(System.getProperty("user.home"), TEST_FILE_NAME).getAbsolutePath();
		setTestFeatureVectors(CsvInputOutput.read(path));
	}
}
