package com.bv_gruppe_d.imagej;

import ij.process.ImageProcessor;

/**
 * A data structure for storing an image processor with the provided label.
 *
 */
public class ImageData {
	
	private ImageProcessor imageProcessor;
	private Lable lable;
	
	public ImageData(ImageProcessor imageProcessor, Lable lable) {
		this.lable = lable;
		this.imageProcessor = imageProcessor;
	}
	
	public ImageProcessor getImageProcessor() {
		return this.imageProcessor;
	}
	
	public Lable getLable() {
		return this.lable;
	}
}
