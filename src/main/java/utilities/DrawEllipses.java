package utilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.List;

import featureextraction.EllipsisData;
import ij.process.ImageProcessor;

public class DrawEllipses {
	
	/**
	 * Draws the provided ellipses on the image in the image processor and thereby creates a new RGB image.
	 * @param ip The original image
	 * @param ellipses The ellipses to draw
	 * @return A colored copy of the original with ellipses drawn on it
	 */
	public static BufferedImage drawEllipses(ImageProcessor ip, List<EllipsisData> ellipses) {
		BufferedImage grayscaleImage = ip.getBufferedImage();
		BufferedImage imageWithRedEllipses = new BufferedImage(grayscaleImage.getWidth(), grayscaleImage.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = imageWithRedEllipses.createGraphics();
		g.drawImage(grayscaleImage, 0, 0, null);// Copy grayscale to the color image
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(2));

		for (EllipsisData e : ellipses) {
			drawEllipsis(g, e.center.x, e.center.y, e.a, e.b, e.orientation);
		}
		g.dispose();
		return imageWithRedEllipses;
	}

	/**
	 * Draws an ellipsis to the Graphics2D object
	 */
	private static void drawEllipsis(Graphics2D g, double cx, double cy, double a, double b, double rot) {
		Ellipse2D e = new Ellipse2D.Double(cx - a, cy - b, 2 * a, 2 * b);

		AffineTransform rotateArroundEllipsisCenter = new AffineTransform();
		rotateArroundEllipsisCenter.rotate( - rot - 0.5 * Math.PI, cx, cy);

		g.setTransform(rotateArroundEllipsisCenter);
		g.draw(e);
	}
}
