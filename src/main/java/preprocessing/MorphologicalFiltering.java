package preprocessing;

import com.bv_gruppe_d.imagej.Hyperparameter;
import com.bv_gruppe_d.imagej.ImageData;
import ij.process.ImageProcessor;

/**
 * This class provides morphological filtering. It uses squared masks and works also erode and dilate.
 * By running the execute-method this class works with object from input and manipulates on the original object.
 */
public abstract class MorphologicalFiltering
{
	private static final int WHITE = 0xFFFFFF, BLACK = 0x000000;

	private static final StructureElement edgeDetection = createCenteredSquare(Hyperparameter.MASK_SIZE);
	private static final StructureElement closeHoles = createCenteredSquare(Hyperparameter.MASK_SIZE);

	public enum Type {
		ERODE, DILATE
	}

	/**
	 * Set the size for an structure element.
	 * @param size
	 * @return
	 */
	private static StructureElement createCenteredSquare(int size) {
		return new StructureElement(createSquareMask(size), size / 2, size / 2);
	}

	private static boolean[][] createSquareMask(int size) {
		boolean[][] mask = new boolean[size][size];
		for (int a = 0; a < mask.length; ++a) {
			boolean[] array = mask[a];
			for (int b = 0; b < array.length; ++b) {
				array[b] = true;
			}
		}
		return mask;
	}

	/**
	 * Execute methode - should be used for filtering. The ImageProcessor of the manipulated ImageData object
	 * gets black and with withe
	 * @param imageData
	 * @return returns a copy of the input ImageData object. The edges on the image a filtered.
	 */
	public static ImageData execute(ImageData imageData) {

		Binarization.execute(imageData);
		invert(imageData.getImageProcessor());
		close(imageData.getImageProcessor(), closeHoles);
		ImageProcessor other = imageData.getImageProcessor().duplicate();
		dilate(imageData.getImageProcessor(), other, edgeDetection);
		xor(other, imageData.getImageProcessor());
		imageData = new ImageData(removeBorder(imageData).getImageProcessor(), imageData.getLabel());

		return imageData;
	}

	/**
	 * This methode simply inverts every single pixel value in the image.
	 * @param output input ImageProcessor
	 */
	public static void invert(ImageProcessor output) {
		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				boolean a = output.get(x, y) > 127;
				output.set(x, y, (!a) ? WHITE : BLACK);
			}
		}
	}

	/**
	 * A logic XOR operation on every single pixel, between inverted closed and one extra dilated image
	 * @param input
	 * @param output
	 */
	public static void xor(ImageProcessor input, ImageProcessor output) {
		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				boolean a = input.get(x, y) > 127;
				boolean b = output.get(x, y) > 127;
				boolean aXORb = (a && !b) || (!a && b);
				output.set(x, y, aXORb ? WHITE : BLACK);
			}
		}
	}

	public static void open(ImageProcessor output, StructureElement structureElement) {
		erode(output.duplicate(), output, structureElement);
		dilate(output.duplicate(), output, structureElement);
	}

	/**
	 * opens the ellipses by first dilate and than erode the objects on the image
	 * @param output
	 * @param structureElement
	 */
	public static void close(ImageProcessor output, StructureElement structureElement) {
		dilate(output.duplicate(), output, structureElement);
		erode(output.duplicate(), output, structureElement);
	}

	/**
	 * Dilate operation on binary image
	 * @param input
	 * @param output
	 * @param structureElement
	 */
	public static void dilate(ImageProcessor input, ImageProcessor output, StructureElement structureElement) {
		applyOperation(input, output, structureElement, Type.DILATE);
	}

	/**
	 * Erode operation on binary image
	 * @param input
	 * @param output
	 * @param structureElement
	 */
	public static void erode(ImageProcessor input, ImageProcessor output, StructureElement structureElement) {
		applyOperation(input, output, structureElement, Type.ERODE);
	}

	/**
	 * Provides a structure element for opening an closing.
	 */
	public static class StructureElement {
		private final boolean[][] mask;
		private final int anchorX, anchorY;
		private final int w, h;

		public StructureElement(boolean[][] mask, int anchorX, int anchorY) {
			this.mask = mask;
			this.anchorX = anchorX;
			this.anchorY = anchorY;
			w = mask.length;
			h = mask[0].length;

			if (anchorX < 0 || anchorX >= getWidth()) {
				throw new RuntimeException("anchorX out of bounds");
			}
			if (anchorY < 0 || anchorY >= getHeight()) {
				throw new RuntimeException("anchorY out of bounds");
			}
		}

		public boolean get(int x, int y) {
			return mask[h - (y + 1)][w - (x + 1)];
		}

		public int getWidth() {
			return w;
		}

		public int getHeight() {
			return h;
		}
	}

	/**
	 * After filtering this method removes the white border
	 * @param imageData
	 * @return
	 */
	public static ImageData removeBorder(ImageData imageData) {
		
		final int imageWidth = imageData.getImageProcessor().getWidth()-2*Hyperparameter.MASK_SIZE;
		final int imageHeight = imageData.getImageProcessor().getHeight()-2*Hyperparameter.MASK_SIZE;
		
		ImageData image = imageData.duplicate();
		image.getImageProcessor().setRoi(Hyperparameter.MASK_SIZE, Hyperparameter.MASK_SIZE, imageWidth, imageHeight);
		imageData = new ImageData(image.getImageProcessor().crop(), image.getLabel());
		
		return imageData;		
	}

	/**
	 * Executes the method which is inserted as parameter
	 * @param input
	 * @param output
	 * @param structureElement
	 * @param type
	 */
	private static void applyOperation(ImageProcessor input, ImageProcessor output, StructureElement structureElement,
									   Type type) {
		int thresholdInclusive = -1;
		if (type == Type.ERODE) {
			int sum = 0;
			for (int x = 0; x < structureElement.getWidth(); x++) {
				for (int y = 0; y < structureElement.getHeight(); y++) {
					if (structureElement.get(x, y)) {
						sum++;
					}
				}
				thresholdInclusive = sum;
			}
		} else if (type == Type.DILATE) {
			thresholdInclusive = 1;
		} else {
			throw new RuntimeException("Unknown type: " + type);
		}

		int maxX = input.getWidth() - structureElement.getWidth();// off by 1?
		int maxY = input.getHeight() - structureElement.getHeight();// off by 1?

		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				output.set(x, y, BLACK);
			}
		}

		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				int sum = 0;
				for (int i = 0; i < structureElement.getWidth(); i++) {
					for (int j = 0; j < structureElement.getHeight(); j++) {
						boolean set = (input.getPixel(x + i, y + j) > 127) && structureElement.get(i, j);
						if (set) {
							sum++;
						}
					}
				}
				int newValue = (sum >= thresholdInclusive) ? WHITE : BLACK;
				output.putPixel(x + structureElement.anchorX, y + structureElement.anchorY, newValue);
			}
		}
	}
}
