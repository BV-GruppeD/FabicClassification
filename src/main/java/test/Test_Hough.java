package test;

import test.DilateAndErode;

import java.util.ArrayList;

import classification.HoughTransformation;
import classification.HoughTransformation.EllipsisData;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import test.DilateAndErode.StructureElement;

public class Test_Hough implements PlugInFilter {

	@Override
	public void run(ImageProcessor ip) {
		HoughTransformation ht = new HoughTransformation();
		DilateAndErode.color2gray(ip);
		DilateAndErode.binarize(ip, 0x80);

		StructureElement edgeDetection = DilateAndErode.createCenteredSquare(3);
		ImageProcessor other = ip.duplicate();
		DilateAndErode.dilate(ip, other, edgeDetection);
		DilateAndErode.xor(other, ip);

		int w = ip.getWidth(), h = ip.getHeight();
		boolean[][] isEdge = new boolean[w][h];
		for (int x = 0; x < w; ++x) {
			for (int y = 0; y < h; ++y) {
				isEdge[x][y] = ip.get(x, y) >= 0x80;
			}
		}

		ArrayList<EllipsisData> ellipsisList = ht.findEllipsis(isEdge);
		for (EllipsisData e : ellipsisList) {
			System.out.println(e.xc + "," + e.yc + " " + e.a + "," + e.b);
		}
	}

	@Override
	public int setup(String arg0, ImagePlus img) {
		return DOES_RGB;
	}
}
