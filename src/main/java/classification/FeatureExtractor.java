package classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bv_gruppe_d.imagej.ImageData;

import classification.Misc.EllipsisData;

public class FeatureExtractor {
	public FeatureVector execute(ImageData imageData, List<EllipsisData> ellipsisList) {
		int size = ellipsisList.size();
		ArrayList<Double> ratios = new ArrayList<>(size);
		ArrayList<Double> areas = new ArrayList<>(size);
		for (EllipsisData e : ellipsisList) {
			ratios.add(Math.max(e.a / e.b, e.b / e.a)); //make sure it is (bigger / smaller) radius
			areas.add(Math.PI * e.a * e.b);
		}
		
		double ratio = median(ratios);
		double area = median(areas);
		double ellipsisPerArea = size / (double) imageData.getImageProcessor().getPixelCount();
		double[] features = {ratio, area, ellipsisPerArea};
		return new FeatureVector(features, imageData.getLable());
	}
	
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
			return 0.5 * (values.get(center) + values.get(center + 1));
		}
	}
}
