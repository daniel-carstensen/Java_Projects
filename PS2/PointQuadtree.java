import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, 
 * with children at the subdivided quadrants.
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015.
 * @author CBK, Spring 2016, explicit rectangle.
 * @author CBK, Fall 2016, generic with Point2D interface.
 * @author Daniel Carstensen, Winter 2022, modified for pset2
 * @author Max Lawrence, Winter 2022, modified for pset2
 * 
 */
public class PointQuadtree<E extends Point2D> {
	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// bottom-right corner of the region
	private PointQuadtree<E> c1, c2, c3, c4;	// children

	/**
	 * Initializes a leaf quadtree, holding the point in the rectangle
	 */
	public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	// Getters
	
	public E getPoint() {
		return point;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

	public int getY2() {
		return y2;
	}

	/**
	 * Returns the child (if any) at the given quadrant, 1-4
	 * @param quadrant 1 through 4
	 */
	public PointQuadtree<E> getChild(int quadrant) {
		if (quadrant==1) return c1;
		if (quadrant==2) return c2;
		if (quadrant==3) return c3;
		if (quadrant==4) return c4;
		return null;
	}

	/**
	 * Returns whether or not there is a child at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public boolean hasChild(int quadrant) {
		return (quadrant==1 && c1!=null) || (quadrant==2 && c2!=null) || (quadrant==3 && c3!=null) || (quadrant==4 && c4!=null);
	}

	/**
	 * Inserts the point into the tree
	 */
	public void insert(E p2) { // uses the mathematical quadrants
//		if (p2.getX() < this.x1 || p2.getX() > this.x2 || p2.getY() < this.y1 || p2.getY() > this.y2) {
//			System.out.println("coordinate out of bounds");
		if (p2.getX() <= point.getX()) {		// determine if p2 is in quadrant 2, 3
			if (p2.getY() <= point.getY()) {	// determine if p2 is in quadrant 2
				if (hasChild(2)) {		// if there is already a node in quadrant 2
					c2.insert(p2);				// insert p2 in node c2 in a recursive fashion
				} else {						// base case of recursion: else, create a new node in quadrant 3
					this.c2 = new PointQuadtree<E>(p2, this.x1, this.y1, (int)point.getX(), (int)point.getY());
				}
			} else {							// else p2 is in quadrant 3
				if (hasChild(3)) {		// if there is already a node in quadrant 3
					c3.insert(p2);				// recursively call insert on node c3
				} else {						// base case of recursion: else, create a new node in quadrant 3
					c3 = new PointQuadtree<E>(p2, this.x1, (int)point.getY(), (int)point.getX(), this.y2);
				}
			}
		} else if (p2.getY() <= point.getY()) {	// else p2 is in quadrant 1, 4; determine if p2 is in quadrant 1 or 4
			if (hasChild(1)) {			// if there is already a node in quadrant 1
				c1.insert(p2);					// recursively call insert on node c1
			} else {							// base case of recursion: else, create a new node in quadrant 1
				c1 = new PointQuadtree<E>(p2, (int)point.getX(), y1, x2, (int)point.getY());
			}
		} else {								// else p2 is in quadrant 4
			if (hasChild(4)) {			// if there is already a node in quadrant 4
				c4.insert(p2);					// recursively call insert on node c4
			} else {							// base case of recursion: else, create a new node in quadrant 4
				c4 = new PointQuadtree<E>(p2, (int)point.getX(), (int)point.getY(), x2, y2);
			}
		}
	}
	
	/**
	 * Finds the number of points in the quadtree (including its descendants)
	 */
	public int size() {
		int size = 1;		// initialize size variables to hold the size of the root and all 4 quadrants
		int size1 = 0;
		int size2 = 0;
		int size3 = 0;
		int size4 = 0;
		// if root has children, recursively call size on all quadrants that contain a child until we hit the base case
		// where the quadrants don't have children
		if (hasChild(1)) size1 = c1.size();
		if (hasChild(2)) size2 = c2.size();
		if (hasChild(3)) size3 = c3.size();
		if (hasChild(4)) size4 = c4.size();

		size = size + size1 + size2 + size3 + size4;		// add up all the sizes
		return size;
	}
	
	/**
	 * Builds a list of all the points in the quadtree (including its descendants)
	 */
	public List<E> allPoints() {
		List<E> allPointsList = new ArrayList<E>();		// initialize lists to hold points of the root and all 4 quadrants
		allPointsList.add(point);						// allPointsList holds the root
		List<E> allPointsListC1 = new ArrayList<E>();
		List<E> allPointsListC2 = new ArrayList<E>();
		List<E> allPointsListC3 = new ArrayList<E>();
		List<E> allPointsListC4 = new ArrayList<E>();
		// if root has children, recursively call allPoints on all quadrants that contain a child until we hit the base case
		// where the quadrants don't have children
		if (hasChild(1)) allPointsListC1 = c1.allPoints();
		if (hasChild(2)) allPointsListC2 = c2.allPoints();
		if (hasChild(3)) allPointsListC3 = c3.allPoints();
		if (hasChild(4)) allPointsListC4 = c4.allPoints();

		allPointsList.addAll(allPointsListC1);		// add all points in the quadrant lists to allPointsList
		allPointsList.addAll(allPointsListC2);
		allPointsList.addAll(allPointsListC3);
		allPointsList.addAll(allPointsListC4);

		return allPointsList;
	}

	/**
	 * Uses the quadtree to find all points within the circle
	 * @param cx	circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 * @return    	the points in the circle (and the qt's rectangle)
	 */

	public ArrayList<E> findInCircle(double cx, double cy, double cr) {
		ArrayList<E> list = new ArrayList<E>();		// similar to allPoints, initialize lists to hold points of the root and all 4 quadrants
		List<E> list1 = new ArrayList<E>();
		List<E> list2 = new ArrayList<E>();
		List<E> list3 = new ArrayList<E>();
		List<E> list4 = new ArrayList<E>();

		// if the circle is not within the frame, return an empty list
		if (!Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2)) {return list;}
		// check the circle against the current point, if within circle, add to list
		if (Geometry.pointInCircle(point.getX(), point.getY(), cx, cy, cr)) {list.add(point);}
		// if root has children, recursively call findInCircle on all quadrants that contain a child until we hit the base case
		// where the quadrants don't have children
		if (hasChild(1)) list1 = c1.findInCircle(cx, cy, cr);
		if (hasChild(2)) list2 = c2.findInCircle(cx, cy, cr);
		if (hasChild(3)) list3 = c3.findInCircle(cx, cy, cr);
		if (hasChild(4)) list4 = c4.findInCircle(cx, cy, cr);

		list.addAll(list1);		// add all points in the quadrant lists to allPointsList
		list.addAll(list2);
		list.addAll(list3);
		list.addAll(list4);

		return list;
	}

	public static void main(String[] args) { // to test methods as we write them
		PointQuadtree<Point2D> quadtree = new PointQuadtree<Point2D>(new Blob(5, 5), 0, 0, 10, 10 );
		quadtree.insert(new Blob(2, 2));
		quadtree.insert(new Blob(6, 6));
		quadtree.insert(new Blob(2, 6));
		quadtree.insert(new Blob(6, 2));
		quadtree.insert(new Blob(3, 2));
		System.out.println(quadtree.allPoints());
		ArrayList<Point2D> collisionList = quadtree.findInCircle(2, 2, 1);
		for (Point2D point : collisionList) {
			System.out.println(point.getX());
			System.out.println(point.getY());
		}
	}
}
