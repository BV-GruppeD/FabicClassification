package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;

public interface IPreprocessingOperation {
	ImageData execute(ImageData imageData);
}
