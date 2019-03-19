package preprocessing;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import com.bv_gruppe_d.imagej.ImageData;

import ij.process.ImageProcessor;

/**
 * This class splits a image into multiple segments
 * A segment is a list of pixels which are connected. All segments with less elements than minSize are ignored
 * 
 * The image is interpreted as a undirected graph, where each 
 * white pixel is a node and all white pixels that are directly next to each other
 * (horizontal or vertical) are connected by an edge. Each segment is a connected subgraph.
 */
public class Segmenter {
	private final int minSize;

	public Segmenter(int minSize) {
		this.minSize = minSize;
	}

	public ArrayList<ArrayList<Point>> execute(ImageData imageData) {
		ArrayList<ArrayList<Point>> shapeList = new ArrayList<>();
		ImageProcessor ip = imageData.getImageProcessor();

		EdgeMap edgeMap = /**/new EdgeMap(ip.getWidth(), ip.getHeight());
		for (int x = 0; x < ip.getWidth(); ++x) {
			for (int y = 0; y < ip.getHeight(); ++y) {
				boolean isEdge = (ip.get(x, y) & 0xff) >= 0x80;
				if (isEdge) {
					edgeMap.setEdge(x, y, isEdge);
				}
			}
		}// TODO: extract method something like createEdgeMap

		Point start;
		//While there are unassigned edge points, chose one at random
		while ((start = edgeMap.getRandomPoint()) != null) {
			// Then add all the neighbors using a breadth first search
			ArrayList<Point> shape = new ArrayList<Point>();
			LinkedList<Point> checkNext = new LinkedList<Point>();
			checkNext.add(start);
			edgeMap.setEdge(start.x, start.y, false);

			// TODO: Is there a point in setting these final?
			final int w = edgeMap.getWidth(), h = edgeMap.getHeight();
			while (!checkNext.isEmpty()) {
				Point current = checkNext.removeFirst();
				shape.add(current);

				// check neighbors
				for (int dx = -1; dx < 2; dx++) {
					for (int dy = -1; dy < 2; dy++) {
						int x = current.x + dx;
						int y = current.y + dy;
						// TODO: extract boolean expression to give it a descriptive name
						if (x >= 0 && x < w && y >= 0 && y < h && edgeMap.isEdge(x, y)) {
							checkNext.addLast(new Point(x, y));
							edgeMap.setEdge(x, y, false);
						}
					}
				}
			}
			// Ignore any shapes that are to small
			if (shape.size() > minSize) {
				shapeList.add(shape);
			}
		}

		System.out.println("Found " + shapeList.size() + " segments");
		return shapeList;
	}

	
}
