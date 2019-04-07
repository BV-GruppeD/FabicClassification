package com.bv_gruppe_d.imagej;

/**
 * Provides hyperparameter for the computation process, which were defined problem specific by
 * manual testing. 
 */
public class Hyperparameter {

	/**
	 * How many votes the maximum of the accumulator needs to be considered a valid
	 * ellipsis. Increasing this number leads to fewer ellipses. Choosing a too
	 * small number will make almost anything like an ellipsis
	 */
	public static final int HOUGH_ACCUMULATOR_THRESHOLD = 2;

	/**
	 * How big the bin size of the accumulator is. The bigger it is the less
	 * accurate the ellipsis parameters, but if it is chosen too small no ellipsis
	 * will be found
	 */
	public static final double HOUGH_ACCUMULATOR_BIN_SIZE = 0.25;

	/**
	 * How small the minor (small) axis of an ellipsis may be. Smaller ellipses will
	 * be ignored
	 */
	public static final double HOUGH_ELLIPSIS_AXIS_MIN = 4;

	/**
	 * How big the major (big) axis of an ellipsis may be. Bigger ellipses will be
	 * ignored
	 */
	public static final double HOUGH_ELLIPSIS_AXIS_MAX = 100;
	
}
