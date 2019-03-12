package preprocessing;

import com.bv_gruppe_d.imagej.ImageData;
import ij.process.ImageProcessor;

public class MorphologicalFiltering
{

	private final StructureElement edgeDetection = createCenteredSquare(5);
	// TODO: closing Size
	private final StructureElement closeHoles = createCenteredSquare(2);

	public enum Type {
		ERODE, DILATE
	}

	private static final int WHITE = 0xFFFFFF, BLACK = 0x000000;


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

	public ImageData execute(ImageData imageData) {

		Binarization binarization = new Binarization();
		binarization.execute(imageData);
		invert(imageData.getImageProcessor());
		close(imageData.getImageProcessor(), closeHoles);
		ImageProcessor other = imageData.getImageProcessor().duplicate();
		dilate(imageData.getImageProcessor(), other, edgeDetection);
		xor(other, imageData.getImageProcessor());


		return null;//TODO shouldn't it return imageData?
	}

	public static void invert(ImageProcessor output) {
		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				boolean a = output.get(x, y) > 127;
				output.set(x, y, (!a) ? WHITE : BLACK);
			}
		}
	}

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

	public static void close(ImageProcessor output, StructureElement structureElement) {
		dilate(output.duplicate(), output, structureElement);
		erode(output.duplicate(), output, structureElement);
	}

	public static void dilate(ImageProcessor input, ImageProcessor output, StructureElement structureElement) {
		applyOperation(input, output, structureElement, Type.DILATE);
	}

	public static void erode(ImageProcessor input, ImageProcessor output, StructureElement structureElement) {
		applyOperation(input, output, structureElement, Type.ERODE);
	}

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
			// TODO Do we need to mirror the mask? (wg faltung)
			return mask[h - (y + 1)][w - (x + 1)];
		}

		public int getWidth() {
			return w;
		}

		public int getHeight() {
			return h;
		}
	}

	private static void applyOperation(ImageProcessor input, ImageProcessor output, StructureElement structureElement,
									   Type type) {
		int thresholdInclusive = -1;// just so java does not complain
		if (type == Type.ERODE) {
			int sum = 0;
			for (int x = 0; x < structureElement.getWidth(); x++) {
				for (int y = 0; y < structureElement.getHeight(); y++) {
					if (structureElement.get(x, y)) {
						sum++;
					}
				}
				thresholdInclusive = sum;// All pixels must be set
			}
		} else if (type == Type.DILATE) {
			thresholdInclusive = 1;// Only 1 pixel needs to be set
		} else {
			throw new RuntimeException("Unknown type: " + type);
		}

		int maxX = input.getWidth() - structureElement.getWidth();// off by 1?
		int maxY = input.getHeight() - structureElement.getHeight();// off by 1?

		// TODO what to do with the border pixels?
		// This just sets them to black
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
