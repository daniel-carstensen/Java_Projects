import net.datastructures.Vertex;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Interface for the Kevin Bacon Game
 *
 * @author Daniel Carstensen, Winter 2022, created for PS4
 * @author Max Lawrence, Winter 2022, created for PS4
 */

public class BaconGame {
    // Hold the current center actor, the graph of all actors, and the tree created from the center and the graph
    private String center;
    private AdjacencyMapGraph<String, Set<String>> graph;
    private Graph<String, Set<String>> tree;


    private class Actor { // private class internal to BaconGame that holds actor names and values
        String actor;
        double value;

        public Actor(String actor, double value) { // constructor
            this.actor = actor;
            this.value = value;
        }

        public String getActor() {
            return actor;
        } // get actor name

        public double getValue() {
            return value;
        } // get actor value
    }

    /**
     * Constructor
     * @param center name of the actor acting as the center of the game universe
     * @param actorsFile file with actor names and IDs
     * @param moviesFile file with movie names and IDs
     * @param actorsMoviesFile file with movies and actors in them
     * @throws IOException
     */
    public BaconGame(String center, String actorsFile, String moviesFile, String actorsMoviesFile) throws IOException {
        // create a graph when the game is constructed
        GraphBuilder graphBuilder = new GraphBuilder(actorsFile, moviesFile, actorsMoviesFile);
        this.graph = graphBuilder.buildGraph();
        this.center = center;
        startNewGame(); // starts the game
    }

    /**
     * Takes an initial center and starts the game, printing all game commands, and then waiting for the first command
     */
    public void startNewGame() {
        System.out.println("Commands:");
        System.out.println("c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation");
        System.out.println("d <#>: list top (positive number) or bottom (negative) <#> actors sorted by degree");
        System.out.println("i: list actors with infinite separation from the current center");
        System.out.println("p <name>: find path from <name> to current center of the universe");
        System.out.println("u <name>: make <name> the center of the universe");
        System.out.println("q: quit game");
        newCenter(this.center);
        readCommand();
    }

    /**
     * Initializes a new center for the game.  Called either by user input of the command "u" and then an actor name
     * or by the first call to start the game
     * @param center name of the new actor to serve as the center of the game universe
     */
    public void newCenter(String center) {
        this.center = center; // override the current center
        this.tree = GraphLib.bfs(graph, this.center); // override the current tree to one with the new center at its root
        // print initial information on the new center
        System.out.println(this.center + " is now the center of the acting universe, connected to " + tree.numVertices()
                + "/" + graph.numVertices() + " actors with average separation " + GraphLib.averageSeparation(tree, this.center));
    }

    /**
     * Prompts the user to input a command and then reads and responds to the user input
     */
    public void readCommand() {
        System.out.println();
        System.out.println(center + " Game >"); // state the current center
        System.out.println("Input game command: "); // prompt the user command
        Scanner input = new Scanner(System.in); // start scanning for input
        String command = input.nextLine(); // take in the next inputted line as the command
        if (command.equals("i")) {
            printMissingVertices(); // print missing vertices as the i command is meant to do
            readCommand(); // wait for a new command
        }
        else if (command.equals("q")) {
            System.out.println("Game Over."); // print game over and don't call readCommand()
        }
        else if (command.equals("c") | command.equals("d") | command.equals("p") |command.equals("u")) {
            readParameter(command); // these commands require further user input so send them to readParameter()
        }
        else {
            System.out.println("Invalid input, try again"); // input did not match any options
            readCommand(); // wait for a new command
        }
    }

    /**
     * Prompts and reads parameters for game commands that need them
     * @param command the command inputted by the user and passed in to the method
     */
    public void readParameter(String command) {
        if (command.equals("c")) {
            System.out.println("List this many actors: "); // asking for # of actors to the listed
            Scanner input = new Scanner(System.in); // scanning for input
            int parameter = input.nextInt();
            if (parameter == 0) {
                System.out.println("Top 0 actors requested"); // no need to run the method as no actors will be listed
            }
            else {
                bestCenters(parameter); // call the bestCenters() method
            }
        }
        else if (command.equals("p")) {
            System.out.println("From " + center + " to: "); // ask for the actor to connect the center to
            Scanner input = new Scanner(System.in); // scanning for input
            String actor = input.nextLine();
            printPath(actor); // print the path from the center to the inputted actor
        }
        else if (command.equals("u")) {
            System.out.println("New center of the universe: "); // ask for the name of the new center
            Scanner input = new Scanner(System.in);
            String actor = input.nextLine();
            newCenter(actor); // call the newCenter() method to set the new center
        }
        else if (command.equals("d")) {
            System.out.println("List this many actors: "); // ask for # of actors to be listed
            Scanner input = new Scanner(System.in);
            int parameter = input.nextInt();
            if (parameter == 0) {
                System.out.println("Top 0 actors requested");
            }
            else {
                bestByDegree(parameter); // call bestByDegree to print parameter # of the most/least connected actors
            }
        }
        readCommand(); // wait for a new command
    }

