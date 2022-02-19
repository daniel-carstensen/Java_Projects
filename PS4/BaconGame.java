import net.datastructures.Vertex;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class BaconGame {
    private String center;
    private AdjacencyMapGraph<String, Set<String>> graph;
    private Graph<String, Set<String>> tree;


    private class Actor {
        String actor;
        double value;

        public Actor(String actor, double value) {
            this.actor = actor;
            this.value = value;
        }

        public String getActor() {
            return actor;
        }

        public double getValue() {
            return value;
        }
    }

    public BaconGame(String center, String actorsFile, String moviesFile, String actorsMoviesFile) throws IOException {
        GraphBuilder graphBuilder = new GraphBuilder(actorsFile, moviesFile, actorsMoviesFile);
        this.graph = graphBuilder.buildGraph();
        this.center = center;
        startNewGame("Kevin Bacon");
    }

    public void startNewGame(String center) {
        System.out.println("Commands:");
        System.out.println("c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation");
        System.out.println("d <low> <high>: list actors sorted by degree, with degree between low and high");
        System.out.println("i: list actors with infinite separation from the current center");
        System.out.println("p <name>: find path from <name> to current center of the universe");
        System.out.println("u <name>: make <name> the center of the universe");
        System.out.println("q: quit game");
        newCenter("Kevin Bacon");
        readCommand();
    }

    public void newCenter(String center) {
        this.center = center;
        this.tree = GraphLib.bfs(graph, this.center);
        System.out.println(this.center + " is now the center of the acting universe, connected to " + tree.numVertices()
                + "/" + graph.numVertices() + " actors with average separation " + GraphLib.averageSeparation(tree, this.center));
    }

    public void readCommand() {
        System.out.println("Input game command: ");
        Scanner input = new Scanner(System.in);
        String command = input.next();
        readParameter(command);
        if (command.equals("i")) {
            printMissingVertices();
        }
        else if (command.equals("q")) {
            System.out.println("Game Over.");
        }
        else if (command.equals("c") | command.equals("d") | command.equals("p") |command.equals("u")) {
            readParameter(command);
        }
        else {
            System.out.println("Invalid input, try again");
            readCommand();
        }
    }

    public void readParameter(String command) {
        if (command.equals("c")) {
            System.out.println("List this many actors: ");
            Scanner input = new Scanner(System.in);
            int parameter = input.nextInt();
            bestCenters(parameter);
        }
        else if (command.equals("p")) {
            System.out.println("From " + center + " to: ");
            Scanner input = new Scanner(System.in);
            String parameter = input.next();
            printPath(parameter);
        }
        else if (command.equals("u")) {
            System.out.println("New center of the universe: ");
            Scanner input = new Scanner(System.in);
            String parameter = input.next();
            newCenter(parameter);
        }
        else if (command.equals("d")) {
            System.out.println("List this many actors: ");
            Scanner input = new Scanner(System.in);
            int parameter = input.nextInt();
            bestByDegree(parameter);
        }
        readCommand();
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

    public void bestCenters(int num) {
        Graph<String, Set<String>> baconUniverse = GraphLib.bfs(this.graph, "Kevin Bacon");
        Set<String> loners = GraphLib.missingVertices(this.graph, baconUniverse);
        PriorityQueue<Actor> centerQueue;
        if (num < 0) {
            centerQueue = new PriorityQueue<Actor>((a1, a2) -> (Double.compare(a1.getValue(), a2.getValue())));
        } else {
            centerQueue = new PriorityQueue<Actor>((a1, a2) -> Double.compare(a2.getValue(), a1.getValue()));
        }

        for (String v : baconUniverse.vertices()) {
            Graph<String, Set<String>> tree = GraphLib.bfs(this.graph, v);
            double averageSeparation = GraphLib.averageSeparation(tree, v);
            BaconGame.Actor centerSeparation = new BaconGame.Actor(v, averageSeparation);
            centerQueue.add(centerSeparation);
        }

        for (int i = 1; i <= Math.abs(num); ++i) {
            BaconGame.Actor actor = centerQueue.poll();
            System.out.println(actor.getActor() + " with average separation " + actor.getValue());
        }
    }

    public void bestByDegree(int num) {
        List<String> actorsByDegree = GraphLib.verticesByInDegree(this.graph);
        if (num < 0) {
            int size = actorsByDegree.size() - 1;
            for (int i = 0; i <= Math.abs(num); i++) {
                System.out.println(actorsByDegree.get(size - i) + " with " + graph.inDegree(actorsByDegree.get(size - i)) + " direct connections.");
            }
        } else {
            for (int i = 0; i <= num; i++) {
                System.out.println(actorsByDegree.get(i) + " with " + graph.inDegree(actorsByDegree.get(i)) + " direct connections.");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BaconGame test = new BaconGame("Kevin Bacon", "PS4/actors.txt", "PS4/movies.txt", "PS4/movie-actors.txt");
    }
}
