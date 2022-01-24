import java.awt.*;
import java.awt.image.*;
import java.util.*;

import static java.awt.Color.blue;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 *
 * @author Chris Bailey-Kellogg, Winter 2014 (based on a very different structure from Fall 2012)
 * @author Max Lawrence, Winter 2022, modified the scaffold to find regions similar to a target color within a BufferedImage
 * @author Daniel Carstensen, Winter 2022
 */
public class RegionFinder {
    private static final int maxColorDiff = 2000;				// how similar a pixel color must be to the target color, to belong to a region
    private static final int minRegion = 50; 				// how many points in a region to be worth considering

    private BufferedImage image;                            // the image in which to find regions
    private BufferedImage recoloredImage;                   // the image with identified regions recolored
    private BufferedImage helperImage;

    private ArrayList<ArrayList<Point>> regions;			// a region is a list of points
    // so the identified regions are in a list of lists of points

    public RegionFinder() {
        this.image = null;
    }

    public RegionFinder(BufferedImage image) {
        this.image = image;
    }

    /**
     * Sets this.image to a new BufferedImage
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Returns the current image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Returns the current recolored image
     */
    public BufferedImage getRecoloredImage() {
        return recoloredImage;
    }

    /**
     * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
     */
    public void findRegions(Color targetColor) {
        helperImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
        this.regions = new ArrayList<ArrayList<Point>>(); // initializes regions to hold lists of regions that will be found
        for (int y = 0; y < image.getHeight(); y++) {  // loop over every pixel
            for (int x = 0; x < image.getWidth(); x++) {
                // get the color for each pixel and test it against the targetColor
                Color c = new Color(image.getRGB(x,y));
                if (colorMatch(c, targetColor)) {
                    // if the color matches, create a new region and execute the regionGrowth algorithm at the current pixel
                    ArrayList<Point> newRegion = new ArrayList<>();
                    newRegion = regionGrowth(new Point(x, y), targetColor);
                    if (newRegion.size() > minRegion) {
                        regions.add(newRegion);  // add the new region if it's as large as the minRegion threshold
                    }
                }
            }
        }
    }

    /**
     * Using a flood-fill algorithm, takes a Point object and a target color and returns a list of the
     * Point objects around the original point which match the target color
     */
    public ArrayList<Point> regionGrowth(Point point, Color targetColor) {
        // create empty lists to hold visited points and ones to visit
        ArrayList<Point> visited = new ArrayList<Point>();
        ArrayList<Point> toVisit = new ArrayList<Point>();
        // start by adding the initial point
        toVisit.add(point);
        while (!toVisit.isEmpty()) { // while there are still neighbors to visit
            // move a point from the toVisit list to the visited list and then check its neighbors
            Point newPoint = toVisit.get(0);
            visited.add(newPoint);
            toVisit.remove(0);
            // loop through the neighbors
            for (int x = Math.max((int) newPoint.getX() - 1, 0); x <= Math.min(newPoint.getX() + 1, image.getWidth() - 1) ; x++) {
                for (int y = Math.max((int) newPoint.getY() - 1, 0); y <= Math.min(newPoint.getY() + 1, image.getHeight() - 1); y++) {
                    Color c = new Color(helperImage.getRGB(x, y));
                    if (colorMatch(c, targetColor)) {
                        toVisit.add(new Point(x, y));  // if a neighbor also matches the color, plan to visit it
                        helperImage.setRGB(x, y, 0);  // change the pixel color in helperImage to black so that we will not try to add this point again
                    }
                }
            }
        }
        return visited;
    }

    /**
     * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary).
     */
    private static boolean colorMatch(Color c1, Color c2) {
        // find the squared RGB difference and return whether the difference is <= the maxColorDiff we have defined
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