package classification;

import java.util.ArrayList;

// Loosely based on https://github.com/scikit-image/scikit-image/blob/master/skimage/transform/_hough_transform.pyx
public class HoughTransformation {// TODO use camel case
	private final int acc_threshold;
	private final int min_major_length;
	private final int wanted_max_minor_length;
	private final double acc_bin_size;
	private double max_b_squared;
	private final ArrayList<ConstPoint> pixels;
	private ArrayList<EllipsisData> results;
	private double a, orientation;// to avoid passing them as arguments
	private ConstPoint center;

	public HoughTransformation() {
		this(4, 1.0, 4, Integer.MAX_VALUE);// Taken from the python as sane defaults
	}

	public HoughTransformation(int acc_threshold, double acc_accuracy, int min_major_length, int max_minor_length) {
		this.acc_threshold = acc_threshold;
		this.min_major_length = min_major_length;
		this.wanted_max_minor_length = max_minor_length;

		this.acc_bin_size = acc_accuracy * acc_accuracy;
		this.pixels = new ArrayList<>();// edge pixels
	}

	public ArrayList<EllipsisData> findEllipsis(boolean[][] isEdge) {
		// Remove old data
		pixels.clear();
		results = new ArrayList<>();

		// <= half image size
		int maxDim = Math.max(isEdge.length, isEdge[0].length);
		double max_minor_length = Math.min(wanted_max_minor_length, (maxDim + 1) / 2);
		max_b_squared = max_minor_length * max_minor_length;

		// find edge pixels
		for (int a = 0; a < isEdge[0].length; a++) {// TODO check
			for (int b = 0; b < isEdge.length; b++) {
				if (isEdge[b][a]) {
					pixels.add(new ConstPoint(b, a));
				}
			}
		}

		// iterate over all possible combinations of p1 and p2
		int startSize = pixels.size();
		for (int p1 = 0; p1 < pixels.size(); ++p1) {
			System.out.println("Testing point " + p1 + "/" + startSize);
			for (int p2 = 0; p2 < p1; ++p2) {
				if (internal_stuff(pixels.get(p1), pixels.get(p2))) {
					//removed p1, p2 and p3
					// p1 < p2
					p1 -= 2;
					break;
				}
			}
		}

		pixels.clear(); // To save RAM
		return results;
	}

	private boolean internal_stuff(ConstPoint p1, ConstPoint p2) {
		// # Candidate: center (xc, yc) and main axis a
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		a = 0.5 * Math.sqrt(dx * dx + dy * dy);
		if (a > 0.5 * min_major_length) {
			ArrayList<Double> accumulator = new ArrayList<>();

//			System.out.println(p1+" "+p2+" "+center);
			center = new ConstPoint(0.5 * (p1.x + p2.x), 0.5 * (p1.y + p2.y));

			for (ConstPoint p3 : pixels) {
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

			orientation = Math.atan2(p1.x - p2.x, p1.y - p2.y);
			return handle_accumulator(accumulator);
		}
		return false;
	}

	private boolean handle_accumulator(ArrayList<Double> accumulator) {
		if (!accumulator.isEmpty()) {
			// A dynamically sized accumulator

			double bin_max_center = 0;
			int bin_max_count = 0;

			double max = 0;
			for (double d : accumulator) {
				max = Math.max(max, d);
			}

			int bin_count = 1 + (int) (max / acc_bin_size);
			int[] bins = new int[bin_count];

//			System.out.println("accu_size="+accumulator.size()+"  bin_count="+bin_count);

			for (double d : accumulator) {
				int index = (int) (d / acc_bin_size);
				bins[index] += 1;
			}

			for (int i = 0; i < bins.length; ++i) {
				if (bins[i] > bin_max_count) {
					bin_max_count = bins[i];
					bin_max_center = i;// max index
				}
			}

			bin_max_center = (bin_max_center + 0.5) * acc_bin_size; // convert index to bin center

			if (bin_max_count > acc_threshold) {
				double b = bin_max_center;

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
				results.add(new EllipsisData(center, a, b, bin_max_count, orientation));
				return true;
			}
		}
		return false;
	}

	public static final class EllipsisData implements Comparable<EllipsisData> {
		public final int accumulator;
		public final double a, b, orientation;
		public final ConstPoint center;

		public EllipsisData(ConstPoint center, double a, double b, int accumulator, double orientation) {
			this.center = center;
			this.a = a;
			this.b = b;
			this.accumulator = accumulator;
			this.orientation = orientation;
		}

		@Override
		public int compareTo(EllipsisData o) {
			return o.accumulator - accumulator;
		}
	}

	public static final class ConstPoint {
		public final double x, y;

		public ConstPoint(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}
	}
}
