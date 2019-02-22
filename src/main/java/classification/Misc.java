package classification;

import java.util.ArrayList;
import java.util.Collections;

import ij.process.ImageProcessor;

public class Misc {
	public static final class EllipsisData implements Comparable<EllipsisData> {
		public final int accumulator;
		public final double a, b, orientation;
		public final ConstPoint center;
		public int removed;

		public EllipsisData(ConstPoint center, double a, double b, int accumulator, double orientation) {
			this.center = center;
			this.a = a;
			this.b = b;
			this.accumulator = accumulator;
			this.orientation = orientation;
		}

		@Override
		public int compareTo(EllipsisData o) {
			int score = o.accumulator - accumulator;
			if (score == 0) {
				score = o.removed - removed;
			}

//			int score = o.removed - removed;
//			if (score == 0) {
//				score = o.accumulator - accumulator;
//			}
			return score;
		}

		public ArrayList<ConstPoint> points() {
			int pointCount = 10000;
			ArrayList<ConstPoint> points = new ArrayList<ConstPoint>(pointCount);
//			double approxPerimeter = Math.PI * (3*(a+b)-Math.sqrt((3*a + b)*(a+3*b)));
			double stepWidth = 2 * Math.PI / pointCount;

			for (double r = 0; r < 2 * Math.PI; r += stepWidth) {
				double angle = r + orientation;
				double x = center.x + a * Math.cos(angle);
				double y = center.y + b * Math.sin(angle);
				ConstPoint p = new ConstPoint(x, y);
				points.add(p);
			}
			return points;
		}

		public void drawTo(ImageProcessor ip) {
			for (ConstPoint p : points()) {
				int x = (int) (p.x + 0.5);
				int y = (int) (p.y + 0.5);
				try {
					ip.set(x, y, ip.get(x, y) | 0xff0000);
				} catch (Exception ex) {
				}
			}
		}
	}

	public static final double EQUAL_DIST = 5.0;

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

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ConstPoint) {
				ConstPoint o = (ConstPoint) obj;
				double dx = o.x - x;
				double dy = o.y - y;
				double d2 = dx * dx + dy * dy;
				return d2 <= EQUAL_DIST * EQUAL_DIST;
			}
			return false;
		}
	}

	public static class Accumulator {
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
