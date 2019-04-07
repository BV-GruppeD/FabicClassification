package featureextraction;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bv_gruppe_d.imagej.ImageData;

import preprocessing.Segmenter;

/**
 * This class uses the Hough transformation to extract ellipsis information from
 * an edge image
 */
public class HoughTransformation {
	/**
	 * All segments shorter than this number will be ignored
	 */
	private static final int MIN_SEGMENT_SIZE = 60;
	
	private final int accumulatorThreshold;
	private final double accumulatorAccuracy;
	private final double minMajor, maxMajor;
	private final double minMinor, maxMinor;

	/**
	 * 
	 * @param accumulatorThreshold the minimum number of values in one bin to return
	 *                             a result
	 * @param accumulatorAccuracy  the bin size of the accumulator
	 * @param min                  the minimal length of the ellipsis short axis
	 *                             (edge to center)
	 * @param max                  the maximal length of the ellipsis long axis
	 *                             (edge to center)
	 */
	public HoughTransformation(int accumulatorThreshold, double accumulatorAccuracy, double min, double max) {
		minMajor = min;
		maxMajor = max;
		minMinor = min;
		maxMinor = max;
		this.accumulatorAccuracy = accumulatorAccuracy;
		this.accumulatorThreshold = accumulatorThreshold;
	}

	public List<EllipsisData> execute(ImageData imageData) {
		// Split the image into segments (each ellipse is one segment) to speed up
		// calculation time and improve accuracy
		ArrayList<ArrayList<Point>> segments = new Segmenter(MIN_SEGMENT_SIZE).execute(imageData);
		ArrayList<Thread> threads = new ArrayList<Thread>();

		Object lock = new Object();
		ArrayList<EllipsisData> foundEllipsisList = new ArrayList<>();

		// Create a new thread for every segment. The best result will be stored in
		// foundEllipsisList
		for (int i = 0; i < segments.size(); ++i) {
			ArrayList<Point> segment = segments.get(i);
			Thread t = new WorkerThread(lock, segment, foundEllipsisList);
			t.setDaemon(true);
			t.setName("HoughWorker#" + i);

			// Add the thread to the list and start it
			threads.add(t);
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
	 * A thread that tries to finds an ellipse in one segment and add it to the
	 * resultList
	 */
	private class WorkerThread extends Thread {
		private final Object lock;
		private final ArrayList<Point> segment;
		private final ArrayList<EllipsisData> resultList;

		public WorkerThread(Object lock, ArrayList<Point> segment, ArrayList<EllipsisData> resultList) {
			super();
			this.lock = lock;
			this.segment = segment;
			this.resultList = resultList;
		}

		@Override
		public void run() {
			ArrayList<EllipsisData> ellipsisList = findEllipsis(segment);
			if (!ellipsisList.isEmpty()) {
				Collections.sort(ellipsisList);
				synchronized (lock) {
					// Use the best candidate
					resultList.add(ellipsisList.get(0));

					System.out.print(".");
					System.out.flush();
				}
			}
		}
	}

	/**
	 * The logic is in its own class so that the findEllipsis method can be called
	 * on the same object on multiple threads
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

		/**
		 * For an explaination of the math see: "A New Efficient Ellipse Detection
		 * Method" (Yonghong Xie Qiang , Qiang Ji / 2002)
		 * 
		 * There is also a python implementation (as of 2019-03-20, the link may be
		 * invalidated by future updates to scikit-image):
		 * https://github.com/scikit-image/scikit-image/blob/master/skimage/transform/_hough_transform.pyx#L101
		 * 
		 */
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
}
