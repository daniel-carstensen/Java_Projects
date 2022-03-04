import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			String line;
			Sketch sketch = editor.getSketch();
			while ((line = in.readLine()) != null) {
				System.out.println("message received");
				System.out.println(line);
				String[] pieces = line.split(" ");
				Integer id = Integer.parseInt(pieces[0]);
				if (pieces[1].equals("ellipse")) {
					sketch.addShapeID(id, new Ellipse(Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]),
							Integer.parseInt(pieces[4]), Integer.parseInt(pieces[5]), new Color(Integer.parseInt(pieces[6]))));
				}
				else if (pieces[1].equals("rectangle")) {
					sketch.addShapeID(id, new Rectangle(Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]),
							Integer.parseInt(pieces[4]), Integer.parseInt(pieces[5]), new Color(Integer.parseInt(pieces[6]))));
				}
				else if (pieces[1].equals("segment")) {
					sketch.addShapeID(id, new Segment(Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]),
							Integer.parseInt(pieces[4]), Integer.parseInt(pieces[5]), new Color(Integer.parseInt(pieces[6]))));
				}
				else if (pieces[1].equals("polyline")) {
					ArrayList<Point> points = new ArrayList<>();
					int i = 2;
					while (!pieces[i + 2].equals(":")) {
						points.add(new Point(Integer.parseInt(pieces[i]), Integer.parseInt(pieces[i + 1])));
						i += 2;
					}
					sketch.addShapeID(id, new Polyline(points, new Color(Integer.parseInt(pieces[i + 3]))));
				}
				else if (pieces[1].equals("remove")) {
					sketch.removeShape(id);
				}
			}
			editor.setSketch(sketch);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	// Send editor requests to the server
	// TODO: YOUR CODE HERE
	
}