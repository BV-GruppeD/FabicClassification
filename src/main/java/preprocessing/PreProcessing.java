package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;

public class PreProcessing {
	private final MorphologicalFiltering edgeDetection;
	
	public PreProcessing() {
		edgeDetection = new MorphologicalFiltering();
	}

	public void execute(ImageData imageData) {
		Binarization.execute(imageData, 2);
		edgeDetection.execute(imageData);
		//TODO make all preprocessing methods return void?I mean we never use the result and it is kind of confusing
	}
}
