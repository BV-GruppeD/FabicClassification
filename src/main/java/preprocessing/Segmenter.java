package preprocessing;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import com.bv_gruppe_d.imagej.ImageData;

import ij.process.ImageProcessor;

public class Segmenter {
	private final int minSize;

	public Segmenter(int minSize) {
		this.minSize = minSize;
	}

	public ArrayList<ArrayList<Point>> execute(ImageData imageData) {
		ArrayList<ArrayList<Point>> shapeList = new ArrayList<>();
		ImageProcessor ip = imageData.getImageProcessor();

		EdgeMap edgeMap = new EdgeMap(ip.getWidth(), ip.getHeight());
		for (int x = 0; x < ip.getWidth(); ++x) {
			for (int y = 0; y < ip.getHeight(); ++y) {
				boolean isEdge = (ip.get(x, y) & 0xff) >= 0x80;
//				ip.set(x, y, isEdge ? 0x00ffff : 0x000000);
				if (isEdge) {
					edgeMap.setEdge(x, y, isEdge);
				}
			}
		}

		Point start;
		while ((start = edgeMap.getRandomPoint()) != null) {
			// Breadth first search
			ArrayList<Point> shape = new ArrayList<Point>();
			LinkedList<Point> checkNext = new LinkedList<Point>();
			checkNext.add(start);
			edgeMap.setEdge(start.x, start.y, false);

			final int w = edgeMap.getWidth(), h = edgeMap.getHeight();
			while (!checkNext.isEmpty()) {
				Point current = checkNext.removeFirst();
				shape.add(current);

				// check neighbors
				for (int dx = -1; dx < 2; dx++) {
					for (int dy = -1; dy < 2; dy++) {
						int x = current.x + dx;
						int y = current.y + dy;
						if (x >= 0 && x < w && y >= 0 && y < h && edgeMap.isEdge(x, y)) {
							checkNext.addLast(new Point(x, y));
							edgeMap.setEdge(x, y, false);
						}
					}
				}
			}
			if (shape.size() > minSize) {
				shapeList.add(shape);
			}
		}

		System.out.println("Found " + shapeList.size() + " segments");
		return shapeList;
	}

	private static class EdgeMap {
		private final boolean[][] isEdge;
		private final int width, height;
		private int edgeCount;

		public EdgeMap(int width, int height) {
			this.width = width;
			this.height = height;
			isEdge = new boolean[width][height];
			edgeCount = 0;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public boolean isEdge(int x, int y) {
			return isEdge[x][y];
		}

		public void setEdge(int x, int y, boolean newValue) {
			boolean oldValue = isEdge[x][y];
			if (oldValue != newValue) {
				isEdge[x][y] = newValue;
				if (newValue) {
					edgeCount++;
				} else {
					edgeCount--;
				}
			}
		}

		public Point getRandomPoint() {
			if (edgeCount <= 0) {
				return null;
			}

			for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					if (isEdge[x][y]) {
						return new Point(x, y);
					}
				}
			}

			throw new IllegalStateException("edgeCount > 0, but no edge found");
		}
	}
}
