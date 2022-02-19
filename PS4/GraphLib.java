import java.util.*;

/**
 * Library for graph analysis
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2016
 * @author Daniel Carstensen, Winter 2022, completed for SA7, augmented for PS4
 * 
 */
public class GraphLib {
	/**
	 * Performs a bfs on the graph from a specified start vertex and builds a tree graph with the starting vertex
	 * at the root
	 *
	 * @param graph graph
	 * @param source starting vertex
	 *
	 * @return backTrack tree graph with the shortest paths from any connected vertex back to the starting vertex
	 */
	public static <V, E> Graph<V, E> bfs(Graph<V, E> graph, V source) {
		Graph<V, E> backTrack = new AdjacencyMapGraph<>(); // initialize backTrack tree graph
		backTrack.insertVertex(source); // load start vertex into graph
		Set<V> visited = new HashSet<V>(); // Set to track which vertices have already been visited
		Queue<V> queue = new LinkedList<V>(); // queue to implement BFS

		queue.add(source); // enqueue start vertex
		visited.add(source); // add start to visited Set
		while (!queue.isEmpty()) { // loop until no more vertices
			V u = queue.remove(); // dequeue
			for (V v : graph.outNeighbors(u)) { // loop over out neighbors
				if (!visited.contains(v)) { // if neighbor not visited, then neighbor is discovered from this vertex
					visited.add(v); // add neighbor to visited Set
					queue.add(v); // enqueue neighbor
					backTrack.insertVertex(v); // insert neighbor into backTrack graph
					backTrack.insertDirected(v, u, graph.getLabel(v, u)); // save that this vertex was discovered from prior vertex
				}
			}
		}
		return backTrack;
	}

	/**
	 * Find the path from a vertex to the vertex at the root in the tree graph created in BFS and return it
	 * as a list
	 *
	 * @param tree tree graph
	 * @param v input vertex in graph
	 *
	 * @return list that specifies the path from v to the root
	 */
	public static <V, E> List<V> getPath(Graph<V, E> tree, V v) {
		List<V> path = new ArrayList<>(); // initialize path list
		path.add(v); // add input vertex to list
		Iterator<V> out = tree.outNeighbors(v).iterator(); // initialize iterator to iterate through outNeighbors of input vertex
		while (out.hasNext()) {	// while the vertex as an outNeighbor
			v = out.next();	// redefine vertex as its outNeighbor
			out = tree.outNeighbors(v).iterator();	// redefine iterator
			path.add(v); // add vertex to the path list
		}
		return path;
	}


	/**
	 * find all vertices in graph that are not present in a subgraph (in our case created by BFS) and insert them to a Set
	 *
	 * @param graph graph
	 * @param subgraph subgraph

	 * @return
	 */
	public static <V, E> Set<V> missingVertices(Graph<V, E> graph, Graph<V, E> subgraph) {
		Set<V> missingVertices = new HashSet<>(); // initialize a Set of missingVertices

		for (V v : graph.vertices()) { // for all vertices in graph
			if (!subgraph.hasVertex(v)) missingVertices.add(v); // if subgraph does not contain vertex add to Set
		}
		return missingVertices;
	}

	/**
	 * calculate the average distance from a root to every other vertex in a tree graph
	 *
	 * @param tree tree
	 * @param root root

	 * @return average separation as a double
	 */
	public static <V, E> double averageSeparation(Graph<V, E> tree, V root) {
		int total; // create a total integer variable
		total = totalDistance(tree, root, 0); // set total equal to return value of recursive function
		return (double) total / (double) tree.numVertices(); // return total divided by total number of vertices in graph
	}

	/**
	 *
	 * @param tree tree
	 * @param currentV current vertex
	 * @param currentLevel current level in the tree, i.e. distance from the root

	 * @return total distance of all vertices from the root
	 */
	public static  <V,E> int totalDistance(Graph<V,E> tree, V currentV, int currentLevel) {
		int total = currentLevel; // set total equal to current level
		for (V v : tree.inNeighbors(currentV)) { // for all vertices that point to the current vertex
			total += totalDistance(tree, v, currentLevel+1); // recursively call totalDistance and add return value to total distance
		}
		return total;
	}
}
