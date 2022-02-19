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
     * 
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    private HashMap<String, String> readToHashMap(String filePath) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath)); // creating a reader
        HashMap<String, String> map = new HashMap<>();
        while (input.ready()) {
            String[] pieces = input.readLine().split("\\|");
            String key = pieces[0];
            String value = pieces[1];
            map.put(key, value);
        }
        input.close();
        return map;
    }

    public AdjacencyMapGraph<String, Set<String>> buildGraph() throws IOException {
        AdjacencyMapGraph<String, Set<String>> gameGraph = new AdjacencyMapGraph<>();
        HashMap<String, String> actorIDs = readToHashMap(actorsFile);
        HashMap<String, String> movieIDs = readToHashMap(moviesFile);
        HashMap<String, Set<String>> actorsInMovie = new HashMap<>();
        for (String actorID : actorIDs.keySet()) {
            if (!gameGraph.hasVertex(actorIDs.get(actorID))) {
                gameGraph.insertVertex(actorIDs.get(actorID));
            }
        }
        BufferedReader input = new BufferedReader(new FileReader(actorsMoviesFile)); // creating a reader
        while (input.ready()) {
            String[] pieces = input.readLine().split("\\|");
            String movie = movieIDs.get(pieces[0]);
            String actor = actorIDs.get(pieces[1]);
            Set<String> set;
            if (actorsInMovie.containsKey(movie)) {
                set = actorsInMovie.get(movie);
            } else {
                set = new HashSet<>();
            }
            set.add(actor);
            actorsInMovie.put(movie, set);
        }
        input.close();

        for (String actor : gameGraph.vertices()) {
            for (String movie : actorsInMovie.keySet()) {
                if (actorsInMovie.get(movie).contains(actor)) {
                    for (String costar : actorsInMovie.get(movie)) {
                        if (!actor.equals(costar)) {
                            Set<String> edgeLabel;
                            if (!gameGraph.hasEdge(actor, costar)) {
                                edgeLabel = new HashSet<>();
                            } else {
                                edgeLabel = gameGraph.getLabel(actor, costar);
                            }
                            edgeLabel.add(movie);
                            gameGraph.insertUndirected(actor, costar, edgeLabel);
                        }
                    }
                }
            }
        }
        return gameGraph;
    }
}
