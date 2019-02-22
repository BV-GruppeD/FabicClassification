package test;

import classification.HoughTransformation.ConstPoint;
import test.DilateAndErode;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

import com.bv_gruppe_d.imagej.ImageData;

import classification.Hough2;
import classification.HoughTransformation;
import classification.HoughTransformation.EllipsisData;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import preprocessing.Segmenter;
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
//		DilateAndErode.invert(ip);
//		DilateAndErode.close(ip, DilateAndErode.createCenteredSquare(10));

		StructureElement edgeDetection = DilateAndErode.createCenteredSquare(3);
		ImageProcessor other = ip.duplicate();
		DilateAndErode.erode(ip, other, edgeDetection);
		DilateAndErode.xor(other, ip);
//		DilateAndErode.invert(ip);

//		int w = ip.getWidth(), h = ip.getHeight();
//		boolean[][] isEdge = new boolean[w][h];
//		for (int x = 0; x < w; ++x) {
//			for (int y = 0; y < h; ++y) {
//				isEdge[x][y] = (ip.get(x, y) & 0xff) >= 0x80;
//				ip.set(x, y, isEdge[x][y] ? 0x00ff00 : 0x000000);
//			}
//		}

		ArrayList<ArrayList<Point>> segments = new Segmenter(30).execute(new ImageData(ip, null));// DBG
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for (ArrayList<Point> segment : segments) {
			int r = 0;
			int g = (int) (0xff*Math.random());
			int b = (int) (0xff*Math.random());
			int color = (r << 16) + (g << 8) + b; 
			for (Point p : segment) {
				ip.set(p.x, p.y, color);
			}
			
			Thread t = new Thread() {
				public void run() {
					ArrayList<EllipsisData> ellipsisList = ht.findEllipsis(ip, segment);
					System.out.println(ellipsisList.size() + " results");

					int draw = 5;
					Collections.sort(ellipsisList);
					int cnt = Math.min(ellipsisList.size(), 30);
					for (int i = 0; i < cnt; ++i) {
						EllipsisData e = ellipsisList.get(i);

						if (i < draw) {
							e.drawTo(ip);
						}
						System.out.println(e.center + " " + e.a + "," + e.b + " " + e.orientation + "  vote_count="
								+ e.accumulator + " removed=" + e.removed);
					}
				}
			};
			threads.add(t);
		}
		
//		//start all threads
//		for (Thread t : threads) {
//			t.start();
//		}
//		
//		//wait for all threads to finish
//		for (Thread t : threads) {
//			try {
//				t.join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	public int setup(String arg0, ImagePlus img) {
		return DOES_RGB + DOES_8G;
	}
}
