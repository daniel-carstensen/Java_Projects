import java.io.IOException;
import java.util.*;

public class BaconGame {
    private String center;
    private AdjacencyMapGraph<String, Set<String>> graph;
    private Graph<String, Set<String>> tree;

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
        this.tree = GraphLib.bfs(graph, this.center);
        System.out.println(this.center + " is now the center of the acting universe, connected to " + tree.numVertices()
                + "/" + graph.numVertices() + " actors with average separation " + GraphLib.averageSeparation(tree, this.center));
    }
    public void readInput() {
        Scanner input = new Scanner(System.in);
        String userInput = input.next();
        String[] pieces = userInput.split(" ");
        String command = pieces[0];
        String parameter = pieces[1];
        if (command.equals("i")) {
            printMissingVertices();
        }
    }

    public void printMissingVertices() {
        Set<String> missingVertices = GraphLib.missingVertices(graph, tree);
        System.out.println("Disconnected Vertices:");
        for (String missingActor : missingVertices) {
            System.out.println(missingActor);
        }
    }
    public void printPath(String actor) {
        List<String> path = GraphLib.getPath(tree, actor);
        for (int indx = 0; indx < path.size() - 1; indx++) {
            String costar = path.get(indx);
            Set<String> label = graph.getLabel(costar, path.get(indx + 1));
            Object[] movies = label.toArray();
            Object movie = movies[0];
            System.out.println(costar + " appeared in [" + movie + "] with " + path.get(indx + 1));
        }
    }

    public static void main(String[] args) throws IOException {
    }
}
