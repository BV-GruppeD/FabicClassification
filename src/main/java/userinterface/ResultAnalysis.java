package userinterface;

import java.util.Arrays;

import com.bv_gruppe_d.imagej.Label;
import com.github.habernal.confusionmatrix.ConfusionMatrix;

import featureextraction.FeatureVector;

/**
 * Provides methods to compare Feature Vectors and associated Labels,
 * analysis of conformity ratios and formating of results.
 */
public class ResultAnalysis {

	/**
	 * A confusion matrix that stores the True-Positive, False-Negative, and False-Positive
	 * results from an experiment.
	 * 
	 * For more information about the concept see:
	 * (https://towardsdatascience.com/machine-learning-multiclass-classification-with-imbalanced-data-set-29f6a177c1a)
	 * For information about the Java implementation look on GitHub:
	 * (https://github.com/habernal/confusion-matrix)
	 */
	private ConfusionMatrix confusionMatrix;
	
	/**
	 * Cross platform line separator.
	 */
	private static final String newLine = System.lineSeparator();
	
	public ResultAnalysis() {
		confusionMatrix = new ConfusionMatrix();
	}
	
	/**
	 * Provides a string containing a statistical analysis of the compliance between the results 
	 * and the original Feature Vectors. It contains the Precision and Recall rates for each Label
	 * and the overall F-Macro-Average-Measure.
	 * 
	 * @param testFeatureVectors The Feature Vectors containing the target Labels.
	 * @param results The actual Labels to be compared to the target Labels.
	 * @return The formated String
	 */
	public String getFormatedResultAnalysis(FeatureVector[] testFeatureVectors, Label[] results) {
		analyzeResults(testFeatureVectors, results);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Analyse der Ergebnisse" + newLine);
		
		Arrays.stream(Label.values())
			.filter(label -> label != Label.UNKNOWN)
			.forEach(label -> appendPrecisionAndRecall(sb, label));

		sb.append(newLine);
		sb.append("F-Maß (F-Measure) insgesammt: " + confusionMatrix.getMacroFMeasure());
		
		return sb.toString();
	}
	
	private void appendPrecisionAndRecall(StringBuilder sb, Label label) {
		sb.append(newLine + label + newLine);
		sb.append("Genauigkeit (precision):\t" + 
				confusionMatrix.getPrecisionForLabel(label.toString()) + newLine);
		sb.append("Trefferquote (recall):  \t" + 
				confusionMatrix.getRecallForLabel(label.toString()) + newLine);
	}

	/**
	 * Creates a confusion matrix to keep track of the compliance between target and actual values.
	 * 
	 * @param testFeatureVectors Set of Feature Vectors holding the target Labels
	 * @param results Set of actual Labels
	 */
	private void analyzeResults(FeatureVector[] testFeatureVectors, Label[] results) {
		confusionMatrix = new ConfusionMatrix();
		
		for (int i = 0; i < results.length; i++) {
			Label actualLabel = results[i];
			Label targetLabel = testFeatureVectors[i].getLabel();
			confusionMatrix.increaseValue(targetLabel.toString(), actualLabel.toString());
		}
	}
}
