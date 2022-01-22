import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static java.awt.Color.blue;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 * 
 * @author Chris Bailey-Kellogg, Winter 2014 (based on a very different structure from Fall 2012)
 * @author Travis W. Peters, Dartmouth CS 10, Updated Winter 2015
 * @author CBK, Spring 2015, updated for CamPaint
 */
public class RegionFinderExtraCr {
	private static final int maxColorDiff = 2000;				// how similar a pixel color must be to the target color, to belong to a region
	private static final int minRegion = 50; 				// how many points in a region to be worth considering

	private BufferedImage image;                            // the image in which to find regions
	private BufferedImage recoloredImage;                   // the image with identified regions recolored
	private BufferedImage helperImage;

	private ArrayList<ArrayList<Point>> regions;			// a region is a list of points
															// so the identified regions are in a list of lists of points

	public RegionFinderExtraCr() {
		this.image = null;
	}

	public RegionFinderExtraCr(BufferedImage image) {
		this.image = image;		
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}

	public BufferedImage getRecoloredImage() {
		return recoloredImage;
	}

	/**
	 * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
	 */
	public ArrayList<Point> regionGrowth(Point point, Color targetColor) {
		ArrayList<Point> visited = new ArrayList<Point>();
		ArrayList<Point> toVisit = new ArrayList<Point>();
		toVisit.add(point);
		while (!toVisit.isEmpty()) {
			Point newPoint = toVisit.get(0);
			visited.add(newPoint);
			toVisit.remove(0);
			for (int x = Math.max((int) newPoint.getX() - 1, 0); x <= Math.min(newPoint.getX() + 1, image.getWidth() - 1) ; x++) {
				for (int y = Math.max((int) newPoint.getY() - 1, 0); y <= Math.min(newPoint.getY() + 1, image.getHeight() - 1); y++) {
					Color c = new Color(helperImage.getRGB(x, y));
					if (colorMatch(c, targetColor)) {
						toVisit.add(new Point(x, y));
						helperImage.setRGB(x, y, 0);
					}
				}
			}
		}
		return visited;
	}

	public void findRegions(Color targetColor) {
		helperImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
		this.regions = new ArrayList<ArrayList<Point>>();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Color c = new Color(image.getRGB(x,y));
				if (colorMatch(c, targetColor)) {
					ArrayList<Point> newRegion = new ArrayList<>();
					newRegion = regionGrowth(new Point(x, y), targetColor);
					regions.add(newRegion);
				}
			}
		}
	}

	/**
	 * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary).
	 */
	private static boolean colorMatch(Color c1, Color c2) {
		int d = (c1.getRed() - c2.getRed()) * (c1.getRed() - c2.getRed())
				+ (c1.getGreen() - c2.getGreen()) * (c1.getGreen() - c2.getGreen())
				+ (c1.getBlue() - c2.getBlue()) * (c1.getBlue() - c2.getBlue());
		return d <= maxColorDiff;
	}

	/**
	 * Returns the largest region detected (if any region has been detected)
	 */
	public ArrayList<Point> largestRegion() {
		if (regions.isEmpty()) {
			return null;
		}
		ArrayList<Point> largestList = regions.get(0);
		for (int x = 1; x < regions.size() - 1; x++) {
			if (regions.get(x).size() > largestList.size()) {
				largestList = regions.get(x);
			}
		}
		return largestList;
	}

	/**
	 * Sets recoloredImage to be a copy of image, 
	 * but with each region a uniform random color, 
	 * so we can see where they are
	 */
	public void recolorImage() {
		// First copy the original
		recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
		// Now recolor the regions in it
		if (largestRegion() != null) {
			for (Point point : largestRegion()) {
				recoloredImage.setRGB((int) point.getX(), (int) point.getY(), blue.getRGB());
			}
		}
	}
}
