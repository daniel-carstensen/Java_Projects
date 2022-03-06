import java.awt.*;
import java.util.TreeMap;

/**
 * Sketch class that holds a map of ID's and associated shapes
 *
 * @author Max Lawrence, Daniel Carstensen, CS10, Winter 2022, created for PSet 6
 */
public class Sketch {
    private TreeMap<Integer, Shape> sketch; // initialize sketch as TreeMap with ID and the shape for the ID

    /**
     * Constructor
     * initializes empty TreeMap for the sketch
     */
    public Sketch() { this.sketch = new TreeMap<>(); }

    public TreeMap<Integer, Shape> getSketch() { return sketch; }

    /**
     * Gets the shape from the id key in the sketch
     * @param id the ID of the shape being requested
     * @return the shape matching the ID
     */
    public Shape getShape(Integer id) {
        Shape shape;
        // if the sketch has a shape matching the parameter id
        if (sketch.keySet().contains(id)) {
            // get the shape and return it
            shape = sketch.get(id);
        } else {
            // if not just return null;
            shape = null;
        }
        return shape;
    }

    /**
     * removes a shape from the map using its ID
     *
     * @param id id of the shape
     */
    public void removeShape(Integer id) {
        // if the sketch contains the id parameter
        if (sketch.keySet().contains(id)) {
            // remove the shape at that id
            sketch.remove(id);
        }
    }

    /**
     * Adds a shape to the sketch with a random ID indicator
     * @param shape the shape being added to the sketch
     * @return the ID assigned to the shape
     */
    public int addShape(Shape shape) {
        // create a new ID, and keep creating a new one until the ID is not yet in the sketch
        int id = (int)(Math.random()*100000);
        while (sketch.containsKey(id)) {
            id = (int)(Math.random()*100000);
        }
        // add the shape with its id to the sketch
        sketch.put(id, shape);
        return id; // return the ID
    }

    /**
     * Add a shape with a specific ID
     * @param id id to add with the shape to the sketch
     * @param shape shape to add to the sketch
     */
    public void addShapeID(Integer id, Shape shape) {
        sketch.put(id, shape);
    }

    /**
     * Check if any shape contains the parameter point
     * @param x x value of the point
     * @param y y value of the point
     * @return the newest shape that contains the point, or null if no shapes contain the point
     */
    public Integer contains(int x, int y) {
        // check if each shape contains the point, starting at the latest added shape and moving down
        for (Integer key : sketch.descendingKeySet()) {
            if (sketch.get(key).contains(x, y)) {
                // stop checking and return the id if a shape contains the point
                return key;
            }
        }
        return null;
    }

    public String toString() {
        String str = "";
        // for each shape in the sketch, add a line with the id of the shape and its details
        for (Integer key : sketch.navigableKeySet()) {
            if (!str.isEmpty()) {
                str += "\n";
            }
            str += key + " " + sketch.get(key).toString();
        }
        return str; // return the final string
    }

    public void draw(Graphics g) {
        // draw each shape in the sketch, from oldest to newest
        for (Integer key : sketch.navigableKeySet()) {
            sketch.get(key).draw(g);
        }
    }
}
