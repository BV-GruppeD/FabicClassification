package preprocessing;

import com.bv_gruppe_d.imagej.Hyperparameter;
import com.bv_gruppe_d.imagej.ImageData;
import ij.process.ImageProcessor;

/**
 * Provides methods to adjust the contrast of an image. 
 */
public abstract class ContrastAdjustment {
	
	static final int max = 255, min = 0;
	
	/**
	 * Adjusts the pixel values of the provided image and clamps the result with a saturation of 5%.
	 * @param imageData The image to process.
	 * @return An image with adjusted pixel values.
	 */
	public static ImageData execute(ImageData imageData) {
		
		int[] histogram = imageData.getImageProcessor().getHistogram();
		int imageHeight = imageData.getImageProcessor().getHeight();
		int imageWidth = imageData.getImageProcessor().getWidth();
		ImageProcessor imageProcessor = imageData.getImageProcessor();
		
		int calculatedLow = calculateLowestModifiedPixelValue(imageProcessor, histogram);
		int calculatedHigh = calculateHighestModifiedPixelValue(imageProcessor, histogram);
		
		double scalingFactor = (double)(max) / (calculatedHigh - calculatedLow);	
			for (int h = 0; h < imageHeight; h++) {
				for (int w = 0; w < imageWidth; w++) {
					int oldPixelValue = imageProcessor.getPixel(w, h);
					
					int newPixelValue = (int) (min + (oldPixelValue - calculatedLow) * scalingFactor);
					newPixelValue = Math.min(Math.max(min, newPixelValue), max);
					imageProcessor.putPixel(w, h, newPixelValue);
				}
		}
		
		return imageData;
	}

	/**
	 * Chooses the maximum pixel value from original image
	 * @param histogram
	 * @return
	 */
	protected static int getMaxValueOfHistogram(int[] histogram) {
		int pixelValue = 255;
		while(histogram[pixelValue] == 0)
			pixelValue--;
		return pixelValue;
	}

	/**
	 * Chooses the minimum pixel value from original image
	 * @param histogram
	 * @return minimum pixel value
	 */
	protected static int getMinValueOfHistogram(int[] histogram) {
		int pixelValue = 0;
		while(histogram[pixelValue] == 0)
			pixelValue++;
		return pixelValue;
	}

	/**
	 * Calculates the new highest pixel value
	 * @param imageProcessor
	 * @param histogram
	 * @return return The new highest value as int
	 */
	private static int calculateLowestModifiedPixelValue(ImageProcessor imageProcessor, int[] histogram) {
		int modifiedPixelValue = min;
		int border = (int) Math.ceil(Hyperparameter.SATURATION * imageProcessor.getHeight() * imageProcessor.getWidth());
		int sum = 0;
		
		while(sum < border) {
			sum += histogram[modifiedPixelValue];
			modifiedPixelValue++;
		}
		return modifiedPixelValue;
	}

	/**
	 * Calculates the new lowest pixel value
	 * @param imageProcessor
	 * @param histogram
	 * @return return the new lowest pixel value as int
	 */
	private static int calculateHighestModifiedPixelValue(ImageProcessor imageProcessor, int[] histogram) {
		int modifiedPixelValue = max;
		int border = (int) (Hyperparameter.SATURATION * imageProcessor.getHeight() * imageProcessor.getWidth());
		int sum = 0;

		while(sum < border) {
			sum += histogram[modifiedPixelValue];
			modifiedPixelValue--;
		}
		return modifiedPixelValue;
	}
}
