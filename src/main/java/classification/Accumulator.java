package classification;

/**
 * An accumulator used by the hough transformation to vote for an ellipsis
 * parameter. An accumulator does not remember exact values, instead it stores
 * its values in bins and only remembers how many entries are in each bin.
 * It is similar to a histogram.
 */
public class Accumulator {
	private final double binSize, minValue;
	private final int[] counts;
	private int maxUsedIndex;

	/**
	 * This
	 * 
	 * @param minValue the minimum value to be stored, all smaller ones are discarded. Will be the start of the first bin.
	 * @param maxValue the maximum value to be stored. It will be rounded up to the next possible end of a bucket and be used as the end of the last bin. This behavior ensures that all buckets have the same size.
	 * @param binSize how big each bin is
	 */
	public Accumulator(double minValue, double maxValue, double binSize) {
		this.minValue = minValue;
		this.binSize = binSize;
		maxUsedIndex = 0;
		counts = new int[binIndex(maxValue) + 1];
	}

	private int binIndex(double value) {
		return (int) ((value - minValue) / binSize);
	}

	/**
	 * This method adds a value to the accumulator. This is done by finding the
	 * bucket the value belongs in and increasing its counter by one
	 * 
	 * @param value The value to add
	 */
	public void add(double value) {
		int index = binIndex(value);
		try {
			counts[index]++;
			maxUsedIndex = Math.max(maxUsedIndex, index);
		} catch (ArrayIndexOutOfBoundsException ex) {
			// Value is not between min and max, so we can safely ignore it
		}
	}

	/**
	 * This method finds the maximum of the accumulator. It also resets the
	 * accumulator so that it can be used again. Combining both operations ensures
	 * the accumulator reset is not forgot and provides shorter and faster code.
	 * 
	 * @return information about the accumulators maximum
	 */
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

	/**
	 * This class represents the maximum of the accumulator. It contains information
	 * about the chosen value (center) and how many votes it got
	 */
	public static class Max {
		public final int votes;
		public final double center;

		private Max(int votes, double center) {
			this.votes = votes;
			this.center = center;
		}
	}
}
