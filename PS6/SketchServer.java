import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * A server to handle sketches: getting requests from the clients,
 * updating the overall state, and passing them on to the clients
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServer {
	private ServerSocket listen;						// for accepting connections
	private ArrayList<SketchServerCommunicator> comms;	// all the connections with clients
	private Sketch sketch;								// the state of the world
	private SLLQueue<String> changeRequests;
	
	public SketchServer(ServerSocket listen) {
		this.listen = listen;
		sketch = new Sketch();
		comms = new ArrayList<SketchServerCommunicator>();
		changeRequests = new SLLQueue<>();
	}

	public Sketch getSketch() {
		return sketch;
	}

	public SLLQueue<String> getChangeRequests() { return changeRequests; }

	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		System.out.println("server ready for connections");
		while (true) {
			SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);
			comm.start();
			addCommunicator(comm);
		}
	}

	private synchronized void handleChangeRequests() throws Exception {
		String line;
		int id;
		while (!changeRequests.isEmpty()) {
			line = changeRequests.dequeue();
			System.out.println("message is being handled");
			System.out.println(line);
			String[] pieces = line.split(" ");
			if (pieces[0].equals("ellipse")) {
				id = sketch.addShape(new Ellipse(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]),
						Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]), new Color(Integer.parseInt(pieces[5]))));
				broadcast(id + " " + getSketch().getShape(id).toString());
			}
			else if (pieces[0].equals("rectangle")) {
				id = sketch.addShape(new Rectangle(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]),
						Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]), new Color(Integer.parseInt(pieces[5]))));
				broadcast(id + " " + getSketch().getShape(id).toString());
			}
			else if (pieces[0].equals("segment")) {
				id = sketch.addShape(new Segment(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]),
						Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]), new Color(Integer.parseInt(pieces[5]))));
				broadcast(id + " " + getSketch().getShape(id).toString());
			}
			else if (pieces[0].equals("polyline")) {
				ArrayList<Point> points = new ArrayList<>();
				int i = 1;
				while (!pieces[i + 2].equals(":")) {
					points.add(new Point(Integer.parseInt(pieces[i]), Integer.parseInt(pieces[i + 1])));
					i += 2;
				}
				id = sketch.addShape(new Polyline(points, new Color(Integer.parseInt(pieces[i + 3]))));
				broadcast(id + " " + getSketch().getShape(id).toString());
			}
			else if (pieces[1].equals("move")) {
				id = Integer.parseInt(pieces[0]);
				sketch.getShape(id).moveBy(Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]));
				broadcast(id + " " + "move" + " " + pieces[2] + " " + pieces[3]);
			}
			else if (pieces[1].equals("recolor")) {
				id = Integer.parseInt(pieces[0]);
				Color color = new Color(Integer.parseInt(pieces[2]));
				sketch.getShape(id).setColor(color);
				broadcast(id + " " + "recolor" + " " + color.getRGB());
			}
			else if (pieces[1].equals("remove")) {
				id = Integer.parseInt(pieces[0]);
				sketch.removeShape(id);
				broadcast(id + " " + "remove");
			}
		}
	}

	/**
	 * Adds the communicator to the list of current communicators
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
	}

	/**
	 * Removes the communicator from the list of current communicators
	 */
	public synchronized void removeCommunicator(SketchServerCommunicator comm) {
		comms.remove(comm);
	}

	/**
	 * Sends the message from the one communicator to all (including the originator)
	 */
	public synchronized void broadcast(String msg) {
		for (SketchServerCommunicator comm : comms) {
			comm.send(msg);
		}
	}
	
	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();
	}
}
