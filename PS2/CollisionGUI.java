import java.awt.*;

import javax.swing.*;

import java.util.List;
import java.util.ArrayList;


/**
 * Using a quadtree for collision detection
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, updated for blobs
 * @author CBK, Fall 2016, using generic PointQuadtree
 */
public class CollisionGUI extends DrawingGUI {
	private static final int width=800, height=600;		// size of the universe

	private List<Blob> blobs;						// all the blobs
	private List<Blob> colliders;					// the blobs who collided at this step
	private char blobType = 'b';						// what type of blob to create
	private char collisionHandler = 'c';				// when there's a collision, color them, or destroy them
	private int delay = 100;							// timer control

	public CollisionGUI() {
		super("super-collider", width, height);

		blobs = new ArrayList<Blob>();

		// Timer drives the animation.
		startTimer();
	}

	/**
	 * Adds a blob of the current blobType at the location
	 */
	private void add(int x, int y) {
		if (blobType=='b') {
			blobs.add(new Bouncer(x,y,width,height));
		}
		else if (blobType=='w') {
			blobs.add(new Wanderer(x,y));
		}
		else {
			System.err.println("Unknown blob type "+blobType);
		}
	}

	/**
	 * DrawingGUI method, here creating a new blob
	 */
	public void handleMousePress(int x, int y) {
		add(x,y);
		repaint();
	}

	/**
	 * DrawingGUI method
	 */
	public void handleKeyPress(char k) {
		if (k == 'f') { // faster
			if (delay>1) delay /= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 's') { // slower
			delay *= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 'r') { // add some new blobs at random positions
			for (int i=0; i<10; i++) {
				add((int)(width*Math.random()), (int)(height*Math.random()));
				repaint();
			}			
		}
		else if (k == 'c' || k == 'd') { // control how collisions are handled
			collisionHandler = k;
			System.out.println("collision:"+k);
		}
		else { // set the type for new blobs
			blobType = k;			
		}
	}

	/**
	 * DrawingGUI method, here drawing all the blobs and then re-drawing the colliders in red
	 */
	public void draw(Graphics g) {
		// TODO: YOUR CODE HERE
		for (Blob blob : blobs) {		// Ask all the blobs to draw themselves.
			blob.draw(g);
		}
		if (colliders != null) {
			g.setColor(Color.red);
			for (Blob collider : colliders) {        // Ask the colliders to draw themselves in red.
				collider.draw(g);
			}
		}
	}

	/**
	 * Sets colliders to include all blobs in contact with another blob
	 */
	private void findColliders() {
		// TODO: YOUR CODE HERE
		colliders = new ArrayList<>();
		PointQuadtree<Blob> colliderTree = new PointQuadtree<>(blobs.get(0), 0, 0, 800, 600);
		for (int index=1; index < blobs.size(); index++) {
			colliderTree.insert(blobs.get(index));
		}
		// For each blob, see if anybody else collided with it
		colliders = addColliders(colliderTree);
	}

	private ArrayList addColliders(PointQuadtree tree) {
		ArrayList colliderList = new ArrayList<>();
		List colliderListC1 = new ArrayList<>();
		List colliderListC2 = new ArrayList<>();
		List colliderListC3 = new ArrayList<>();
		List colliderListC4 = new ArrayList<>();

		ArrayList<Blob> temp = tree.findInCircle(tree.getPoint().getX(), tree.getPoint().getY(), 10);
		if (temp.size() > 1) colliderList = temp;
		if (tree.hasChild(1)) colliderListC1 = addColliders(tree.getChild(1));
		if (tree.hasChild(2)) colliderListC2 = addColliders(tree.getChild(2));
		if (tree.hasChild(3)) colliderListC3 = addColliders(tree.getChild(3));
		if (tree.hasChild(4)) colliderListC4 = addColliders(tree.getChild(4));

		colliderList.addAll(colliderListC1);
		colliderList.addAll(colliderListC2);
		colliderList.addAll(colliderListC3);
		colliderList.addAll(colliderListC4);
		return colliderList;
	}

	/**
	 * DrawingGUI method, here moving all the blobs and checking for collisions
	 */
	public void handleTimer() {
		// Ask all the blobs to move themselves.
		for (Blob blob : blobs) {
			blob.step();
		}
		// Check for collisions
		if (blobs.size() > 0) {
			findColliders();
			if (collisionHandler=='d') {
				blobs.removeAll(colliders);
				colliders = null;
			}
		}
		// Now update the drawing
		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CollisionGUI();
			}
		});
	}
}
