package classification;

import java.util.ArrayList;

import com.bv_gruppe_d.imagej.Lable;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

/**
 * Provides methods to train and test a classifier with provided data.
 * The class works as a wrapper and translator for the LibSVM library and internally handles the 
 * necessary initialization.
 * 
 * For the understanding of the library methods, parameters and fields a look in the GitHub 
 * repository (especially in the README file) is absolutely essential.
 * (Link provided below)
 * 
 * https://github.com/cjlin1/libsvm
 */
public class Classificator {
	
	private svm_model model;
	private FeatureScaling scaling;
	
	// Model parameter
	private double nu;
	private double gamma;
	
	public double getNu() {
		return this.nu;
	}
	
	public double getGamma() {
		return this.gamma;
	}
	
	/**
	 * Initializes the classifier and optimizes a multi-class support vector machine to fit the 
	 * provided data. The one-versus-one approach is used internally.
	 * 
	 * @param featureVectors The data used to train the classifier.
	 * @throws Exception Thrown if the parameters are unsuited or incomplete for learning a classifier.
	 */
	public void learnClassifier(FeatureVector[] featureVectors) throws Exception {
		svm_problem data = createDataFormatForLearning(featureVectors);

		SVMParameterSearch search = new SVMParameterSearch();
		svm_parameter parameters = search.parameterGridSearch(data);
		
		// A library provided check for the integrity of the parameters
		String parameterCheck = svm.svm_check_parameter(data, parameters);
		if(parameterCheck == null) {
			nu = parameters.nu;
			gamma = parameters.gamma;
			model = svm.svm_train(data, parameters);
			search.saveParameterSearchLogToFile();
		} else {
			throw new Exception("Die Parameter zum Lernen des Klassifizierers sind ungültig", 
					new Throwable(parameterCheck));
		}
	}
	
	/**
	 * Translates the data format used throughout this application in the LibSVM required objects.
	 * 
	 * @param featureVectors The data for translation.
	 * @return The LibSVM data structure containing the complete set of data given to the method.
	 */
	private svm_problem createDataFormatForLearning(FeatureVector[] featureVectors) {
		svm_problem data = new svm_problem();
		data.l = featureVectors.length; 									// Number of training data		
		data.y = new double[featureVectors.length];							// Labels 
		
		ArrayList<svm_node[]> convertedFeatureVectors = new ArrayList<>();	
		scaling = new FeatureScaling(featureVectors);
		for (int i = 0; i < featureVectors.length; i++) {
			// Scale Feature Vector for 
			FeatureVector scaledFeaturVector = scaling.scaleFeatureVector(featureVectors[i]);
			
			// Create svm_nodes
			data.y[i] = scaledFeaturVector.getLable().getNumericRepresentation();
			svm_node[] features = convertFeatureVector(scaledFeaturVector);
			convertedFeatureVectors.add(features);
		}		
		
		data.x = convertedFeatureVectors.toArray(new svm_node[][] {});		// Features
		
		return data;
	}
	
	/**
	 * Converts the application specific data format in the format required from LibSVM.
	 * @param featureVector The data to be converted.
	 * @return A representation of input data in the LibSVM data format
	 */
	private svm_node[] convertFeatureVector(FeatureVector featureVector) {
		svm_node[] features = new svm_node[featureVector.getFeatureValues().length];
		for (int j = 0; j < featureVector.getFeatureValues().length; j++) {
			svm_node feature = new svm_node();
			feature.index = j+1; // Indexes in LibsSVM start with 1
			feature.value = featureVector.getFeatureValues()[j];
			features[j] = feature;
		}
		return features;
	}
	
	/**
	 * Requires a previous execution of the training method.
	 * 
	 * Classifies the given data point with this classifier.
	 * 
	 * @param featureVector The data point to be classified.
	 * @return The Label which the classifier determined for the data point.
	 */
	public Lable testClassifier(FeatureVector featureVector) {
		FeatureVector scaledFeatureVector = scaling.scaleFeatureVector(featureVector);
		
		svm_node[] features = convertFeatureVector(scaledFeatureVector);
		
		return Lable.valueOf(svm.svm_predict(model, features));
	}
}
