import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Max Lawrence, Daniel Carstensen, CS10, Winter 2022; completed for PSet 6
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged
	private ArrayList<Point> freehandPoints;    // the current points in the freehand drawing


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		 // Connect to server
		 comm = new EditorCommunicator(serverIP, this);
		 comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		sketch.draw(g);
		if (curr != null) {
			curr.draw(g);
		}
	}

	// Helpers for event handlers
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// In drawing mode, start drawing a new shape
		if (mode == Mode.DRAW) {
			if (curr == null) {
				drawFrom = p;
				if (shapeType.equals("ellipse")) {
					// create a new ellipse with the initial point
					curr = new Ellipse((int)p.getX(), (int)p.getY(), color);
				}
				if (shapeType.equals("freehand")) {
					// create a new ellipse with the initial point
					curr = new Polyline((int)p.getX(), (int)p.getY(), color);
					// initialize the freehandPoints list to hold newly added points as the polyline is drawn
					freehandPoints = new ArrayList<>();
					// add the initial point to the list
					freehandPoints.add(p);
				}
				if (shapeType.equals("rectangle")) {
					// create a new rectangle with the initial point
					curr = new Rectangle((int)p.getX(), (int)p.getY(), color);
				}
				if (shapeType.equals("segment")) {
					// create a new segment with the initial point
					curr = new Segment((int)p.getX(), (int)p.getY(), color);
				}
				repaint(); // repaint with new shape
			}
		}
		// In moving mode, start dragging if clicked in the shape
		if (mode == Mode.MOVE) {
			// get the id of the moving shape if one was clicked on
			Integer clickedShapeID = sketch.contains((int)p.getX(), (int)p.getY());
			if (clickedShapeID != null) {
				// set initial conditions for moving
				movingId = clickedShapeID;
				moveFrom = p;
			}
		}
		// In recoloring mode, change the shape's color if clicked in it
		if (mode == Mode.RECOLOR) {
			// if a shape was clicked on, get its ID and send a recolor message
			Integer clickedShapeID = sketch.contains((int)p.getX(), (int)p.getY());
			if (clickedShapeID != null) {
				comm.send(clickedShapeID + " " + "recolor" + " " + color.getRGB());
			}
		}
		// In deleting mode, delete the shape if clicked in it
		if (mode == Mode.DELETE) {
			// if a shape was clicked on, get its ID and send a remove message
			Integer clickedShapeID = sketch.contains((int)p.getX(), (int)p.getY());
			if (clickedShapeID != null) {
				comm.send(clickedShapeID + " " + "remove");
			}
		}
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// In drawing mode, revise the shape as it is stretched out
		if (mode == Mode.DRAW) {
			// as the mouse moves before being released, update the shape according to its
			if (shapeType == "ellipse") {
				curr = new Ellipse((int)drawFrom.getX(), (int)drawFrom.getY(), (int)p.getX(), (int)p.getY(), color);
			}
			if (shapeType.equals("freehand")) {
				freehandPoints.add(p);
				curr = new Polyline(freehandPoints, color);
			}
			if (shapeType.equals("rectangle")) {
				curr = new Rectangle((int)drawFrom.getX(), (int)drawFrom.getY(), (int)p.getX(), (int)p.getY(), color);
			}
			if (shapeType.equals("segment")) {
				curr = new Segment((int)drawFrom.getX(), (int)drawFrom.getY(), (int)p.getX(), (int)p.getY(), color);
			}
			repaint();
		}
		// In moving mode, shift the object and keep track of where next step is from
		if (mode == Mode.MOVE) {
			if (moveFrom != null && movingId != -1) {
				// send a message with the ID of the shape being moved and the distance being moved in the x
				// and y directions
				comm.send(movingId + " " + "move" + " " + (int)(p.getX() - moveFrom.getX()) + " " + (int)(p.getY() - moveFrom.getY()));
				// then update the point being moved from
				moveFrom = p;
			}
		}
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		if (mode == Mode.DRAW) {
			// when a shape is done being drawn, send a draw message through the communicator
			comm.send(curr.toString());
			// and set current drawing variables back to null
			curr = null;
			freehandPoints = null;
			repaint(); // and repaint
		}
		if (mode == Mode.MOVE) {
			// stop moving
			moveFrom = null;
			movingId = -1;
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
