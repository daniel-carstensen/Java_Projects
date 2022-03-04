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
	private ArrayList<Point> points;
	private Color color;

	public Polyline(int x, int y, Color color) {
		this.points = new ArrayList<>();
		points.add(new Point(x, y));
		this.color = color;
	}

	public Polyline(ArrayList<Point> points, Color color) {
		this.points = points;
		this.color = color;
	}

	@Override
	public void moveBy(int dx, int dy) {
		for (Point point : points) {
			point.x = (int) (point.getX() + dx);
			point.y = (int) (point.getY() + dy);
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
		for (int i = 0; i < points.size() - 1; i++) {
			Point curr = points.get(i);
			Point next = points.get(i + 1);
			g.drawLine((int)curr.getX(), (int)curr.getY(), (int)next.getX(), (int)next.getY());
		}
	}

	@Override
	public String toString() {
		String str = "polyline";
		for (Point point : points) {
			str += " " + (int)point.getX() + " " + (int)point.getY();
		}
		str += " : " + color.getRGB();
		return str;
	}
}
