package classification;

/**
 * Data structure to map nu-SVM parameters to a f-measure.
 * Thereby maps parameterization to results on an unspecified test set.
 */
public class ClassifierTestMapping {
	private double gamma;
	private double nu;
	private double fMeasure;

	
	public ClassifierTestMapping(double gamme, double nu, double fMeasure) {
		this.nu = nu;
		this.gamma = gamme;
		this.fMeasure = fMeasure;
	}
	
	public double getGamma() {
		return gamma;
	}
	
	public double getNu() {
		return nu;
	}
	
	public double getFMeasure() {
		return fMeasure;
	}	
}
