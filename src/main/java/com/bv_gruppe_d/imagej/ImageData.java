package com.bv_gruppe_d.imagej;

import ij.process.ImageProcessor;

/**
 * A data structure for storing an image processor with the provided label.
 *
 */
public class ImageData {
	
	private ImageProcessor imageProcessor;
	private Label label;
	
	public ImageData(ImageProcessor imageProcessor, Label label) {
		this.label = label;
		this.imageProcessor = imageProcessor;
	}
	
	public ImageProcessor getImageProcessor() {
		return this.imageProcessor;
	}
	
	public Label getLabel() {
		return this.label;
	}
	
	public ImageData duplicate() {
		return new ImageData(imageProcessor.duplicate(), label);
	}
}
