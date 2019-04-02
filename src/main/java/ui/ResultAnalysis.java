package ui;

import java.util.Arrays;
import java.util.stream.Stream;

import com.bv_gruppe_d.imagej.Lable;
import com.github.habernal.confusionmatrix.ConfusionMatrix;

import classification.FeatureVector;

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
	public String getFormatedResultAnalysis(FeatureVector[] testFeatureVectors, Lable[] results) {
		analyzeResults(testFeatureVectors, results);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Analyse der Ergebnisse" + newLine);
		
		Arrays.stream(Lable.values())
			.filter(lable -> lable != Lable.UNKNOWN)
			.forEach(lable -> appendPrecisionAndRecall(sb, lable));

		sb.append(newLine);
		sb.append("F-Maß (F-Measure) insgesammt: " + confusionMatrix.getMacroFMeasure());
		
		return sb.toString();
	}
	
	private void appendPrecisionAndRecall(StringBuilder sb, Lable label) {
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
	private void analyzeResults(FeatureVector[] testFeatureVectors, Lable[] results) {
		confusionMatrix = new ConfusionMatrix();
		
		for (int i = 0; i < results.length; i++) {
			Lable actualLabel = results[i];
			Lable targetLabel = testFeatureVectors[i].getLable();
			confusionMatrix.increaseValue(targetLabel.toString(), actualLabel.toString());
		}
	}
}
