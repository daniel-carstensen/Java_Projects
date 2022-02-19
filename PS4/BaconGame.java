import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

public class BaconGame {
    private String center;
    private AdjacencyMapGraph<String, Set<String>> graph;

    public BaconGame(String center, String actorsFile, String moviesFile, String actorsMoviesFile) throws IOException {
        GraphBuilder graphBuilder =  new GraphBuilder(actorsFile, moviesFile, actorsMoviesFile);
        this.graph = graphBuilder.buildGraph();
        this.center = center;
    }

    public void startNewGame(String center) {
        System.out.println("Commands:");
        System.out.println("c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation");
        System.out.println("d <low> <high>: list actors sorted by degree, with degree between low and high");
        System.out.println("i: list actors with infinite separation from the current center");
        System.out.println("p <name>: find path from <name> to current center of the universe");
        System.out.println("s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high");
        System.out.println("u <name>: make <name> the center of the universe");
        System.out.println("q: quit game");
        newCenter("Kevin Bacon");
    }
    public void newCenter(String center) {
        this.center = center;

    }
    public void readInput() {
        Scanner input = new Scanner(System.in);
        String userInput = input.next();
        String[] pieces = userInput.split(" ");
        String command = pieces[0];
        String parameter = pieces[1];
        if (command.compareTo("c") == 0) {
            bestCenters(Integer.getInteger(parameter));
        }
    }
    public void bestCenters(int num) {
        PriorityQueue<HashMap<String, Double>> centerQueue = new PriorityQueue<>(); // add a comparator
        for (String vertex : graph.vertices()) {
            Graph<String, Set<String>> tree = GraphLib.bfs(graph, center);
            double averageSeparation = GraphLib.averageSeparation(tree, center);
            HashMap<String, Double> map = new HashMap<>();
            map.put(vertex, averageSeparation);

        }
    }

    public static void main(String[] args) throws IOException {
    }
}
