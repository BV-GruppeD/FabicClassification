package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;


/**
 * Provides the required preprocessing for the usage of an image in the classification.
 */
public abstract class PreProcessing {
	
	/**
	 * Executes all preprocessing operations in a sequence of operations.
	 * @param imageData The image to be preprocessed.
	 * @return A preprocessed copy of the image with the same label.
	 */
	public static ImageData execute(ImageData imageData) {
		ContrastAdjustment.execute(imageData);
		Binarization.execute(imageData);
		return MorphologicalFiltering.execute(imageData);
	}
}