    /**
     * Prints all actors not connected to the current center
     */
    public void printMissingVertices() {
        Set<String> missingVertices = GraphLib.missingVertices(graph, tree); // get missing vertices
        System.out.println("Disconnected Vertices:");
        for (String missingActor : missingVertices) { // print each unconnected actor
            System.out.println(missingActor);
        }
    }

    /**
     * Prints the path from the center to the param actor
     * @param actor actor to connect to the center
     */
    public void printPath(String actor) {
        List<String> path = GraphLib.getPath(tree, actor); // create the path list
        if (path == null) System.out.println(actor + " is not connected to " + center ); // if there is no path, say so
        else {
            System.out.println(actor + "'s number is " + (path.size() - 1)); // print the Bacon (or other center) #
            for (int indx = 0; indx < path.size() - 1; indx++) { // for each index in the list except the last
                String costar = path.get(indx); // get the actor at the index
                // get one movie shared by the actor and the next actor in the list
                Set<String> label = graph.getLabel(costar, path.get(indx + 1));
                // print the path information (actor x appeared with next actor y in z movie)
                System.out.println(costar + " appeared in " + label + " with " + path.get(indx + 1));
            }
        }
    }

    /**
     * Prints the num best (or worst) centers based on average separation
     * @param num the num of actors to be printed (min average separation if num<0 and max otherwise)
     */
    public void bestCenters(int num) {
        Graph<String, Set<String>> baconUniverse = GraphLib.bfs(this.graph, "Kevin Bacon");
        PriorityQueue<Actor> centerQueue;
        if (num < 0) { // if num < 0, make centerQueue a min PriorityQueue
            centerQueue = new PriorityQueue<Actor>((a1, a2) -> Double.compare(a1.getValue(), a2.getValue()));
        } else { // else make centerQueue a max PriorityQueue
            centerQueue = new PriorityQueue<Actor>((a1, a2) -> Double.compare(a2.getValue(), a1.getValue()));
        }

        for (String v : baconUniverse.vertices()) { // for each actor in the Bacon Universe
            Graph<String, Set<String>> tree = GraphLib.bfs(this.graph, v); // create a tree from their connections
            double averageSeparation = GraphLib.averageSeparation(tree, v); // calculate the actors average separation
            BaconGame.Actor centerSeparation = new BaconGame.Actor(v, averageSeparation); // create an actor class from the value
            centerQueue.add(centerSeparation); // add the Actor to the queue
        }

        for (int i = 1; i <= Math.abs(num); ++i) { // remove from the queue and print based on the # of actors requested
            BaconGame.Actor actor = centerQueue.poll();
            System.out.println(actor.getActor() + " with average separation " + actor.getValue());
        }
    }

    /**
     * Print the num best centers by degree
     * @param num the num of actors to be printed (min degree if num<0 and max otherwise)
     */
    public void bestByDegree(int num) {
        // get the sorted list of actors (all edges are undirected so InDegree == OutDegree == Degree)
        List<String> actorsByDegree = GraphLib.verticesByInDegree(this.graph);
        if (num < 0) { // if looking for the lowest degree actors
            int size = actorsByDegree.size() - 1;
            for (int i = 0; i <= Math.abs(num) - 1; i++) { // take from the back of the list and print num of actors
                System.out.println(actorsByDegree.get(size - i) + " with " + graph.inDegree(actorsByDegree.get(size - i)) + " direct connections.");
            }
        } else { // else if looking for highest degree actors
            for (int i = 0; i <= num - 1; i++) { // take and print from the front of the list
                System.out.println(actorsByDegree.get(i) + " with " + graph.inDegree(actorsByDegree.get(i)) + " direct connections.");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BaconGame test0 = new BaconGame("Kevin Bacon", "PS4/actors.txt", "PS4/movies.txt", "PS4/movie-actors.txt");
        // BaconGame test1 = new BaconGame("Kevin Bacon", "PS4/actorsTest.txt", "PS4/moviesTest.txt", "PS4/movie-actorsTest.txt");
    }
}
