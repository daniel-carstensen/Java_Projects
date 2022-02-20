import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * GraphBuilder to play the Bacon game gy creating a graph that holds a network of actors and movies using text files
 *
 * @author Max Lawrence, Winter 2022, Dartmouth CS10, pset 4
 * @author Daniel Carstensen, Winter 2022, Dartmouth CS10, pset 4
 */
public class GraphBuilder {
    public String actorsFile; // variable to hold path to actors file
    public String moviesFile; // variable to hold path to movies file
    public String actorsMoviesFile; // variable to hold path to actors-movies file

    /**
     * instantiate path variables
     *
     * @param actorsFile input path to actors file
     * @param moviesFile input path to movies file
     * @param actorsMoviesFile input path to actors-movies file
     */
    public GraphBuilder(String actorsFile, String moviesFile, String actorsMoviesFile) {
        this.actorsFile = actorsFile; // initialize the path
        this.moviesFile = moviesFile; // initialize the path
        this.actorsMoviesFile = actorsMoviesFile; // initialize the path
    }

    /**
     * Takes in a .txt filePath and creates a Map from the text in the file
     * @param filePath
     * @return a map constructed from the text in the inputted filePath
     * @throws IOException
     */
    private HashMap<String, String> readToHashMap(String filePath) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath)); // creating a reader
        HashMap<String, String> map = new HashMap<>(); // map to hold the key value pairs
        while (input.ready()) {
            // each line of the file has key-value pairs seperated by | symbol
            // get the key and value and add them to the map
            String[] pieces = input.readLine().split("\\|");
            String key = pieces[0];
            String value = pieces[1];
            map.put(key, value);
        }
        input.close(); // close the reader
        return map; // return the completed map
    }

    /**
     * Build a graph so that the vertices are actor names and edges between actors are sets of the movies
     * they starred in together
     * @return the graph
     * @throws IOException
     */
    public AdjacencyMapGraph<String, Set<String>> buildGraph() throws IOException {
        AdjacencyMapGraph<String, Set<String>> gameGraph = new AdjacencyMapGraph<>();
        // create maps for actor IDs and movie IDs to get names from IDs
        HashMap<String, String> actorIDs = readToHashMap(actorsFile);
        HashMap<String, String> movieIDs = readToHashMap(moviesFile);
        for (String actorID : actorIDs.keySet()) { // add a vertex for each actor
            gameGraph.insertVertex(actorIDs.get(actorID));
        }
        // new HashMap to hold movies and sets of the actors in them
        HashMap<String, Set<String>> actorsInMovie = new HashMap<>();
        BufferedReader input = new BufferedReader(new FileReader(actorsMoviesFile)); // creating a reader
        while (input.ready()) {
            String[] pieces = input.readLine().split("\\|");
            String movie = movieIDs.get(pieces[0]);
            String actor = actorIDs.get(pieces[1]);
            Set<String> set;
            // if the movie has already been added to the map, get its set of actors and add the new actor
            if (actorsInMovie.containsKey(movie)) {
                set = actorsInMovie.get(movie);
            } else {
                // otherwise, create a new set to hold the actor and add the movie to the map
                set = new HashSet<>();
            }
            set.add(actor);
            actorsInMovie.put(movie, set);
        }
        input.close(); // close the reader

        for (String actor : gameGraph.vertices()) { // for each vertex
            for (String movie : actorsInMovie.keySet()) { // for each movie
                if (actorsInMovie.get(movie).contains(actor)) { // if the actor is in the movie
                    for (String costar : actorsInMovie.get(movie)) { // loop through each actor in that movie
                        if (!actor.equals(costar)) { // if the actor is not the current costar being assessed
                            Set<String> edgeLabel;
                            // if there is not already an edge between the actor
                            if (!gameGraph.hasEdge(actor, costar)) {
                                edgeLabel = new HashSet<>(); // create a new set to hold the label
                            } else { // if there is already an edge
                                edgeLabel = gameGraph.getLabel(actor, costar); // get the Set label
                            }
                            edgeLabel.add(movie); // add the shared movie to the label
                            // add a new edge between the actor and costar, with the set edgeLabel as the label
                            gameGraph.insertUndirected(actor, costar, edgeLabel);
                        }
                    }
                }
            }
        }
        return gameGraph; // return the final graph
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Testing on small, test files:");
        GraphBuilder test0 = new GraphBuilder("PS4/actorsTest.txt", "PS4/moviesTest.txt", "PS4/movie-actorsTest.txt");
        AdjacencyMapGraph<String, Set<String>> test0graph = test0.buildGraph();
        System.out.println("# of Vertices: " + test0graph.numVertices());
        System.out.println("# of Edges: " + test0graph.numEdges());
        System.out.println(test0graph);
        System.out.println();

        System.out.println("Now testing on real files:");
        GraphBuilder test1 = new GraphBuilder("PS4/actors.txt", "PS4/movies.txt", "PS4/movie-actors.txt");
        AdjacencyMapGraph<String, Set<String>> test1graph = test1.buildGraph();
        System.out.println("# of Vertices: " + test1graph.numVertices());
        System.out.println("# of Edges: " + test1graph.numEdges());
        System.out.println("For test, printing all edges from Kevin Bacon: ");
        System.out.println("# of Actors connected to Kevin Bacon: " + test1graph.inDegree("Kevin Bacon"));
        System.out.println(test1graph.inNeighbors("Kevin Bacon"));
    }
}
