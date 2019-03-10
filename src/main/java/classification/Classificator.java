package classification;

import java.util.ArrayList;

import com.bv_gruppe_d.imagej.Lable;

import ij.IJ;
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
	
	private double[] scalingFaktor;
	
	/**
	 * Initializes the classifier and optimizes a multi-class support vector machine to fit the 
	 * provided data. The one-versus-one approach is used internally.
	 * 
	 * @param featureVectors The data used to train the classifier.
	 */
	public void learnClassifier(FeatureVector[] featureVectors) {
		svm_parameter parameters = createParametrizationForLearning();
		FeatureVector[] scaledFeatureVectors = getScaledVectors(featureVectors);
		svm_problem data = createDataFormatForLearning(scaledFeatureVectors);
		
		// A library provided check for the integrety of the parameters
		String parameterCheck = svm.svm_check_parameter(data, parameters);
		if(parameterCheck == null) {
			model = svm.svm_train(data, parameters);
			IJ.showMessage("Training Done.");
		} else {
			IJ.showMessage(parameterCheck);
		}
	}
	
	private FeatureVector[] getScaledVectors(FeatureVector[] featureVectors) {
		int numberOfFeatures = featureVectors[0].getFeatureValues().length;
		
		// Find min and max
		double[] maximums = new double[numberOfFeatures];
		for (int i = 0; i < numberOfFeatures; i++) {
			for (int j = 0; j < featureVectors.length; j++) {
				maximums[i] = Math.max(Math.abs(featureVectors[j].getFeatureValues()[i]), maximums[i]); 
			}
		}
		
		// Calculate Scaling Factors
		scalingFaktor = new double[numberOfFeatures];
		for (int i = 0; i < numberOfFeatures; i++) {
			scalingFaktor[i] = 1 / maximums[i]; // Scale feature values to the interval [-1,1]
		}
		
		// Scale Feature Vectors
		FeatureVector[] scaledFeatureVectors = new FeatureVector[featureVectors.length];
		for (int i = 0; i < featureVectors.length; i++) {
			
			double[] scaledFeatureValues = new double[numberOfFeatures];
			for (int j = 0; j < numberOfFeatures; j++) {
				scaledFeatureValues[j] = featureVectors[i].getFeatureValues()[j] * scalingFaktor[j];
			}
			
			scaledFeatureVectors[i] = new FeatureVector(scaledFeatureValues, featureVectors[i].getLable());
		}
		
		return scaledFeatureVectors;
	}

	/**
	 * Defines the parameter set used internally in the library. For descriptions of the parameters 
	 * the above mentioned link provides informations.
	 * 
	 * @return The initialized parameter set for the training algorithm in LibSVM
	 */
	private svm_parameter createParametrizationForLearning() {
		svm_parameter parameters = new svm_parameter();
		parameters.svm_type = svm_parameter.NU_SVC;
		parameters.kernel_type = svm_parameter.RBF;
		parameters.cache_size = 1000;
		parameters.eps = 0.00001;
		parameters.nu = 0.5;
		parameters.C = 1;
		parameters.gamma = 0.33;
		
		return parameters;
	}

	/**
	 * Translates the data format used throughout this application in the LibSVM required objects.
	 * 
	 * @param featureVectors The data for translation.
	 * @return The LibSVM data structure containing the complete set of data given to the method.
	 */
	private svm_problem createDataFormatForLearning(FeatureVector[] featureVectors) {
		svm_problem data = new svm_problem();
		data.l = featureVectors.length; 									// Number of Trainingsdata		
		data.y = new double[featureVectors.length];							// Labels 
		ArrayList<svm_node[]> convertedFeatureVectors = new ArrayList<>();	// Features in sparse representation
		
		// Create svm_nodes
		for (int i = 0; i < featureVectors.length; i++) {
			data.y[i] = featureVectors[i].getLable().getNumericRepresentation();
			
			svm_node[] features = new svm_node[featureVectors[i].getFeatureValues().length];
			for (int j = 0; j < featureVectors[i].getFeatureValues().length; j++) {
				svm_node feature = new svm_node();
				feature.index = j+1; // Indexes in LibSVM start with 1
				feature.value = featureVectors[i].getFeatureValues()[j];
				features[j] = feature;
			}
			convertedFeatureVectors.add(features);
		}		
		
		data.x = convertedFeatureVectors.toArray(new svm_node[][] {});
		
		return data;
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
		FeatureVector scaledFeatureVector = getScaledFeatureVector(featureVector);
		svm_node[] features = new svm_node[scaledFeatureVector.getFeatureValues().length];
		for (int j = 0; j < scaledFeatureVector.getFeatureValues().length; j++) {
			svm_node feature = new svm_node();
			feature.index = j+1; // Indexes in LibsSVM start with 1
			feature.value = scaledFeatureVector.getFeatureValues()[j];
			features[j] = feature;
		}
		
		return Lable.valueOf(svm.svm_predict(model, features));
	}

	private FeatureVector getScaledFeatureVector(FeatureVector featureVector) {
		int numberOfFeatures = featureVector.getFeatureValues().length;
		
		double[] scaledFeatureValues = new double[numberOfFeatures];
		for (int j = 0; j < numberOfFeatures; j++) {
			scaledFeatureValues[j] = featureVector.getFeatureValues()[j] * scalingFaktor[j];
		}
		
		return new FeatureVector(scaledFeatureValues, featureVector.getLable());
	}
}
