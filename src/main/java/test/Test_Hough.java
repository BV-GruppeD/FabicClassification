package test;

import classification.HoughTransformation.ConstPoint;
import test.DilateAndErode;

import java.util.ArrayList;
import java.util.Collections;

import classification.Hough2;
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

		System.out.println(new ConstPoint(0, 0).equals(new ConstPoint(1, 1)));
		run_test(ip);
	}

	private void run_test(ImageProcessor ip) {
		Hough2 ht = new Hough2(40, 2.0, 20, 100);
		DilateAndErode.binarize(ip, 80);
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
				ip.set(x, y, isEdge[x][y] ? 0x00ff00 : 0x000000);
			}
		}

		Thread t = new Thread() {
			public void run() {
				ArrayList<EllipsisData> ellipsisList = ht.findEllipsis(ip, isEdge);
				System.out.println(ellipsisList.size() + " results");

				int draw = 5;
				Collections.sort(ellipsisList);
				int cnt = Math.min(ellipsisList.size(), 30);
				for (int i = 0; i < cnt; ++i) {
					EllipsisData e = ellipsisList.get(i);

					if (i < draw) {
						e.drawTo(ip);
					}
					System.out.println(e.center + " " + e.a + "," + e.b + " " + e.orientation + "  vote_count=" + e.accumulator+" removed="+e.removed);
				}
			}
		};
		boolean RUN_IN_OTHER_THREAD = false;
		if (RUN_IN_OTHER_THREAD) t.start();
		else t.run();
	}

	@Override
	public int setup(String arg0, ImagePlus img) {
		return DOES_RGB + DOES_8G;
	}
}
