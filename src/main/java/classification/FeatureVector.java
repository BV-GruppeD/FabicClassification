package classification;

import com.bv_gruppe_d.imagej.Lable;

public class FeatureVector {
	private double[] featureValues;
	private Lable lable;
	
	public FeatureVector(double[] featureValues, Lable lable) {
		this.featureValues = featureValues;
		this.lable = lable;
	}
	
	public double[] getFeatureValues() {
		return this.featureValues;
	}
	
	public Lable getLable() {
		return this.lable;
	}
}
