package test;

import test.DilateAndErode;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

import com.bv_gruppe_d.imagej.ImageData;

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

		ArrayList<ArrayList<Point>> segments = new Segmenter(30).execute(new ImageData(ip, null));// DBG
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		Object lock = new Object();

		int ns = Math.min(segments.size(), 10000000);
		for (int i = 0; i < ns; ++i) {
			ArrayList<Point> segment = segments.get(i);
			int r = 0;
			int g = (int) (0xff * Math.random());
			int b = (int) (0xff * Math.random());
			int color = (r << 16) + (g << 8) + b;
			for (Point p : segment) {
				ip.set(p.x, p.y, color);
			}

			Thread t = new Thread() {
				public void run() {
					HoughTransformation ht = new HoughTransformation(4, 2.0, 4, 100);
					ArrayList<EllipsisData> ellipsisList = ht.findEllipsis(segment);
					System.out.println(ellipsisList.size() + " results");

					int draw = 1;
					Collections.sort(ellipsisList);
					int cnt = Math.min(ellipsisList.size(), 3);
					synchronized (lock) {
						for (int i = 0; i < cnt; ++i) {
							System.out.println("=================================================");
							EllipsisData e = ellipsisList.get(i);

							if (i < draw) {
								e.drawTo(ip);
							}
							System.out.println(e.center + " " + e.a + "," + e.b + " " + e.orientation + "  vote_count="
									+ e.accumulator + " removed=" + e.removed);
						}
					}
				}
			};
			threads.add(t);
		}

		// start all threads
		for (Thread t : threads) {
			t.start();
		}

		// wait for all threads to finish
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public int setup(String arg0, ImagePlus img) {
		return DOES_RGB + DOES_8G;
	}
}
