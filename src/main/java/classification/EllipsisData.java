package classification;

import java.awt.geom.Point2D;

/**
 * An object that contains all the information about an ellipsis. Can be compared to find which is a better match (smaller = better)
 */
public class EllipsisData implements Comparable<EllipsisData> {
	public final int accumulator;
	public final double a, b, orientation;
	public final Point2D.Double center;

	// TODO: Add parameter description
	public EllipsisData(Point2D.Double center, double a, double b, int accumulator, double orientation) {
		this.center = center;
		this.a = a;
		this.b = b;
		this.accumulator = accumulator;
		this.orientation = orientation;
	}

	//TODO: Appeal to last paragraph of the javadoc of the overriden method(Contains a recommended warning because equals and compare should have some relation.
	@Override
	public int compareTo(EllipsisData o) {
		int score = o.accumulator - accumulator;
		return score;
	}
}