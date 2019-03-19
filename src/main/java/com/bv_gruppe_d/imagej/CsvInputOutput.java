package com.bv_gruppe_d.imagej;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import classification.ClassifierTestMapping;
import classification.FeatureVector;

public class CsvInputOutput {
	public static void write(String filePath, FeatureVector[] data) throws IOException {
		CsvListWriter writer = new CsvListWriter(new FileWriter(filePath), CsvPreference.STANDARD_PREFERENCE);
		try {
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
					elements.add(v.getLable().toString());
					for (double d : v.getFeatureValues()) {
						elements.add(Double.toString(d));
					}
					writer.write(elements);
				}
			}
			writer.flush();
		} finally {
			writer.close();
		}
	}

	public static FeatureVector[] read(String filePath) throws IOException {
		CsvListReader listReader = new CsvListReader(new FileReader(filePath), CsvPreference.STANDARD_PREFERENCE);
		List<FeatureVector> featureVectors = new LinkedList<FeatureVector>();
		try {
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
				Lable label = Lable.valueOf(line.get(0));
				double[] values = new double[valueCount];
				for (int i = 0; i < valueCount; ++i) {
					values[i] = Double.valueOf(line.get(i + valueOffset));
				}
				featureVectors.add(new FeatureVector(names, values, label));
			}
		} finally {
			listReader.close();
		}
		return featureVectors.toArray(new FeatureVector[featureVectors.size()]);
	}
	
	public static void writeGridSearchLog(String filePath, List<ClassifierTestMapping> parameterResultMap) {
		try (CsvListWriter writer = new CsvListWriter(new FileWriter(filePath), CsvPreference.STANDARD_PREFERENCE)){
			if (parameterResultMap != null && parameterResultMap.size() >= 0) {
				// write the header
				List<String> elements = new ArrayList<String>();
				elements.add("nu");
				elements.add("Gamma");
				elements.add("Crossvalidation Classification Rate");
				writer.write(elements);

				// Write the entries
				for (ClassifierTestMapping mapping : parameterResultMap) {
					elements.clear();
					elements.add(Double.toString(mapping.getNu()));
					elements.add(Double.toString(mapping.getGamma()));
					elements.add(Double.toString(mapping.getClassificationRate()));
					writer.write(elements);
				}
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}