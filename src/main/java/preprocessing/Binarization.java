package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;
import ij.process.ImageProcessor;

//TODO comment
public class Binarization {
	private static final int THRESHOLDINC = 128, WHITE = 0xFFFFFF, BLACK = 0x000000;

	public ImageData execute(ImageData imageData) {
		ImageProcessor imageProcessor = imageData.getImageProcessor();

		//imageData.getImageProcessor().setAutoThreshold(AutoThresholder.Method.Mean, false);


		for (int x = 0; x < imageProcessor.getWidth(); x++) {
			for (int y = 0; y < imageProcessor.getHeight(); y++) {

				int binarizedPixelValue = imageProcessor.get(x,y) >= THRESHOLDINC ? WHITE : BLACK;
				imageProcessor.set(x, y, binarizedPixelValue);

			}
		}

		return imageData;
	}
}
