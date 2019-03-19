package ui;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import com.bv_gruppe_d.imagej.Lable;

import classification.FeatureVector;

/**
 * Provides methods to compare Feature Vectors and associated Labels,
 * analysis of conformity ratios and formating of results.
 */
public class ResultAnalysis {

	private Map<Lable, Integer> countLabelOccurances;
	private Map<Lable, Integer> countFalsePositives;
	private Map<Lable, Integer> countFalseNegatives;
	
	public ResultAnalysis() {
		countLabelOccurances = initializeCountingHashMap();
		countFalsePositives = initializeCountingHashMap();
		countFalseNegatives = initializeCountingHashMap();
	}
	
	/**
	 * Initializes a Map with the values of the Label enumeration as keys and 0 as the related value.
	 * @return The initialized Map object.
	 */
	private static Map<Lable, Integer> initializeCountingHashMap() {
		Map<Lable, Integer> map = new HashMap<>();
		for (Lable label : Lable.values()) {
			map.put(label, 0);
		}
		return map;
	}
	
	/**
	 * Provides a string containing a statistical analysis of the compliance between the results 
	 * and the original Feature Vectors. It contains the False-Positive and False-Negative rates
	 * for each Label and an overall compliance rate.
	 * 
	 * @param testFeatureVectors The Feature Vectors containing the target Labels.
	 * @param results The actual Labels to be compared to the target Labels.
	 * @return The formated String
	 */
	public String getFormatedResultAnalysis(FeatureVector[] testFeatureVectors, Lable[] results) {
		analyzeResults(testFeatureVectors, results);
		
		// To be cross platform
		final String newLine = System.lineSeparator();
		DecimalFormat formater = new DecimalFormat("#00.000");
		StringBuilder sb = new StringBuilder();
		
		sb.append("Analyse der Ergebnisse" + newLine);
		for (Lable label : countLabelOccurances.keySet()) {
			sb.append(newLine + label.toString() + newLine);
			sb.append("Falsch positiv klassifiziert:\t" + 
					formater.format(getFalsePositiveRate(label)) + "%" + newLine);
			sb.append("Falsch negativ klassifiziert:\t" + 
					formater.format(getFalseNegativeRate(label)) + "%"+ newLine);
		}
		sb.append(newLine);
		sb.append("Insgesamt korrekt klassifiziert: " 
				+ formater.format(countCorrectClassified()/results.length * 100) + "%");
		
		return sb.toString();
	}

	/**
	 * Compares the target Label in the Feature Vectors with the actual Label provided in results
	 * and keeps track of the compliance between the parameters.
	 * 
	 * @param testFeatureVectors Set of Feature Vectors holding the target Labels
	 * @param results Set of actual Labels
	 */
	private void analyzeResults(FeatureVector[] testFeatureVectors, Lable[] results) {
		for (int i = 0; i < results.length; i++) {
			Lable actualLabel = results[i];
			Lable targetLabel = testFeatureVectors[i].getLable();
			
			increaseLabelOccurance(targetLabel);
			
			if (!Lable.areEqual(actualLabel, targetLabel)) {
				increaseFalsePositve(actualLabel);
				increaseFalseNegative(targetLabel);
			}
		}
	}

	private void increaseFalseNegative(Lable label) {
		countFalseNegatives.replace(label, countFalseNegatives.get(label) + 1);
	}

	private void increaseFalsePositve(Lable label) {
		countFalsePositives.replace(label, countFalsePositives.get(label) + 1);		
	}

	private void increaseLabelOccurance(Lable label) {
		countLabelOccurances.replace(label, countLabelOccurances.get(label) + 1);
	}

	/**
	 * Calculates Times the Label should have occurred but did not divided by the total number of 
	 * times the Label should have occurred.
	 */
	private double getFalseNegativeRate(Lable label) {
		return ((double)countFalseNegatives.get(label))/countLabelOccurances.get(label) * 100;
	}

	/**
	 * Calculates Times the Label should not have occurred but did divided by the total number of 
	 * times other Labels should have occurred.
	 */
	private double getFalsePositiveRate(Lable label) {
		int otherLabelOccurances = countLabelOccurances.values().stream().mapToInt(Integer::intValue).sum();
		return ((double)countFalsePositives.get(label))/ otherLabelOccurances * 100;
	}

	private double countCorrectClassified() {
		double correctClassifiedSum = 0;
		for (Lable label : Lable.values()) {
			correctClassifiedSum += (countLabelOccurances.get(label) - countFalseNegatives.get(label));
		}
		return correctClassifiedSum;
	}	
}
