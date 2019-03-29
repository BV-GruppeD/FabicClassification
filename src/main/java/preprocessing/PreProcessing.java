package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;

public class PreProcessing {
	
	public PreProcessing() {
	}

	public ImageData execute(ImageData imageData) {
		Binarization.execute(imageData);
		return MorphologicalFiltering.execute(imageData);
		//TODO make all preprocessing methods return void?I mean we never use the result and it is kind of confusing
		//return imageData;
	}
}
