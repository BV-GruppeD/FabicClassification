package ui;

import java.util.ArrayList;
import java.util.Arrays;

import com.bv_gruppe_d.imagej.Lable;

import classification.FeatureVector;
import ij.IJ;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;

public class FabricClassificationScatterChartPopulator {


	private ScatterChart<Number,Number> scatterChart;
	
	private XYChart.Series<Number, Number> seriesNoStretch;
	private XYChart.Series<Number, Number> seriesMediumStretch;
	private XYChart.Series<Number, Number> seriesMaximumStretch;
	private XYChart.Series<Number, Number> seriesDisturbance;
	private XYChart.Series<Number, Number> seriesSheard;  
	private XYChart.Series<Number, Number> seriesUnknown;
	
	
	public FabricClassificationScatterChartPopulator(ScatterChart<Number, Number> scatterChart) {
		this.scatterChart = scatterChart;
		
		initializeDataSeries(); 
	}

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

	public void populateScatterChartWithExampleData() {
		ArrayList<FeatureVector> exampleVectors = new ArrayList<>(Arrays.asList(
				new FeatureVector(new double[] {1,1}, Lable.NO_STRETCH),
				new FeatureVector(new double[] {1.05,0.95}, Lable.NO_STRETCH),
				new FeatureVector(new double[] {2,1}, Lable.MEDIUM_STRETCH),
				new FeatureVector(new double[] {2.05,0.95}, Lable.MEDIUM_STRETCH),
				new FeatureVector(new double[] {3,1}, Lable.MAXIMUM_STRECH),
				new FeatureVector(new double[] {3.05,0.95}, Lable.MAXIMUM_STRECH),
				new FeatureVector(new double[] {1.05,0.2}, Lable.DISTURBANCE),
				new FeatureVector(new double[] {1,0.1}, Lable.DISTURBANCE),
				new FeatureVector(new double[] {0.5,1.5}, Lable.SHEARD),
				new FeatureVector(new double[] {0.5,1.95}, Lable.SHEARD)
				
			));	
		scatterChart.setTitle("Example data");
		
		populateScatterChart(exampleVectors.toArray(new FeatureVector[] {}),"Axis Ratio", "Number of Ellipses");
	}
	
	public void populateScatterChart(FeatureVector[] featureVectors, String xLabel, String yLabel) {
		
		scatterChart.getXAxis().setLabel(xLabel);
		scatterChart.getYAxis().setLabel(yLabel);

		
		for (FeatureVector featureVector : featureVectors) {
			if (featureVector.getFeatureValues().length == 2) {
				addVectorToCorrectSeries(featureVector);
			} else {
				IJ.showMessage("Der Datensatz kann nicht in einem 2D-Graphen angezeigt werden.");
			}
		}
		addDataSeriesToChart();
	}

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
	
	private void addDataSeriesToChart() {
		scatterChart.getData().addAll(seriesNoStretch);
		scatterChart.getData().addAll(seriesMediumStretch);
		scatterChart.getData().addAll(seriesMaximumStretch);
		scatterChart.getData().addAll(seriesDisturbance);
		scatterChart.getData().addAll(seriesSheard);
		scatterChart.getData().addAll(seriesUnknown);
	}
}
