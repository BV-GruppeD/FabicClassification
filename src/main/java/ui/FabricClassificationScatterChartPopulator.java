package ui;

import classification.FeatureVector;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

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

	// Hold the index of the feature used for the x/y-values
	private int featureIndexAxisX;
	private int featureIndexAxisY;
	
	// Hold the current data plottet
	private FeatureVector[] featureVectors;
	
	/**
	 * Creates a new scatter chart populator and initializes the data series for
	 * each label used in this application.
	 * 
	 * @param scatterChart The ScatterChart object that is populated by this object.
	 */
	public FabricClassificationScatterChartPopulator(ScatterChart<Number, Number> scatterChart) {
		this.scatterChart = scatterChart;
		scatterChart.getData().clear();
		
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
	 * Re-populates the scatter plot with the last provided data set.
	 * 
	 * @param featureVectors The features to be plotted.
	 */
	public void populateScatterChart() {
		if (this.featureVectors != null) {
			populateScatterChart(this.featureVectors);
		}
	}
	
	/**
	 * Adds all data points from the given array to this scatter chart. Only the two
	 * dimensional data is accepted.
	 * 
	 * @param featureVectors The features to be plotted.
	 */
	public void populateScatterChart(FeatureVector[] featureVectors) {
		this.featureVectors = featureVectors;
		clearDataSeries();
		
		for (FeatureVector featureVector : featureVectors) {
			if (featureVector.getFeatureValues().length >= 2) {
				addVectorToCorrectSeries(featureVector);
			} else {
				new Alert(AlertType.ERROR,"Der Datensatz kann nicht in einem 2D-Graphen angezeigt werden.", 
						ButtonType.OK).showAndWait();
			}
		}
		
		addDataSeriesToChart();
		
		((NumberAxis)scatterChart.getXAxis()).setForceZeroInRange(false);
		((NumberAxis)scatterChart.getYAxis()).setForceZeroInRange(false);
	}

	/**
	 * Removes all data points stores in the each series.
	 */
	private void clearDataSeries() {
		seriesNoStretch.getData().clear();
		seriesMediumStretch.getData().clear();
		seriesMaximumStretch.getData().clear();
		seriesDisturbance.getData().clear();
		seriesSheard.getData().clear();
		seriesUnknown.getData().clear();
		scatterChart.getData().clear();
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
		switch (featureVector.getLabel()) {
		case NO_STRETCH:
			seriesNoStretch.getData().add(new XYChart.Data<Number, Number>(values[featureIndexAxisX], values[featureIndexAxisY]));
			break;
		case MEDIUM_STRETCH:
			seriesMediumStretch.getData().add(new XYChart.Data<Number, Number>(values[featureIndexAxisX], values[featureIndexAxisY]));
			break;
		case MAXIMUM_STRECH:
			seriesMaximumStretch.getData().add(new XYChart.Data<Number, Number>(values[featureIndexAxisX], values[featureIndexAxisY]));
			break;
		case DISTURBANCE:
			seriesDisturbance.getData().add(new XYChart.Data<Number, Number>(values[featureIndexAxisX], values[featureIndexAxisY]));
			break;
		case SHEARD:
			seriesSheard.getData().add(new XYChart.Data<Number, Number>(values[featureIndexAxisX], values[featureIndexAxisY]));
			break;
		default:
			seriesUnknown.getData().add(new XYChart.Data<Number, Number>(values[featureIndexAxisX], values[featureIndexAxisY]));
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

	public void setXIndex(int index) {
		this.featureIndexAxisX = index;
	}

	public void setYIndex(int index) {
		this.featureIndexAxisY = index;
	}
}
