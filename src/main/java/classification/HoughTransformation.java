package classification;

import java.awt.Point;
import java.util.ArrayList;

import classification.Misc.Accumulator;
import classification.Misc.ConstPoint;
import classification.Misc.EllipsisData;

public class HoughTransformation {
	private int i1, i2, i3;// indices of points
	private ArrayList<Point> edgePixels;
	private ArrayList<EllipsisData> results;
	private Accumulator accumulator;
	private final int acc_threshold;
	private final double minMajor, maxMajor;
	private final double minMinor, maxMinor;

	public HoughTransformation(int acc_threshold, double acc_bin_size, int min, int max) {
		minMajor = min;
		maxMajor = max;
		minMinor = min;
		maxMinor = max;
		this.acc_threshold = acc_threshold;

		accumulator = new Accumulator(minMinor, maxMinor, acc_bin_size);
		edgePixels = new ArrayList<>();
		results = new ArrayList<>();
	}

	public ArrayList<EllipsisData> findEllipsis(ArrayList<Point> edgePixels) {
		this.edgePixels = edgePixels;
		results = new ArrayList<>();

		run();

		edgePixels.clear(); // To save RAM
		return results;
	}

	void run() {
		// iterate over all possible combinations of p1 and p2
		while (i1 < edgePixels.size()) {
			if (edgePixels.get(i1) != null) {
				i2 = 0;
				while (i2 < i1) {
					if (edgePixels.get(i2) != null) {
						if (findEllipsis()) {
//							break;
						}
					}
					i2++;
				}
			}
			i1++;
		}
	}

	private boolean findEllipsis() {
		Point p1 = edgePixels.get(i1);
		Point p2 = edgePixels.get(i2);

		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		double a = 0.5 * Math.sqrt(dx * dx + dy * dy);
		if (a >= minMajor && a < maxMajor) {
//			System.out.println(p1+" "+p2+" "+center);
			ConstPoint center = new ConstPoint(0.5 * (p1.x + p2.x), 0.5 * (p1.y + p2.y));

			for (i3 = 0; i3 < edgePixels.size(); ++i3) {
				Point p3 = edgePixels.get(i3);
				if (p3 != null) {
					dx = p3.x - center.x;
					dy = p3.y - center.y;
					double d = Math.sqrt(dx * dx + dy * dy);
					if (d > minMinor) {
						dx = p3.x - p1.x;
						dy = p3.y - p1.y;
						double cos_tau_squared = ((a * a + d * d - dx * dx - dy * dy) / (2.0 * a * d));
						cos_tau_squared *= cos_tau_squared;
						// # Consider b2 > 0 and avoid division by zero
						double k = a * a - d * d * cos_tau_squared;
						if (k > 0.0 && cos_tau_squared < 1.0) {
							double b_squared = a * a * d * d * (1.0 - cos_tau_squared) / k;
							double b = Math.sqrt(b_squared);
							if (b >= minMinor && b < maxMinor) {
								accumulator.add(Math.sqrt(b_squared));
							}
						}
					}
				}
			}

			Accumulator.Max max = accumulator.findMaxAndClear();
			if (max.votes > acc_threshold) {
				double orientation = Math.atan2(p1.x - p2.x, p1.y - p2.y);
				double b = max.center;

				addResult(center, a, b, orientation, max.votes);
				return true;
			}
		}
		return false;
	}

	void addResult(ConstPoint center, double a, double b, double rot, int votes) {
		EllipsisData e = new EllipsisData(center, a, b, votes, rot);
		results.add(e);
	}
}
