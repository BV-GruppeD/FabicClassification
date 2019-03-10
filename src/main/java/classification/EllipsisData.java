package classification;

import java.awt.geom.Point2D;

/**
 * A object that contains all the information about an ellipsis. Can be compared to find which is a better match (smaller = better)
 * @author Patrick
 *
 */
public class EllipsisData implements Comparable<EllipsisData> {
	public final int accumulator;
	public final double a, b, orientation;
	public final Point2D.Double center;

	public EllipsisData(Point2D.Double center, double a, double b, int accumulator, double orientation) {
		this.center = center;
		this.a = a;
		this.b = b;
		this.accumulator = accumulator;
		this.orientation = orientation;
	}

	@Override
	public int compareTo(EllipsisData o) {
		int score = o.accumulator - accumulator;
		return score;
	}
}