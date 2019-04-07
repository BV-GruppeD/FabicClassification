package utilities;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.bv_gruppe_d.imagej.Label;

import classification.ClassifierTestMapping;
import featureextraction.FeatureVector;

/**
 * Provides methods to store and load feature vectors to a csv file and to log the results of a grid search.
 */
public class CsvInputOutput {
	/**
	 * Writes the provided feature vectors to a file at the provided location.
	 * @param filePath The location to write to.
	 * @param data The data to be written.
	 * @throws IOException Thrown when the process failed
	 */
	public static void write(String filePath, FeatureVector[] data) throws IOException {
		try (CsvListWriter writer = new CsvListWriter(new FileWriter(filePath), CsvPreference.STANDARD_PREFERENCE)){
			if (data != null && data.length >= 0) {
				// write the header
				List<String> elements = new ArrayList<String>();
				elements.add("Label");
				for (String name : data[0].getFeatureNames()) {
					elements.add(name);
				}
				writer.write(elements);

				// Write the entries
				for (FeatureVector v : data) {
					elements.clear();
					elements.add(v.getLabel().toString());
					for (double d : v.getFeatureValues()) {
						elements.add(Double.toString(d));
					}
					writer.write(elements);
				}
			}
			writer.flush();
		}
	}

	/**
	 * Reads a set of feature vectors from a file at the provided location.
	 * @param data The data to be written.
	 * @return An array of feature vectors created from the file.
	 * @throws IOException Thrown when the process failed.
	 */
	public static FeatureVector[] read(String filePath) throws IOException {
		List<FeatureVector> featureVectors = new LinkedList<FeatureVector>();
		try (CsvListReader listReader = new CsvListReader(new FileReader(filePath), CsvPreference.STANDARD_PREFERENCE)){
			// read the header			
			List<String> line = listReader.read();
			final int valueOffset = 1;// since the index 0 is for the label
			final int columnCount = line.size();
			final int valueCount = columnCount - 1;
			String[] names = new String[valueCount];
			for (int i = 0; i < valueCount; ++i) {
				names[i] = line.get(i + valueOffset);
			}

			// read the data rows
			while ((line = listReader.read()) != null) {
				if (line.size() != columnCount) {
					throw new RuntimeException(
							"Line column count does not match the header: '" + listReader.getUntokenizedRow() + "'");
				}
				Label label = Label.valueOf(line.get(0));
				double[] values = new double[valueCount];
				for (int i = 0; i < valueCount; ++i) {
					values[i] = Double.valueOf(line.get(i + valueOffset));
				}
				featureVectors.add(new FeatureVector(names, values, label));
			}
		} 
		return featureVectors.toArray(new FeatureVector[featureVectors.size()]);
	}
	
	/**
	 * Creates a file containing the list of parameters with their corresponding results at the provided location.
	 * @param filePath The location to save the file to.
	 * @param parameterResultMap The list of mappings to write.
	 */
	public static void writeGridSearchLog(String filePath, List<ClassifierTestMapping> parameterResultMap) {
		try (CsvListWriter writer = new CsvListWriter(new FileWriter(filePath), CsvPreference.STANDARD_PREFERENCE)){
			if (parameterResultMap != null && parameterResultMap.size() >= 0) {
				// write the header
				List<String> elements = new ArrayList<String>();
				elements.add("nu");
				elements.add("Gamma");
				elements.add("F-Measure");
				writer.write(elements);

				// Write the entries
				for (ClassifierTestMapping mapping : parameterResultMap) {
					elements.clear();
					elements.add(Double.toString(mapping.getNu()));
					elements.add(Double.toString(mapping.getGamma()));
					elements.add(Double.toString(mapping.getFMeasure()));
					writer.write(elements);
				}
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
