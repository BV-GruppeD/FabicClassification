package classification;

import featureextraction.FeatureVector;

/**
 * Provides Methods to determine a factor for linear scaling of a set of Feature Vectors to the 
 * interval [-1,1] and the scaling itself. This is absolutely necessary before training a Support
 * Vector Machine (SVM). An unscaled set of features causes numerical problems when training and 
 * thereby leads to poor classification rates.
 */
public class FeatureScaling {
	
	private double[] scalingFactors;
	private static final double scaledInterval = 1; 

	public FeatureScaling(FeatureVector[] unscaledFeatureVectors) {
		calculateScalingFactors(unscaledFeatureVectors);
	}
	
	/**
	 * Calculates scaled representations of the given Feature Vectors. 
	 * 
	 * @param featureVectors	The array of feature vectors to scale.
	 * @return	An array of Feature Vectors in which each value is between -1 and 1
	 */
	private void calculateScalingFactors(FeatureVector[] featureVectors) {
		
		double[] maxima = findFeatureMaxima(featureVectors);
		
		scalingFactors = calculateMappingToScaledInterval(maxima);
	}

	private double[] findFeatureMaxima(FeatureVector[] featureVectors) {
		
		int numberOfFeatures = featureVectors[0].getFeatureValues().length;
		
		double[] maxima = new double[numberOfFeatures];
		for (int i = 0; i < numberOfFeatures; i++) {
			for (int j = 0; j < featureVectors.length; j++) {
				maxima[i] = Math.max(Math.abs(featureVectors[j].getFeatureValues()[i]), maxima[i]); 
			}
		}
		return maxima;
	}
	
	private double[] calculateMappingToScaledInterval(double[] maxima) {
		double[] factors = new double[maxima.length];
		for (int i = 0; i < maxima.length; i++) {
			factors[i] = scaledInterval / maxima[i]; 
		}
		
		return factors;
	}
	
	
	public FeatureVector scaleFeatureVector(FeatureVector vector) {
		
		int numberOfFeatures = vector.getFeatureValues().length;
		
		double[] scaledFeatureValues = new double[numberOfFeatures];
		for (int j = 0; j < numberOfFeatures; j++) {
			scaledFeatureValues[j] = vector.getFeatureValues()[j] * scalingFactors[j];
		}
		
		return new FeatureVector(vector.getFeatureNames(), scaledFeatureValues, vector.getLabel());
	}
}
