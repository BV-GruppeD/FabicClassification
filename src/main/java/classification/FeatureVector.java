package classification;

import java.io.Serializable;

import com.bv_gruppe_d.imagej.Lable;

public class FeatureVector implements Serializable {

	private static final long serialVersionUID = 1L;
	private final double[] featureValues;
	private final String[] featureNames;
	private final Lable lable;

	public FeatureVector(String[] featureNames, double[] featureValues, Lable lable) {
		this.featureNames = featureNames;
		this.featureValues = featureValues;
		this.lable = lable;
	}

	public double[] getFeatureValues() {
		return this.featureValues;
	}

	public String[] getFeatureNames() {
		return featureNames;
	}

	public Lable getLable() {
		return this.lable;
	}
}
