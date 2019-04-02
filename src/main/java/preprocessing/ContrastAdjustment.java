package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;
import ij.process.ImageProcessor;

public abstract class ContrastAdjustment {
	
	public static ImageData execute(ImageData imageData) {
		
		int[] histogram = imageData.getImageProcessor().getHistogram();		
		int histogramMin = getMinValueOfHistogram(histogram);
		int histoframMax = getMaxValueOfHistogram(histogram);
		int imageHeight = imageData.getImageProcessor().getHeight();
		int imageWidth = imageData.getImageProcessor().getWidth();
		ImageProcessor imageProcessor = imageData.getImageProcessor();
		
		
		double scalingFactor = (double)(histoframMax - histogramMin) / (histoframMax - histogramMin);
				
				for (int h = 0; h < imageHeight; h++) {
					for (int w = 0; w < imageWidth; w++) {
						int oldPixelValue = imageProcessor.getPixel(w, h);
						
						int newPixelValue = (int) (histogramMin + (oldPixelValue - histogramMin) * scalingFactor);
						newPixelValue = Math.min(Math.max(0, newPixelValue), 255);
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

}
