package test;

import test.DilateAndErode;

import java.util.ArrayList;
import java.util.Collections;

import classification.HoughTransformation;
import classification.HoughTransformation.EllipsisData;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import test.DilateAndErode.StructureElement;

public class Test_Hough implements PlugInFilter {

	@Override
	public void run(ImageProcessor ip) {
		DilateAndErode.color2gray(ip);
		run_test(ip);
	}

	private void run_test(ImageProcessor ip) {
		HoughTransformation ht = new HoughTransformation(100, 15.0, 4, 80);
		DilateAndErode.binarize(ip, 0x80);
		DilateAndErode.invert(ip);

		StructureElement edgeDetection = DilateAndErode.createCenteredSquare(3);
		ImageProcessor other = ip.duplicate();
		DilateAndErode.erode(ip, other, edgeDetection);
		DilateAndErode.xor(other, ip);
//		DilateAndErode.invert(ip);

		int w = ip.getWidth(), h = ip.getHeight();
		boolean[][] isEdge = new boolean[w][h];
		for (int x = 0; x < w; ++x) {
			for (int y = 0; y < h; ++y) {
				isEdge[x][y] = (ip.get(x, y) & 0xff) >= 0x80;
				ip.set(x, y, isEdge[x][y] ? 0x00ff00 : 0);
			}
		}

		new Thread() {
			public void run() {
				ArrayList<EllipsisData> ellipsisList = ht.findEllipsis(isEdge);
				System.out.println(ellipsisList.size() + " results");

				Collections.sort(ellipsisList);
				int cnt = Math.min(ellipsisList.size(), 10);
				for (int i = 0; i < cnt; ++i) {
					EllipsisData e = ellipsisList.get(i);
					System.out.println(e.center + " " + e.a + "," + e.b + " " + e.orientation + "  vote_count=" + e.accumulator);
				}
			};
		}.start();
	}

	@Override
	public int setup(String arg0, ImagePlus img) {
		return DOES_RGB + DOES_8G;
	}
}
