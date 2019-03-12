package preprocessing;

import java.awt.Point;

/**
 * This class stores a modifiable edge map 
 */
public class EdgeMap {
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