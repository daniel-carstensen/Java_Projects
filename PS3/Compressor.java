import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Compressor {
    private String filePath;

    public Compressor(String filePath) {
        this.filePath = filePath;
    }

    private HashMap<Character, Integer> fillTable () throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath));
        HashMap<Character, Integer> frequencyMap = new HashMap<>();
        
        while (input.ready()) {
            char currentC = (char) input.read();
            if (frequencyMap.containsKey(currentC)) {
                int newFrequency = frequencyMap.get(currentC) + 1;
                frequencyMap.put(currentC, newFrequency);
            }
            else frequencyMap.put(currentC, 1);
        }
        return frequencyMap;
    }

    private PriorityQueue fillQueue () throws IOException {
        PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue = new PriorityQueue<>((b1, b2) -> {
            if (b1.data.getFrequency() < b2.data.getFrequency()) return -1;
            else if (b1.data.getFrequency() == b2.data.getFrequency()) return 0;
            else return 1;
        });
        HashMap<Character, Integer> frequencyMap = fillTable();

        Set<Character> allCharacters = frequencyMap.keySet();
        for (Character key : allCharacters) {
            initialTreeQueue.add(new BinaryTree<>(new CharFreq(key, frequencyMap.get(key))));
        }
        return initialTreeQueue;
    }

    private BinaryTree createTree () throws IOException {
        PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue = fillQueue();
        return recursiveBuilding(initialTreeQueue);
    }

    private BinaryTree recursiveBuilding(PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue) {
        if (initialTreeQueue.size() >= 2) {
            BinaryTree<CharFreq> t1 = initialTreeQueue.poll();
            BinaryTree<CharFreq> t2 = initialTreeQueue.poll();
            CharFreq root = new CharFreq('/', t1.data.getFrequency() + t2.data.getFrequency());
            initialTreeQueue.add(new BinaryTree<CharFreq>(root, t1, t2));
             return recursiveBuilding(initialTreeQueue);
        }

        return initialTreeQueue.poll();
    }


    public static void main(String[] args) throws IOException {
        Compressor usConstitution = new Compressor("PS3/test.txt");
        Object[] a = usConstitution.fillQueue().toArray();
        for (int j = 0; j < a.length; j++)
            System.out.println(a[j]);
        System.out.println(usConstitution.createTree());
    }
}
