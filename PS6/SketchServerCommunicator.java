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
	 * Establish the communicator with socket and server
	 *
	 * @param sock sock
	 * @param server server
	 */
	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
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
			String line; // create a variable to hold the current line
			Sketch sketch = server.getSketch(); // get the master sketch
			int id; // create a variable to hold the shape id
			while ((line = in.readLine()) != null) { // while we get messages from the editors
				System.out.println("message received");
				System.out.println(line);
				String[] pieces = line.split(" "); // split up the line
				// create a new shape with corresponding id in the master sketch based on the information contained in the message
				// broadcast the newly created shape with associated information and id to all editors
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
				// move a shape with corresponding id in the master sketch based on the information contained in the message
				// broadcast the changed shape with associated information and id to all editors
				else if (pieces[1].equals("move")) {
					id = Integer.parseInt(pieces[0]);
					sketch.getShape(id).moveBy(Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]));
					server.broadcast(id + " " + "move" + " " + pieces[2] + " " + pieces[3]);
				}
				// recolor a shape with corresponding id in the master sketch based on the information contained in the message
				// broadcast the changed shape with associated information and id to all editors
				else if (pieces[1].equals("recolor")) {
					id = Integer.parseInt(pieces[0]);
					Color color = new Color(Integer.parseInt(pieces[2]));
					sketch.getShape(id).setColor(color);
					server.broadcast(id + " " + "recolor" + " " + color.getRGB());
				}
				// remove a shape with corresponding id in the master sketch based on the information contained in the message
				// broadcast the changed shape with associated id to all editors
				else if (pieces[1].equals("remove")) {
					id = Integer.parseInt(pieces[0]);
					sketch.removeShape(id);
					server.broadcast(id + " " + "remove");
				}
			}

			// Clean up -- note that also remove self from server's list, so it doesn't broadcast here
			server.removeCommunicator(this); // when the editor stops running, remove the associated server communicator
			// close the readers and the socket
			out.close();
			in.close();
			sock.close();
		}
		// catch error
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
