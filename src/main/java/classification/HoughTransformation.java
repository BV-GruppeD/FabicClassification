package classification;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bv_gruppe_d.imagej.ImageData;

import preprocessing.Segmenter;

/**
 * This class uses the hough transformation to extract ellipsis information from an edge image
 * @author Patrick
 */
public class HoughTransformation {
	private final int accumulatorThreshold;
	private final double accumulatorAccuracy;
	private final double minMajor, maxMajor;
	private final double minMinor, maxMinor;

	public HoughTransformation(int accumulatorThreshold, double accumulatorAccuracy, int min, int max) {
		minMajor = min;
		maxMajor = max;
		minMinor = min;
		maxMinor = max;
		this.accumulatorAccuracy = accumulatorAccuracy;
		this.accumulatorThreshold = accumulatorThreshold;
	}

	public List<EllipsisData> execute(ImageData imageData) {
		// Split the image into segments (each ellipse is one segment) to speed up calculation time and improve accuracy
		ArrayList<ArrayList<Point>> segments = new Segmenter(30).execute(imageData);
		ArrayList<Thread> threads = new ArrayList<Thread>();

		Object lock = new Object();
		ArrayList<EllipsisData> foundEllipsisList = new ArrayList<>();

		// Create a new thread for every segment. The best result will be stored in foundEllipsisList
		for (int i = 0; i < segments.size(); ++i) {
			ArrayList<Point> segment = segments.get(i);
			
			Thread t = new Thread() {
				public void run() {
					ArrayList<EllipsisData> ellipsisList = findEllipsis(segment);
					if (!ellipsisList.isEmpty()) {
						Collections.sort(ellipsisList);
						synchronized (lock) {
							// Use the best candidate
							foundEllipsisList.add(ellipsisList.get(0));

							System.out.print(".");
							System.out.flush();
						}
					}
				}
			};
			threads.add(t);
		}

		// start all threads
		for (Thread t : threads) {
			t.start();
		}

		// wait for all threads to finish
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Hough done");
		return foundEllipsisList;
	}

	public ArrayList<EllipsisData> findEllipsis(ArrayList<Point> edgePixels) {
		InternalLogic logic = new InternalLogic(edgePixels);
		logic.run();
		return logic.results;
	}

	/**
	 * The logic is in its own class so that the findEllipsis method can be called on the same
	 * object on multiple threads
	 */
	private class InternalLogic {
		private int i1, i2, i3;// indices of points

		private ArrayList<Point> edgePixels;
		private ArrayList<EllipsisData> results;
		private Accumulator accumulator;

		public InternalLogic(ArrayList<Point> edgePixels) {
			this.edgePixels = edgePixels;
			results = new ArrayList<>();
			accumulator = new Accumulator(minMinor, maxMinor, accumulatorAccuracy);
		}

		public void run() {
			// iterate over all possible combinations of p1 and p2
			while (i1 < edgePixels.size()) {
				if (edgePixels.get(i1) != null) {
					i2 = 0;
					while (i2 < i1) {
						if (edgePixels.get(i2) != null) {
							findEllipsis();
						}
						i2++;
					}
				}
				i1++;
			}
		}

		private void findEllipsis() {
			Point p1 = edgePixels.get(i1);
			Point p2 = edgePixels.get(i2);

			double dx = p1.x - p2.x;
			double dy = p1.y - p2.y;
			double a = 0.5 * Math.sqrt(dx * dx + dy * dy);
			if (a >= minMajor && a < maxMajor) {
				Point2D.Double center = new Point2D.Double(0.5 * (p1.x + p2.x), 0.5 * (p1.y + p2.y));

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
								double b = Math.sqrt(a * a * d * d * (1.0 - cos_tau_squared) / k);
								if (b >= minMinor && b < maxMinor) {
									accumulator.add(b);
								}
							}
						}
					}
				}

				Accumulator.Max max = accumulator.findMaxAndClear();
				if (max.votes > accumulatorThreshold) {
					double orientation = Math.atan2(p1.x - p2.x, p1.y - p2.y);
					double b = max.center;

					EllipsisData e = new EllipsisData(center, a, b, max.votes, orientation);
					results.add(e);
				}
			}
		}
	}
	
	/**
	 * An accumulator used to vote for an ellipsis parameter
	 * @author Patrick
	 */
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
				// Value is not between min and max, so we can savely ignore it
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
