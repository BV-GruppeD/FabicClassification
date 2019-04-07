package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;
import ij.process.ImageProcessor;

// TODO: Add comment
public abstract class ContrastAdjustment {
	
	// TODO: move to hyperparameter class and add explenation to values
	static double saturation = 0.05;
	static final int max = 255, min = 0;
	
	public static ImageData execute(ImageData imageData) {
		
		int[] histogram = imageData.getImageProcessor().getHistogram();
		// TODO: remove unused variables and probably remove methods if not used
		int histogramMin = getMinValueOfHistogram(histogram);
		int histoframMax = getMaxValueOfHistogram(histogram);
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

	protected static int getMaxValueOfHistogram(int[] histogram) {
		int pixelValue = 255;
		while(histogram[pixelValue] == 0)
			pixelValue--;
		return pixelValue;
	}

	protected static int getMinValueOfHistogram(int[] histogram) {
		int pixelValue = 0;
		while(histogram[pixelValue] == 0)
			pixelValue++;
		return pixelValue;
	}

	// TODO: Add comment
	private static int calculateLowestModifiedPixelValue(ImageProcessor imageProcessor, int[] histogram) {
		int modifiedPixelValue = min;
		int border = (int) Math.ceil(saturation * imageProcessor.getHeight() * imageProcessor.getWidth());
		int sum = 0;
		
		while(sum < border) {
			sum += histogram[modifiedPixelValue];
			modifiedPixelValue++;
		}
		return modifiedPixelValue;
	}


	// TODO: Add comment
	private static int calculateHighestModifiedPixelValue(ImageProcessor imageProcessor, int[] histogram) {
		int modifiedPixelValue = max;
		int border = (int) (saturation * imageProcessor.getHeight() * imageProcessor.getWidth());
		int sum = 0;

		while(sum < border) {
			sum += histogram[modifiedPixelValue];
			modifiedPixelValue--;
		}
		return modifiedPixelValue;
	}
}
