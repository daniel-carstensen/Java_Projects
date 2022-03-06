import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Max Lawrence, Daniel Carstensen, CS10, Winter 2022; completed for PSet 6
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	/**
	 * Establish a communicator that talks to one editor using a socket and the server
	 * @param sock
	 * @param server
	 */
	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 *
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Process changes requested in one line
	 * Synchronized so as to prevent the OS from switching between different communicators handling requests
	 *
	 * @param line message
	 */
	public synchronized void handleLine(String line) {
		Sketch sketch = server.getSketch(); // get the master sketch
		int id; // create an ID variable
		String[] pieces = line.split(" "); // split up the message
		// create a shape and an ID in the master sketch based on the instructions in the message
		// broadcast the created shape with ID and its information to all editors
		if (pieces[0].equals("ellipse")) {
			id = sketch.addShape(new Ellipse(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]),
					Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]), new Color(Integer.parseInt(pieces[5]))));
			server.broadcast(id + " " + server.getSketch().getShape(id).toString());
		}
		else if (pieces[0].equals("rectangle")) {
			id = sketch.addShape(new Rectangle(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]),
					Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]), new Color(Integer.parseInt(pieces[5]))));
			server.broadcast(id + " " + server.getSketch().getShape(id).toString());
		}
		else if (pieces[0].equals("segment")) {
			id = sketch.addShape(new Segment(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]),
					Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]), new Color(Integer.parseInt(pieces[5]))));
			server.broadcast(id + " " + server.getSketch().getShape(id).toString());
		}
		else if (pieces[0].equals("polyline")) {
			ArrayList<Point> points = new ArrayList<>();
			int i = 1;
			while (!pieces[i + 2].equals(":")) {
				points.add(new Point(Integer.parseInt(pieces[i]), Integer.parseInt(pieces[i + 1])));
				i += 2;
			}
			id = sketch.addShape(new Polyline(points, new Color(Integer.parseInt(pieces[i + 3]))));
			server.broadcast(id + " " + server.getSketch().getShape(id).toString());
		}
		// move a shape using its ID in the master sketch based on the instructions in the message
		// broadcast the movement of the shape to all editors
		else if (pieces[1].equals("move")) {
			id = Integer.parseInt(pieces[0]);
			sketch.getShape(id).moveBy(Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]));
			server.broadcast(id + " " + "move" + " " + pieces[2] + " " + pieces[3]);
		}
		// recolor a shape using its ID in the master sketch based on the instructions in the message
		// broadcast the recoloring of the shape to all editors
		else if (pieces[1].equals("recolor")) {
			id = Integer.parseInt(pieces[0]);
			Color color = new Color(Integer.parseInt(pieces[2]));
			sketch.getShape(id).setColor(color);
			server.broadcast(id + " " + "recolor" + " " + color.getRGB());
		}
		// delete a shape using its ID in the master sketch based on the instructions in the message
		// broadcast the deletion of the shape to all editors
		else if (pieces[1].equals("remove")) {
			id = Integer.parseInt(pieces[0]);
			sketch.removeShape(id);
			server.broadcast(id + " " + "remove");
		}
	}

	/**
	 * Keeps listening for and handling messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			if (!server.getSketch().getSketch().isEmpty()) { send(server.getSketch().toString()); }

			// Keep getting and handling messages from the client
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println("message received");
				System.out.println(line);
				handleLine(line); // call synchronized handleLine method
			}
			// Clean up -- note that also remove self from server's list, so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
