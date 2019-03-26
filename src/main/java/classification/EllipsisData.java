package classification;

import java.awt.geom.Point2D;

/**
 * An object that contains all the information about an ellipsis. Can be
 * compared to find which is a better match (smaller = better)
 */
public class EllipsisData implements Comparable<EllipsisData> {
	public final int accumulator;
	public final double a, b, orientation;
	public final Point2D.Double center;

	/**
	 * 
	 * @param center the ellipsis center
	 * @param a the length of the major axis
	 * @param b the length of the minor axis
	 * @param accumulator the number of votes the maximum of the accumulator got
	 * @param orientation how the ellipse is rotated, in radiant
	 */
	public EllipsisData(Point2D.Double center, double a, double b, int accumulator, double orientation) {
		this.center = center;
		this.a = a;
		this.b = b;
		this.accumulator = accumulator;
		this.orientation = orientation;
	}

	/**
	 * Sorts them by the number of votes. The smallest item has the most votes. It
	 * is not consistent with equals since it is not required and would require a
	 * lot more code.
	 */
	@Override
	public int compareTo(EllipsisData o) {
		int score = o.accumulator - accumulator;
		return score;
	}
}