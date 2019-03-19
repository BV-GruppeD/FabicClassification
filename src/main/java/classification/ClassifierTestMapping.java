package classification;

/**
 * Data structure to map nu-SVM parameters to a classification rate.
 * Thereby maps parameterization to results on an unspecified test set.
 */
public class ClassifierTestMapping {
	private double gamma;
	private double nu;
	private double classificationRate;

	
	public ClassifierTestMapping(double gamme, double nu, double classificationRate) {
		this.nu = nu;
		this.gamma = gamme;
		this.classificationRate = classificationRate;
	}
	
	public double getGamma() {
		return gamma;
	}
	
	public double getNu() {
		return nu;
	}
	
	public double getClassificationRate() {
		return classificationRate;
	}	
}
