package classification;

/**
 * An accumulator used by the hough transformation to vote for an ellipsis parameter
 */
public class Accumulator {
	private final double binSize, minValue;
	private final int[] counts;
	private int maxUsedIndex;

	// TODO: Add javadoc with parameter explanation. 
	// Maybe (Possibly) its because of me but i am not so familiar with the concept of an accumulator and would not directly now whats the meaning of the parameters.
	public Accumulator(double minValue, double maxValue, double binSize) {
		this.minValue = minValue;
		this.binSize = binSize;
		maxUsedIndex = 0;
		counts = new int[binIndex(maxValue) + 1];
	}

	private int binIndex(double value) {
		return (int) ((value - minValue) / binSize);
	}

	// TODO: Add javadoc because its a public method and because the name says add but the method actually increases a counter
	public void add(double value) {
		int index = binIndex(value);
		try {
			counts[index]++;
			maxUsedIndex = Math.max(maxUsedIndex, index);
		} catch (ArrayIndexOutOfBoundsException ex) {
			// Value is not between min and max, so we can savely ignore it
		}
	}

	// TODO: Add javadoc because its a public method and because the name says add but the method actually increases a counter
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

	// TODO: Add comment to explain the construct of a static class with a constructor who explicitly calls the super constructor
	// And tells what this structure is the maximum of
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
