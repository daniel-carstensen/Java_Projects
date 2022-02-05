import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;

public class Placeholder {
    
    private String filePath;
    private PriorityQueue<BinaryTree<CharacterAndFrequency>> initialTreeQueue;


    public Placeholder (String filePath) throws FileNotFoundException {
        this.filePath = filePath;
        initialTreeQueue = new PriorityQueue<>();
    }

    private HashMap<Character, Integer> fillTable () throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath));
        HashMap<Character, Integer> frequencyMap = new HashMap<>();
        
        while (input.ready()) {
            char currentC = (char) input.read();
            if (frequencyMap.containsKey(currentC)) {
                int newFrequency = (int) frequencyMap.get(currentC) + 1;
                frequencyMap.put(currentC, newFrequency);
            }
            else frequencyMap.put(currentC, 1);
            System.out.println(currentC);
        }
        return frequencyMap;
    }

    private void fillQueue () throws IOException {
        HashMap<Character, Integer> frequencyMap = fillTable();
        ArrayList<Character> allCharacters = (ArrayList) frequencyMap.keySet();
        for (Character key : allCharacters) {
            initialTreeQueue.add(new BinaryTree<>(new CharacterAndFrequency(key, frequencyMap.get(key))));
        }
    }


    public static void main(String[] args) throws IOException {
        Placeholder usConstitution = new Placeholder("PS3/USConstitution.txt");

    }
}
