package classification;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bv_gruppe_d.imagej.ImageData;

import classification.Misc.Accumulator;
import classification.Misc.ConstPoint;
import classification.Misc.EllipsisData;
import ij.process.ImageProcessor;
import preprocessing.Segmenter;

public class HoughTransformation {
	private final int accumulatorThreshold;
	private final double accumulatorAccuracy;
	private final double minMajor, maxMajor;
	private final double minMinor, maxMinor;
	private static final boolean DEBUG = false;

	public HoughTransformation(int accumulatorThreshold, double accumulatorAccuracy, int min, int max) {
		minMajor = min;
		maxMajor = max;
		minMinor = min;
		maxMinor = max;
		this.accumulatorAccuracy = accumulatorAccuracy;
		this.accumulatorThreshold = accumulatorThreshold;
	}

	public List<EllipsisData> execute(ImageData imageData) {
		ArrayList<ArrayList<Point>> segments = new Segmenter(30).execute(imageData);
		ArrayList<Thread> threads = new ArrayList<Thread>();

		Object lock = new Object();
		ArrayList<EllipsisData> foundEllipsisList = new ArrayList<>();

		int ns = Math.min(segments.size(), 10000000);
		for (int i = 0; i < ns; ++i) {
			ArrayList<Point> segment = segments.get(i);
			if (DEBUG) {
				int r = 0;
				int g = (int) (0xff * Math.random());
				int b = (int) (0xff * Math.random());
				int color = (r << 16) + (g << 8) + b;
				ImageProcessor ip = imageData.getImageProcessor();
				for (Point p : segment) {
					ip.set(p.x, p.y, color);
				}
			}

			Thread t = new Thread() {
				public void run() {
					ArrayList<EllipsisData> ellipsisList = findEllipsis(segment);
					if (!ellipsisList.isEmpty()) {
						int draw = 1;
						Collections.sort(ellipsisList);
						synchronized (lock) {
							// Use the best candidate
							foundEllipsisList.add(ellipsisList.get(0));

							if (DEBUG) {
								System.out.println(ellipsisList.size() + " results");

								int cnt = Math.min(ellipsisList.size(), 3);
								for (int i = 0; i < cnt; ++i) {
									System.out.println("=================================================");
									EllipsisData e = ellipsisList.get(i);

									if (i < draw) {
										e.drawTo(imageData.getImageProcessor());
									}
									System.out.println(e.center + " " + e.a + "," + e.b + " " + e.orientation
											+ "  vote_count=" + e.accumulator + " removed=" + e.removed);
								}
							}else {
								System.out.print(".");
								System.out.flush();
							}
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
		/*
		 * The logic is in its own class so that this method can be called on the same
		 * object on multiple threads
		 */
		InternalLogic logic = new InternalLogic(edgePixels);
		logic.run();
		return logic.results;
	}

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
}
