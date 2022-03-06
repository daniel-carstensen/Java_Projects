import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {
	private ArrayList<Point> points; // list of all points, connected by lines, that compose the Polyline
	private Color color; // color of the Polyline

	/**
	 * Constructor for "empty" Polyline with only starting point
	 * @param x initial x value
	 * @param y initial y value
	 * @param color color of the line
	 */
	public Polyline(int x, int y, Color color) {
		this.points = new ArrayList<>(); // create an empty list
		points.add(new Point(x, y)); // add the initial point to the list
		this.color = color;
	}

	/**
	 * Constructor for polyline that already has a list of points
	 * @param points list of current points
	 * @param color
	 */
	public Polyline(ArrayList<Point> points, Color color) {
		this.points = points; // when a list of points is passed in, set the current list to it
		this.color = color;
	}


	@Override
	public void moveBy(int dx, int dy) {
		// increment x and y based on dx and dy
		for (Point point : points) {
			point.x += dx;
			point.y += dy;
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public boolean contains(int x, int y) {
		// loop through the line front to back and check each segment for if it contains the x,y point
		for (int i = 0; i < points.size() - 1; i++) {
			Point curr = points.get(i);
			Point next = points.get(i + 1);
			if (Segment.pointToSegmentDistance(x, y, (int)curr.getX(), (int)curr.getY(), (int)next.getX(), (int)next.getY()) <= 3) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		// draw a line between each point and the next point in the list
		for (int i = 0; i < points.size() - 1; i++) {
			Point curr = points.get(i);
			Point next = points.get(i + 1);
			g.drawLine((int)curr.getX(), (int)curr.getY(), (int)next.getX(), (int)next.getY());
		}
	}

	@Override
	public String toString() {
		String str = "polyline"; // start with the polyline identifier
		// add each point's x and y in points list to the string
		for (Point point : points) {
			str += " " + (int)point.getX() + " " + (int)point.getY();
		}
		// add the color at the end with a : to mark the end of the list of points
		str += " : " + color.getRGB();
		return str;
	}
}
