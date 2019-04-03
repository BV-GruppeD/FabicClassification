package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;

/**
 * Is an abstract class wich returns an ImageData. It offers direct manipulation of ImageData Object,
 * so it can be direct used for further process.
 *
 * This class offers the binarization of ImageProcasser in ImageData object.
 */
public abstract class Binarization {
	private static final int WHITE = 0xFFFFFF, BLACK = 0x000000;


	/**
	 * Main function, it manipulates the ImageProcessor into binary image
	 * @param imageData
	 * @return binarized ImageData object result depends on selected function
	 */
	public static ImageData execute(ImageData imageData) {
		ImageProcessor imageProcessor = imageData.getImageProcessor();

		int threshholdinc = 128;//getMean(imageProcessor);

		for (int x = 0; x < imageProcessor.getWidth(); x++) {
			for (int y = 0; y < imageProcessor.getHeight(); y++) {

				int binarizedPixelValue = imageProcessor.get(x,y) >= threshholdinc ? WHITE : BLACK;
				imageProcessor.set(x, y, binarizedPixelValue);

			}
		}
		return imageData;
	}

	/**
	 * This class calculates the mean color of all pixels
	 * @param imageProcessor
	 * @return
	 */
	private static int getMean(ImageProcessor imageProcessor) {

		int threshholdinc = 0;
		for (int x = 0; x < imageProcessor.getWidth(); x++) {
			for (int y = 0; y < imageProcessor.getHeight(); y++) {
				threshholdinc += imageProcessor.get(x,y);
			}
		}
		threshholdinc /= (imageProcessor.getWidth() * imageProcessor.getHeight());


		return threshholdinc;
	}
}
