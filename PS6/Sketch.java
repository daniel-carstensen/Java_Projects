import java.awt.*;
import java.util.TreeMap;

public class Sketch {
    private TreeMap<Integer, Shape> sketch;

    public Sketch() {
        this.sketch = new TreeMap<>();
    }

    public Shape getShape(Integer id) {
        return sketch.get(id);
    }

    public void removeShape(Integer id) {
        sketch.remove(id);
    }
    public int addShape(Shape shape) {
        int id = (int)(Math.random()*100000);
        while (sketch.containsKey(id)) {
            id = (int)(Math.random()*100000);
        }
        sketch.put(id, shape);
        return id;
    }

    public Integer contains(int x, int y) {
        for (Integer key : sketch.descendingKeySet()) {
            if (sketch.get(key).contains(x, y)) {
                return key;
            }
        }
        return null;
    }

    public String toString() {
        String str = "";
        for (Integer key : sketch.navigableKeySet()) {
            str += sketch.get(key).toString() + "\n";
        }
        return str;
    }
    public void draw(Graphics g) {
        for (Integer key : sketch.navigableKeySet()) {
            sketch.get(key).draw(g);
        }
    }
}
