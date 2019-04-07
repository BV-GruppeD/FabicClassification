package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;


// TODO: Add Comment
public abstract class PreProcessing {
	
	public PreProcessing() {
	}
	// TODO: Add Comment
	public static ImageData execute(ImageData imageData) {
		ContrastAdjustment.execute(imageData);
		Binarization.execute(imageData);
		return MorphologicalFiltering.execute(imageData);
	}
}
