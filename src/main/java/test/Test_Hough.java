package test;

import test.DilateAndErode;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.bv_gruppe_d.imagej.ImageData;

import classification.FeatureExtractor;
import classification.FeatureVector;
import classification.HoughTransformation;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import preprocessing.Segmenter;
import test.DilateAndErode.StructureElement;
import classification.Misc.*;

public class Test_Hough implements PlugInFilter {

	@Override
	public void run(ImageProcessor ip) {
		DilateAndErode.color2gray(ip);

		System.out.println(new ConstPoint(0, 0).equals(new ConstPoint(1, 1)));
		run_test(ip);
	}

	private void run_test(ImageProcessor ip) {
		DilateAndErode.binarize(ip, 80);
//		DilateAndErode.invert(ip);
//		DilateAndErode.close(ip, DilateAndErode.createCenteredSquare(10));

		StructureElement edgeDetection = DilateAndErode.createCenteredSquare(3);
		ImageProcessor other = ip.duplicate();
		DilateAndErode.erode(ip, other, edgeDetection);
		DilateAndErode.xor(other, ip);
//		DilateAndErode.invert(ip);

		HoughTransformation ht = new HoughTransformation(4, 1, 4, 100);
		ImageData id = new ImageData(ip, null);
		FeatureVector fv = new FeatureExtractor().execute(id, ht.execute(id));
		System.out.println(Arrays.toString(fv.getFeatureValues()));
	}

	@Override
	public int setup(String arg0, ImagePlus img) {
		return DOES_RGB + DOES_8G;
	}
}
