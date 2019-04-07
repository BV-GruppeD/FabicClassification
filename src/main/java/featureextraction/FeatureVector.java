package featureextraction;

import java.io.Serializable;

import com.bv_gruppe_d.imagej.Label;

public class FeatureVector implements Serializable {

	private static final long serialVersionUID = 1L;
	private final double[] featureValues;
	private final String[] featureNames;
	private final Label label;

	public FeatureVector(String[] featureNames, double[] featureValues, Label label) {
		this.featureNames = featureNames;
		this.featureValues = featureValues;
		this.label = label;
	}

	public double[] getFeatureValues() {
		return this.featureValues;
	}

	public String[] getFeatureNames() {
		return featureNames;
	}

	public Label getLabel() {
		return this.label;
	}
}
