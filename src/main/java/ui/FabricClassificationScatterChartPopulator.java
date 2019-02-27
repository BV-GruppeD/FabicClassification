package ui;

import java.util.ArrayList;
import java.util.Arrays;

import com.bv_gruppe_d.imagej.Lable;

import classification.FeatureVector;
import ij.IJ;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;

/**
 * Provides utility methods to visualize two features of classified or
 * unclassified data samples in ScatterChart.
 */
public class FabricClassificationScatterChartPopulator {

	private ScatterChart<Number, Number> scatterChart;

	private XYChart.Series<Number, Number> seriesNoStretch;
	private XYChart.Series<Number, Number> seriesMediumStretch;
	private XYChart.Series<Number, Number> seriesMaximumStretch;
	private XYChart.Series<Number, Number> seriesDisturbance;
	private XYChart.Series<Number, Number> seriesSheard;
	private XYChart.Series<Number, Number> seriesUnknown;

	/**
	 * Creates a new scatter chart populator and initializes the data series for
	 * each label used in this application.
	 * 
	 * @param scatterChart The ScatterChart object that is populated by this object.
	 */
	public FabricClassificationScatterChartPopulator(ScatterChart<Number, Number> scatterChart) {
		this.scatterChart = scatterChart;

		initializeDataSeries();
	}

	/**
	 * Initializes the data series corresponding to the data labels.
	 */
	private void initializeDataSeries() {
		this.seriesNoStretch = new XYChart.Series<Number, Number>();
		this.seriesMediumStretch = new XYChart.Series<Number, Number>();
		this.seriesMaximumStretch = new XYChart.Series<Number, Number>();
		this.seriesDisturbance = new XYChart.Series<Number, Number>();
		this.seriesSheard = new XYChart.Series<Number, Number>();
		this.seriesUnknown = new XYChart.Series<Number, Number>();

		seriesNoStretch.setName("No_Stretch");
		seriesMediumStretch.setName("Medium_Stretch");
		seriesMaximumStretch.setName("Maxiumum_Stretch");
		seriesDisturbance.setName("Disturbance");
		seriesSheard.setName("Sheard");
		seriesUnknown.setName("Unknown");
	}

	/**
	 * Adds all data points from the given array to this scatter chart. Only the two
	 * dimensional data is accepted.
	 * 
	 * @param featureVectors The features to be plotted.
	 * @param xLabel         The label for the x-Axis (First values in the vector)
	 * @param yLabel         The label for the y-Axis (Second values in the vector)
	 */
	public void populateScatterChart(FeatureVector[] featureVectors, String xLabel, String yLabel) {

		scatterChart.getXAxis().setLabel(xLabel);
		scatterChart.getYAxis().setLabel(yLabel);

		for (FeatureVector featureVector : featureVectors) {
			if (featureVector.getFeatureValues().length >= 2) {
				addVectorToCorrectSeries(featureVector);// will ignore extra dimensions
			} else {
				IJ.showMessage("Der Datensatz kann nicht in einem 2D-Graphen angezeigt werden.");
			}
		}
		addDataSeriesToChart();
	}

	/**
	 * Depending on the label of the feature vector the data point is added to the
	 * corresponding data series. The first value of the vector provides the x
	 * value. The second value of the vector provides the y value.
	 * 
	 * @param featureVector The feature containing the x and y values that are added
	 *                      to a series.
	 */
	private void addVectorToCorrectSeries(FeatureVector featureVector) {
		double[] values = featureVector.getFeatureValues();
		switch (featureVector.getLable()) {
		case NO_STRETCH:
			seriesNoStretch.getData().add(new XYChart.Data<Number, Number>(values[0], values[1]));
			break;
		case MEDIUM_STRETCH:
			seriesMediumStretch.getData().add(new XYChart.Data<Number, Number>(values[0], values[1]));
			break;
		case MAXIMUM_STRECH:
			seriesMaximumStretch.getData().add(new XYChart.Data<Number, Number>(values[0], values[1]));
			break;
		case DISTURBANCE:
			seriesDisturbance.getData().add(new XYChart.Data<Number, Number>(values[0], values[1]));
			break;
		case SHEARD:
			seriesSheard.getData().add(new XYChart.Data<Number, Number>(values[0], values[1]));
			break;
		default:
			seriesUnknown.getData().add(new XYChart.Data<Number, Number>(values[0], values[1]));
			break;
		}
	}

	/**
	 * Adds the data sets to the scatter chart.
	 */
	@SuppressWarnings("unchecked")
	private void addDataSeriesToChart() {
		scatterChart.getData().addAll(seriesNoStretch);
		scatterChart.getData().addAll(seriesMediumStretch);
		scatterChart.getData().addAll(seriesMaximumStretch);
		scatterChart.getData().addAll(seriesDisturbance);
		scatterChart.getData().addAll(seriesSheard);
		scatterChart.getData().addAll(seriesUnknown);
	}

	public void populateScatterChartWithData(FeatureVector[] vectors) {
		scatterChart.setTitle("Training data");

		populateScatterChart(vectors, "Axis Ratio", "Ellipsis Area");
	}
}
