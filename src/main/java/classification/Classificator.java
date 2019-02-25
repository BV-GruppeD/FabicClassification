package classification;

import java.util.ArrayList;

import com.bv_gruppe_d.imagej.Lable;

import ij.IJ;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class Classificator {
	
	private svm_model model;
	
	public void learnClassifier(FeatureVector[] featureVectors) {
		svm_parameter parameters = createParametrizationForLearning();
		
		svm_problem data = createDataFormatForLearning(featureVectors);
		
		String parameterCheck = svm.svm_check_parameter(data, parameters);
		if(parameterCheck == null) {
			IJ.showMessage("Klassifiziere wird trainiert.");
			model = svm.svm_train(data, parameters);
			IJ.showMessage("Training Done.");
		} else {
			IJ.showMessage(parameterCheck);
		}
	}
	
	private svm_parameter createParametrizationForLearning() {
		svm_parameter parameters = new svm_parameter();
		parameters.svm_type = svm_parameter.NU_SVC;
		parameters.kernel_type = svm_parameter.POLY;
		parameters.cache_size = 100;
		parameters.eps = 0.00001;
		parameters.nu = 0.5;
		
		return parameters;
	}

	private svm_problem createDataFormatForLearning(FeatureVector[] featureVectors) {
		svm_problem data = new svm_problem();
		data.l = featureVectors.length; 						// Number of Trainingsdata		
		data.y = new double[featureVectors.length];				// Labels 
		ArrayList<svm_node[]> convertedFeatureVectors = new ArrayList<>();// Features in sparse representation
		
		// Create svm_nodes
		for (int i = 0; i < featureVectors.length; i++) {
			data.y[i] = featureVectors[i].getLable().getNumericRepresentation();
			
			svm_node[] features = new svm_node[featureVectors[i].getFeatureValues().length];
			for (int j = 0; j < featureVectors[i].getFeatureValues().length; j++) {
				svm_node feature = new svm_node();
				feature.index = j;
				feature.value = featureVectors[i].getFeatureValues()[j];
				features[j] = feature;
			}
			convertedFeatureVectors.add(features);
		}		
		
		data.x = convertedFeatureVectors.toArray(new svm_node[][] {});		// Features in sparse representation
		
		return data;
	}
	
	public Lable testClassifier(FeatureVector featureVector) {
		svm_node[] features = new svm_node[featureVector.getFeatureValues().length];
		for (int j = 0; j < featureVector.getFeatureValues().length; j++) {
			svm_node feature = new svm_node();
			feature.index = j;
			feature.value = featureVector.getFeatureValues()[j];
			features[j] = feature;
			
		}
		return Lable.valueOf(svm.svm_predict(model, features));
	}
}
