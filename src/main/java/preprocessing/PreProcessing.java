package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;

public class PreProcessing {
	private final Binarization binarization;
	private final MorphologicalFiltering edgeDetection;
	
	public PreProcessing() {
		binarization = new Binarization();
		edgeDetection = new MorphologicalFiltering();
	}

	public void execute(ImageData imageData) {
		binarization.execute(imageData);
		edgeDetection.execute(imageData);
		//TODO make all preprocessing methods return void?I mean we never use the result and it is kind of confusing
	}
}
