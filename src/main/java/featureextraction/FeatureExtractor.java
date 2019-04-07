package featureextraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bv_gruppe_d.imagej.ImageData;

/**
 * This class converts a list of ellipses to a single FeatureVector. If
 * necessary a median is used, which has the advantage that partial ellipses (at
 * the edges of the image) don't distort the result.
 */
public class FeatureExtractor {
	
	/**
	 * This method converts a list of ellipses to a single FeatureVector. If
	 * necessary a median is used, which has the advantage that partial ellipses (at
	 * the edges of the image) don't distort the result.
	 */
	@SuppressWarnings("unused")
	public FeatureVector execute(ImageData imageData, List<EllipsisData> ellipsisList) {
		int size = ellipsisList.size();
		ArrayList<Double> ratios = new ArrayList<>(size);
		ArrayList<Double> areas = new ArrayList<>(size);
		ArrayList<Double> as = new ArrayList<>(size);
		ArrayList<Double> bs = new ArrayList<>(size);
		double totalEllipseArea = 0;
		for (EllipsisData e : ellipsisList) {
			double a = e.a, b = e.b;
			// make sure it is bigger radius divided by smaller radius
			ratios.add(Math.max(a / b, b / a));
			areas.add(Math.PI * a * b);
			as.add(a);
			bs.add(b);
			totalEllipseArea += a * b;
		}
		totalEllipseArea *= Math.PI;// do this later for numeric purposes

		double ratio = median(ratios);
		double area = median(areas);
		double medianA = median(as);
		double medianB = median(bs);
		double ellipsisCoverage = totalEllipseArea / (double) imageData.getImageProcessor().getPixelCount();

		String[] featureNames = {  "a", "b", "ellipsis coverage" };
		double[] features = {  medianA, medianB, ellipsisCoverage };
		return new FeatureVector(featureNames, features, imageData.getLabel());
	}

	/**
	 * 
	 * @param values the list of values
	 * @return the average of the list of values or 0 if the list is empty
	 */
	public static double average(ArrayList<Double> values) {
		if (values.isEmpty()) {
			return 0;
		}

		double sum = 0;
		for (double d : values) {
			sum += d;
		}
		return sum / values.size();
	}

	/**
	 * 
	 * @param values the list of values
	 * @return the median of the list of values or 0 if the list is empty
	 */
	public static double median(ArrayList<Double> values) {
		if (values.isEmpty()) {
			return 0;
		}

		Collections.sort(values);
		int size = values.size();
		int center = size / 2;
		if (size % 2 == 1) {
			// uneven
			return values.get(center);
		} else {
			// even
			return 0.5 * (values.get(center - 1) + values.get(center));
		}
	}
}
