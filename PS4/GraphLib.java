import net.datastructures.Tree;

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
	 * Takes a random walk from a vertex, up to a given number of steps
	 * So a 0-step path only includes start, while a 1-step path includes start and one of its out-neighbors,
	 * and a 2-step path includes start, an out-neighbor, and one of the out-neighbor's out-neighbors
	 * Stops earlier if no step can be taken (i.e., reach a vertex with no out-edge)
	 * @param g		graph to walk on
	 * @param start	initial vertex (assumed to be in graph)
	 * @param steps	max number of steps
	 * @return		a list of vertices starting with start, each with an edge to the sequentially next in the list;
	 * 			    null if start isn't in graph
	 */
	public static <V,E> List<V> randomWalk(Graph<V,E> g, V start, int steps) {
		if (!g.hasVertex(start)) return null;

		int remainingSteps = steps;
		V currentVertex = start;
		List<V> vertexList = new ArrayList<V>();

		vertexList.add(start);

		while (remainingSteps > 0) {
			if (g.outDegree(currentVertex) == 0) return vertexList;
			else {
				List<V> outNeighborList = new ArrayList<>();
				Iterable<V> outNeighbors = g.outNeighbors(currentVertex);
				for (V vertex : outNeighbors) {
					outNeighborList.add(vertex);
				}
				int direction = (int) (Math.random() * (outNeighborList.size()));
				currentVertex = outNeighborList.get(direction);
				vertexList.add(currentVertex);
				remainingSteps --;
			}
		}

		return vertexList;
	}
	
	/**
	 * Orders vertices in decreasing order by their in-degree
	 * @param g		graph
	 * @return		list of vertices sorted by in-degree, decreasing (i.e., largest at index 0)
	 */
	public static <V,E> List<V> verticesByInDegree(Graph<V,E> g) {
		List<V> allVertices = new ArrayList<>();
		Iterable<V> vertices = g.vertices();
		for (V vertex : vertices) {
			allVertices.add(vertex);
		}

		allVertices.sort((v1, v2) -> (
			g.inDegree(v2) - g.inDegree(v1)
				)
		);

		return allVertices;
	}

	public static <V,E> Graph<V,E> bfs (Graph<V,E> g, V source, E edge) {
		Graph<V, E> backTrack = new AdjacencyMapGraph<>(); //initialize backTrack
		backTrack.insertVertex(source); //load start vertex with null parent
		Set<V> visited = new HashSet<V>(); //Set to track which vertices have already been visited
		Queue<V> queue = new LinkedList<V>(); //queue to implement BFS

		queue.add(source); //enqueue start vertex
		visited.add(source); //add start to visited Set
		while (!queue.isEmpty()) { //loop until no more vertices
			V u = queue.remove(); //dequeue
			for (V v : g.outNeighbors(u)) { //loop over out neighbors
				if (!visited.contains(v)) { //if neighbor not visited, then neighbor is discovered from this vertex
					visited.add(v); //add neighbor to visited Set
					queue.add(v); //enqueue neighbor
					backTrack.insertDirected(v, u, edge); //save that this vertex was discovered from prior vertex
				}
			}
		}
		return backTrack;
	}

	public static <V,E> List<V> getPath (Graph<V,E> tree, V v) {
		List<V> path = new ArrayList<>();
		path.add(v);
		Iterable<V> out = tree.outNeighbors(v);
		while (out.iterator().next() != null) {
			v = out.iterator().next();
			out = tree.outNeighbors(v);
			path.add(v);
		}
		return path;
	}

//	public static <V,E> Set<V> missingVertices (Graph<V,E> graph, Graph<V,E> subgraph) {
//
//	}
//
//	public static <V,E> double averageSeparation (Graph<V,E> tree, V root) {
//
//	}

}
