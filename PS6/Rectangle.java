import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 * @author Max Lawrence, Daniel Carstensen, CS10, Winter 2022; completed for PSet 6
 */
public class Rectangle implements Shape {
	private int x1, y1, x2, y2;	// upper left and lower right
	private Color color; // color of shape

	/**
	 * Constructor for an "Empty" rectangle with only the corner defined
	 */
	public Rectangle(int x1, int y1, Color color) {
		this.x1 = x1;
		this.x2 = x1;
		this.y1 = y1;
		this.y2 = y1;
		this.color = color;
	}

	/**
	 * Constructor for a rectangle with all 4 corners defined
	 */
	public Rectangle(int x1, int y1, int x2, int y2, Color color) {
		setCorners(x1, y1, x2, y2);
		this.color = color;
	}

	/**
	 * Redefines the rectangle based on new corners
	 */
	public void setCorners(int x1, int y1, int x2, int y2) {
		// Ensure correct upper left and lower right
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	@Override
	public void moveBy(int dx, int dy) {
		// update x1,y1,x2,y2 values based on dx and dy
		x1 += dx; y1 += dy;
		x2 += dx; y2 += dy;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Checks if the parameter x,y point is within the bounds of the rectangle
	 * @param x x value of the point
	 * @param y y value of the point
	 * @return true if the point is in the rectangle, false otherwise
	 */
	@Override
	public boolean contains(int x, int y) {
		return ((x <= x2 && x >= x1) && (y <= y2 && y >= y1));
	}

	@Override
	public void draw(Graphics g) {
		// draw a rectangle defined by the corners and the color
		g.setColor(color);
		g.fillRect(x1, y1, (x2 - x1), (y2 - y1));
	}

	public String toString() {
		// return the "rectangle" identifier, the corners, and the color
		return "rectangle "+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB();
	}
}
