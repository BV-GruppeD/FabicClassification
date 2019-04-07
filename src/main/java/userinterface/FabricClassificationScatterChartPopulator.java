package userinterface;

import java.util.Arrays;

import featureextraction.FeatureVector;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;

/**
 * Provides methods to manage the visualization of feature vectors on a scatter chart.
 */
public class FabricClassificationScatterChartPopulator {

	private ScatterChart<Number, Number> scatterChart;

	private XYChart.Series<Number, Number> seriesNoStretch;
	private XYChart.Series<Number, Number> seriesMediumStretch;
	private XYChart.Series<Number, Number> seriesMaximumStretch;
	private XYChart.Series<Number, Number> seriesDisturbance;
	private XYChart.Series<Number, Number> seriesSheard;
	private XYChart.Series<Number, Number> seriesUnknown;

	
	private FeatureVector[] plottedDataSet;
	private boolean dataSetIsExtended;
	
	private int featureIndexAxisX;
	private int featureIndexAxisY;
	
	/**
	 * Creates a new scatter chart populator and initializes the data series for
	 * each label used in this application.
	 * 
	 * @param scatterChart The ScatterChart object that is populated by this object.
	 */
	public FabricClassificationScatterChartPopulator(ScatterChart<Number, Number> scatterChart) {
		this.dataSetIsExtended = false;
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
	 * Sets the data set to be displayed on the scatter chart populated by this object and updates the visualization.
	 * Thereby resets the plot to not show an additional data point.
	 * @param plottedDataSet The data set to be displayed in the scatter chart.
	 */
	public void setPlottedDataSet(FeatureVector[] plottedDataSet) {
		this.plottedDataSet = plottedDataSet;
		this.dataSetIsExtended = false;
		populateScatterChart();
	}

	/**
	 * Sets the index determining which feature is displayed on the x axis from the feature vectors currently displayed
	 * and updates the visualization.
	 * @param index Value for the index.
	 */
	public void setXIndex(int index) {
		this.featureIndexAxisX = index;
		populateScatterChart();
	}

	/**
	 * Sets the index determining which feature is displayed on the y axis from the feature vectors currently displayed
	 * and updates the visualization.
	 * @param index Value for the index.
	 */
	public void setYIndex(int index) {
		this.featureIndexAxisY = index;
		populateScatterChart();
	}

	/**
	 * (Re-)Populates the scatter plot with the current data set.
	 */
	private void populateScatterChart() {
		clearDataSeries();
		
		for (FeatureVector featureVector : plottedDataSet) {
			addVectorToCorrectSeries(featureVector);
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
	@SuppressWarnings("unchecked") // The series are completely encapsulated and can, thereby, be processed.
	private void addDataSeriesToChart() {
		scatterChart.getData().addAll(seriesNoStretch);
		scatterChart.getData().addAll(seriesMediumStretch);
		scatterChart.getData().addAll(seriesMaximumStretch);
		scatterChart.getData().addAll(seriesDisturbance);
		scatterChart.getData().addAll(seriesSheard);
		scatterChart.getData().addAll(seriesUnknown);
	}
	
	/**
	 * Adds the provided vector to the scatter chart temporarily. The data point is removed when the original 
	 * data set changes or a new feature vector is added to the scatter chart.
	 * @param vector The vector to show on the scatter chart.
	 */
	public void includeInScatterChartTemporarily(FeatureVector vector) {
		if (plottedDataSet == null) {
			plottedDataSet = new FeatureVector[] {vector};
		} else {
			if (!dataSetIsExtended) {
				plottedDataSet = (FeatureVector[])Arrays.copyOf(plottedDataSet, plottedDataSet.length+1);
			}
			plottedDataSet[plottedDataSet.length-1] = vector;
		}
		dataSetIsExtended = true;
		populateScatterChart();
	}
}
