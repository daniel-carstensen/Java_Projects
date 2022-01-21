import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

/**
 * Webcam-based drawing 
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 * 
 * @author Chris Bailey-Kellogg, Spring 2015 (based on a different webcam app from previous terms)
 * @author Daniel Carstensen, Winter 2022, modified the scaffold to function as a webcam-based painting program
 * @author Max Lawrence, Winter 2022
 */
public class CamPaint extends Webcam {
	private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
	private RegionFinder finder;			// handles the finding
	private Color targetColor;          	// color of regions of interest (set by mouse press)
	private Color paintColor = Color.blue;	// the color to put into the painting from the "brush"
	private BufferedImage painting;			// the resulting masterpiece

	/**
	 * Initializes the region finder and the drawing
	 */
	public CamPaint() {
		finder = new RegionFinder();		// instantiate new region finder
		clearPainting();					// method call to instantiate a blank image
	}

	/**
	 * Resets the painting to a blank image
	 */
	protected void clearPainting() {
		painting = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);		// instantiate a blank image
	}

	/**
	 * DrawingGUI method, here drawing one of live webcam, recolored image, or painting, 
	 * depending on display variable ('w', 'r', or 'p')
	 */
	@Override
	public void draw(Graphics g) {
		if (displayMode == 'w') {
			g.drawImage(this.image, 0, 0, null);		// draws the original, unmodified webcam image
		}
		else if (displayMode == 'r') {
			g.drawImage(finder.getRecoloredImage(), 0, 0, null);		// draws the output of finder calling its .getRecoloredImage() method
		}
		else if (displayMode == 'p') {
			g.drawImage(painting, 0, 0, null);		// draws the painting
		}
	}

	/**
	 * Webcam method, here finding regions, recoloring the image, and updating the painting.
	 */
	@Override
	public void processImage() {
		if (targetColor != null) {		// only runs if a target color has been selected by the user
			finder.setImage(this.image);		// set image in the RegionFinder finder to the current webcam image
			finder.findRegions(targetColor);		// finder calls its .findRegions() method to find all regions in the image that match the target color
			finder.recolorImage();		// finder calls its .recolorImage() method to recolor the largest region found in the image
			if (finder.largestRegion() != null) {		// only runs if largest region exists
				for (Point point : finder.largestRegion()) {		// for every pixel in largest region
					painting.setRGB((int) point.getX(), (int) point.getY(), paintColor.getRGB());		// set the corresponding pixel in painting to paintColor
				}
			}
		}
	}

	/**
	 * Overrides the DrawingGUI method to set the track color.
	 */
	@Override
	public void handleMousePress(int x, int y) {
		if (image != null) {		// if an image has been instantiated
			targetColor = new Color(image.getRGB(x, y));		// set the target color to the color of the pixel clicked by the mouse
			System.out.println("tracking " + targetColor);		// print statement to confirm the tracking of the selected color
		}
	}

	/**
	 * DrawingGUI method, here doing various drawing commands
	 */
	@Override
	public void handleKeyPress(char k) {
		if (k == 'p' || k == 'r' || k == 'w') { 	// display: painting, recolored image, or webcam
			displayMode = k;
		}
		else if (k == 'c') { 	// clear
			clearPainting();
		}
		else if (k == 'o') { 	// save the recolored image
			saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
		}
		else if (k == 's') { 	// save the painting
			saveImage(painting, "pictures/painting.png", "png");
		}
		else {
			System.out.println("unexpected key "+k);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CamPaint();
			}
		});
	}
}
