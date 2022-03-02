import java.util.TreeMap;

public class Sketch {
    public TreeMap<Integer, Shape> sketch;

    public Sketch() {
        this.sketch = new TreeMap<>();
    }

    public int addShape(Shape shape) {
        int id = (int)(Math.random()*100000);
        while (sketch.containsKey(id)) {
            id = (int)(Math.random()*100000);
        }
        sketch.put(id, shape);
        return id;
    }

    public String toString() {
        String str = "";
        for (Integer key : sketch.navigableKeySet()) {
            str += sketch.get(key).toString() + "\n";
        }
        return str;
    }
}
