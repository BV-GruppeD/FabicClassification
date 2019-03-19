package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;

//TODO comment
public abstract class Binarization {
	private static final int THRESHOLDINC = 128, WHITE = 0xFFFFFF, BLACK = 0x000000;


	/**
	 *
	 * @param imageData
	 * @param methode 0 = selfimplementation, 1: Threshhold = Mean, 2: Threshhold = MaxEntropy
	 * @return binarized ImageData object result depends on slected function
	 */
	public static ImageData execute(ImageData imageData, int methode) {
		ImageProcessor imageProcessor = imageData.getImageProcessor();


		switch (methode) {

			case 0:
				for (int x = 0; x < imageProcessor.getWidth(); x++) {
					for (int y = 0; y < imageProcessor.getHeight(); y++) {

						int binarizedPixelValue = imageProcessor.get(x,y) >= THRESHOLDINC ? WHITE : BLACK;
						imageProcessor.set(x, y, binarizedPixelValue);

					}
				}
				break;

			case 1:
				imageData.getImageProcessor().setAutoThreshold(AutoThresholder.Method.Mean, false);
				break;

			case 2:
				imageData.getImageProcessor().setAutoThreshold(AutoThresholder.Method.MaxEntropy, true);

			default:
				break;

		}


		return imageData;
	}
}
