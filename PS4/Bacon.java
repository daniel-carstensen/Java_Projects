import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Bacon {
    protected String actorsFile;
    protected String moviesFile;
    protected String actorsMoviesFile;

    public Bacon(String actorsFile, String moviesFile, String actorsMoviesFile) {
        this.actorsFile = actorsFile;
        this.moviesFile = moviesFile;
        this.actorsFile = actorsMoviesFile;
    }
    private HashMap<String, String> readToHashMap(String filePath) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath)); // creating a reader
        HashMap<String, String> map = new HashMap<>();
        String line;
        while ((line = input.readLine()) != null) {
            String[] pieces = line.split("\\|");
            String key = pieces[0];
            String value = pieces[1];
//            System.out.println(key);
//            System.out.println(value);
            map.put(key, value);
        }
        input.close();
        return map;
    }

    private AdjacencyMapGraph<String, String> buildGraph() throws IOException{
        AdjacencyMapGraph<String, String> gameGraph = new AdjacencyMapGraph<>();
        HashMap<String, String> moviesActors = readToHashMap(actorsMoviesFile);
        HashMap<String, String> actorIDs = readToHashMap(actorsFile);
        HashMap<String, String> movieIDs = readToHashMap(moviesFile);
        HashMap<String, ArrayList<String>> actorsInMovie = new HashMap<>();
        for (String key : moviesActors.keySet()) {
            if (!gameGraph.hasVertex(moviesActors.get(key))) {
                gameGraph.insertVertex(actorIDs.get(key));
            }
            ArrayList<String> list;
            if (!actorsInMovie.containsKey(key)) {
                list = new ArrayList<>();
            } else {
                list = actorsInMovie.get(movieIDs.get(key));
            }
            list.add(actorIDs.get(moviesActors.get(key)));
            actorsInMovie.put(movieIDs.get(key), list);
        }
        System.out.println(actorsInMovie);
        return gameGraph;
    }


    public static void main(String[] args) throws IOException {
        Bacon test = new Bacon("PS4/actorsTest.txt", "PS4/moviesTest.txt", "PS4/movies-actorsTest");
        test.readToHashMap(test.actorsFile);
    }
}
