package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;

public abstract class PreProcessing {
	
	public PreProcessing() {
	}

	public static ImageData execute(ImageData imageData) {
		ContrastAdjustment.execute(imageData);
		Binarization.execute(imageData);
		return MorphologicalFiltering.execute(imageData);
		//TODO make all preprocessing methods return void?I mean we never use the result and it is kind of confusing
		//return imageData;
	}
}
