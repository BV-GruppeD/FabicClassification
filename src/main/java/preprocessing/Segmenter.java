package preprocessing;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import com.bv_gruppe_d.imagej.ImageData;

import ij.process.ImageProcessor;

/**
 * This class splits a image into multiple segments A segment is a list of
 * pixels which are connected. All segments with less elements than minSize are
 * ignored
 * 
 * The image is interpreted as a undirected graph, where each white pixel is a
 * node and all white pixels that are directly next to each other (horizontal or
 * vertical) are connected by an edge. Each segment is a connected subgraph.
 */
public class Segmenter {
	private final int minSize;
	// TODO: Add Comment
	public Segmenter(int minSize) {
		this.minSize = minSize;
	}
	
	// TODO: Add Comment
	public ArrayList<ArrayList<Point>> execute(ImageData imageData) {
		ArrayList<ArrayList<Point>> shapeList = new ArrayList<>();
		EdgeMap edgeMap = createEdgeMap(imageData.getImageProcessor());

		Point start;
		// While there are unassigned edge points, chose one at random
		while ((start = edgeMap.getRandomPoint()) != null) {
			// Then add all the neighbors using a breadth first search
			ArrayList<Point> shape = breathFirstSearch(edgeMap, start);

			// Ignore any shapes that are to small
			if (shape.size() > minSize) {
				shapeList.add(shape);
			}
		}

		System.out.println("Found " + shapeList.size() + " segments");
		return shapeList;
	}

	private EdgeMap createEdgeMap(ImageProcessor ip) {
		final int w = ip.getWidth(), h = ip.getHeight();

		EdgeMap edgeMap = new EdgeMap(w, h);
		for (int x = 0; x < w; ++x) {
			for (int y = 0; y < h; ++y) {
				boolean isEdge = (ip.get(x, y) & 0xff) >= 0x80;// check if it is white
				if (isEdge) {
					edgeMap.setEdge(x, y, isEdge);
				}
			}
		}
		return edgeMap;
	}

	private ArrayList<Point> breathFirstSearch(EdgeMap edgeMap, Point start) {
		ArrayList<Point> shape = new ArrayList<Point>();
		LinkedList<Point> checkNext = new LinkedList<Point>();
		checkNext.add(start);
		edgeMap.setEdge(start.x, start.y, false);

		final int w = edgeMap.getWidth(), h = edgeMap.getHeight();
		while (!checkNext.isEmpty()) {
			Point current = checkNext.removeFirst();
			shape.add(current);

			// check neighbors. the point itself will be checked too, but since isEdge will return false for it that is no problem
			for (int dx = -1; dx < 2; dx++) {
				for (int dy = -1; dy < 2; dy++) {
					int x = current.x + dx;
					int y = current.y + dy;

					boolean isInBounds = x >= 0 && x < w && y >= 0 && y < h;
					if (isInBounds && edgeMap.isEdge(x, y)) {
						checkNext.addLast(new Point(x, y));
						edgeMap.setEdge(x, y, false);
					}
				}
			}
		}
		return shape;
	}
}
