package classification;

import java.util.ArrayList;

import classification.HoughTransformation.ConstPoint;
import classification.HoughTransformation.EllipsisData;
import ij.process.ImageProcessor;

public class Hough2 {
	private int i1, i2, i3;// indices of points
	private final ArrayList<ConstPoint> edgePixels;
	private ArrayList<EllipsisData> results;
	private Accumulator accumulator;
	private final int acc_threshold;
	private final int min_major_length;
	private final int wanted_max_minor_length;
	private final double acc_bin_size;
	private double max_b_squared;
	private ImageProcessor ip;

	public Hough2(int acc_threshold, double acc_accuracy, int min_major_length, int max_minor_length) {
		this.acc_threshold = acc_threshold;
		this.min_major_length = min_major_length;
		this.wanted_max_minor_length = max_minor_length;

		this.acc_bin_size = acc_accuracy * acc_accuracy;
		accumulator = new Accumulator(0, max_minor_length, acc_bin_size);
		edgePixels = new ArrayList<>();
		results = new ArrayList<>();
	}

	public ArrayList<EllipsisData> findEllipsis(ImageProcessor ip, boolean[][] isEdge) {
		this.ip = ip;

		// Remove old data
		edgePixels.clear();
		results = new ArrayList<>();

		// <= half image size
		int maxDim = Math.max(isEdge.length, isEdge[0].length);
		double max_minor_length = Math.min(wanted_max_minor_length, (maxDim + 1) / 2);
		max_b_squared = max_minor_length * max_minor_length;

		// find edge pixels
		for (int a = 0; a < isEdge[0].length; a++) {// TODO check
			for (int b = 0; b < isEdge.length; b++) {
				if (isEdge[b][a]) {
					edgePixels.add(new ConstPoint(b, a));
				}
			}
		}

		// iterate over all possible combinations of p1 and p2
		run();

		edgePixels.clear(); // To save RAM
		return results;
	}

	void run() {
		while (i1 < edgePixels.size()) {
			if (edgePixels.get(i1) != null) {
				i2 = 0;
				while (i2 < i1) {
					if (edgePixels.get(i2) != null) {
						if (stuff()) {
							break;
						}
					}
					i2++;
				}
			}
			i1++;
		}
	}

	private boolean stuff() {
		ConstPoint p1 = edgePixels.get(i1);
		ConstPoint p2 = edgePixels.get(i2);

		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		double a = 0.5 * Math.sqrt(dx * dx + dy * dy);
		if (a > 0.5 * min_major_length) {
//			System.out.println(p1+" "+p2+" "+center);
			ConstPoint center = new ConstPoint(0.5 * (p1.x + p2.x), 0.5 * (p1.y + p2.y));

			for (i3 = 0; i3 < edgePixels.size(); ++i3) {
				ConstPoint p3 = edgePixels.get(i3);
				if (p3 != null) {
					dx = p3.x - center.x;
					dy = p3.y - center.y;
					double d = Math.sqrt(dx * dx + dy * dy);
					if (d > min_major_length) {
						dx = p3.x - p1.x;
						dy = p3.y - p1.y;
						double cos_tau_squared = ((a * a + d * d - dx * dx - dy * dy) / (2.0 * a * d));
						cos_tau_squared *= cos_tau_squared;
						// # Consider b2 > 0 and avoid division by zero
						double k = a * a - d * d * cos_tau_squared;
						if (k > 0.0 && cos_tau_squared < 1.0) {
							double b_squared = a * a * d * d * (1.0 - cos_tau_squared) / k;
							// # b2 range is limited to avoid histogram memory
							// # overflow
							if (b_squared <= max_b_squared) {
								accumulator.add(b_squared);
							}
						}
					}
				}
			}

			classification.Hough2.Accumulator.Max max = accumulator.findMaxAndClear();
			if (max.votes > acc_threshold) {
				double orientation = Math.atan2(p1.x - p2.x, p1.y - p2.y);

				double b = max.center;

				// Start TODO do we need this?
				// # to keep ellipse_perimeter() convention
				if (orientation != 0) {
					orientation = Math.PI - orientation;
					// # When orientation is not in [-pi:pi]
					// # it would mean in ellipse_perimeter()
					// # that a < b. But we keep a > b.
					if (orientation > Math.PI) {
						orientation = orientation - Math.PI / 2.0;
						double tmp = a;
						a = b;
						b = tmp;
					}
				}
				// end
				addResult(center, a, b, orientation, max.votes);
				return true;
			}
		}
		return false;
	}

	void addResult(ConstPoint center, double a, double b, double rot, int votes) {
		EllipsisData e = new EllipsisData(center, a, b, votes, rot);

		ArrayList<ConstPoint> el = e.points();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (ConstPoint p : el) {
			int i = edgePixels.indexOf(p);
			if (i >= 0) {
				indices.add(i);
			}
		}

		if (indices.size() > 5) {
			for (Integer i : indices) {
				edgePixels.set(i, null);
			}
			
			for (ConstPoint p : el) {
				int x = (int) (p.x + 0.5);
				int y = (int) (p.y + 0.5);
				try {
					ip.set(x, y, ip.get(x, y) | 0xff0000);
				} catch (Exception ex) {
				}
			}
			results.add(e);
		}
//		System.out.println("Removed " + removedCount + " points that were on the ellipsis");

		edgePixels.set(i1, null);
		edgePixels.set(i2, null);

		e.removed = indices.size();
	}

	private static class Accumulator {
		private final double binSize, minValue;
		private final int[] counts;
		private int maxUsedIndex;

		public Accumulator(double minValue, double maxValue, double binSize) {
			this.minValue = minValue;
			this.binSize = binSize;
			maxUsedIndex = 0;
			counts = new int[binIndex(maxValue) + 1];
		}

		private int binIndex(double value) {
			return (int) ((value - minValue) / binSize);
		}

		public void add(double value) {
			int index = binIndex(value);
			try {
				counts[index]++;
				maxUsedIndex = Math.max(maxUsedIndex, index);
			} catch (ArrayIndexOutOfBoundsException ex) {
				// TODO what?
//				ex.printStackTrace();
			}
		}

		public Max findMaxAndClear() {
			int max = 0, maxi = 0;
			for (int i = 0; i < maxUsedIndex; ++i) {
				if (counts[i] > max) {
					maxi = i;
					max = counts[i];
					counts[i] = 0;
				}
			}
			maxUsedIndex = 0;
			return new Max(max, minValue + (maxi + 0.5) * binSize);
		}

		public static class Max {
			public final int votes;
			public final double center;

			private Max(int votes, double center) {
				super();
				this.votes = votes;
				this.center = center;
			}
		}
	}
}