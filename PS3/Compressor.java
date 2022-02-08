import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Compressor {
    private String filePath;
    private String compressedFilePath;
    private String decompressedFilePath;

    /**
     * Constructor
     * @param filePath the path name of the file to open
     */
    public Compressor(String filePath) {
        this.filePath = filePath;
        this.compressedFilePath = "PS3/compressedFile.txt";
        this.decompressedFilePath = "PS3/decompressedFile.txt";
    }

    /**
     * Reads through each character in the text file and constructs a frequency map for each character used
     * and that character's frequency in the file.
     * @return frequencyMap a HashMap holding characters in the text file and their frequencies
     * @throws IOException
     */
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
        input.close();
        return frequencyMap;
    }

    /**
     * Creates and returns a PriorityQueue of BinaryTree nodes where nodes with lower character frequencies
     * receive lower priority
     * @return initialTreeQueue a PriorityQueue which holds BinaryTree nodes that hold CharFreq objects
     * @throws IOException
     */
    private PriorityQueue fillQueue () throws IOException { // im confused here.  Where do b1 and b2 come from?
        PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue = new PriorityQueue<>((b1, b2) -> {
            if (b1.getData().getFrequency() < b2.getData().getFrequency()) return -1;
            else if (b1.getData().getFrequency() == b2.getData().getFrequency()) return 0;
            else return 1;
        });
        HashMap<Character, Integer> frequencyMap = fillTable();

        Set<Character> allCharacters = frequencyMap.keySet();
        for (Character key : allCharacters) {
            initialTreeQueue.add(new BinaryTree<>(new CharFreq(key, frequencyMap.get(key))));
        }
        return initialTreeQueue;
    }

    /**
     * calls fillQueue() to create the initialTreeQueue and then calls recursiveBuilding to construct a BinaryTree
     * @return a BinaryTree
     * @throws IOException
     */
    private BinaryTree createTree () throws IOException {
        PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue = fillQueue();
        return recursiveBuilding(initialTreeQueue);
    }

    /**
     * Constructs a BinaryTree using the initialTreeQueue.  The leaves of the BinaryTree hold CharFreq objects with
     * from initialTreeQueue and are organized so that each CharFreq object has a unique path through the tree and
     * so that the characters with the highest frequencies have the shortest paths.
     * @param initialTreeQueue the PriorityQueue created by fillQueue()
     * @return a BinaryTree
     */
    private BinaryTree recursiveBuilding(PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue) {
        if (initialTreeQueue.size() >= 2) {
            BinaryTree<CharFreq> t1 = initialTreeQueue.poll();
            BinaryTree<CharFreq> t2 = initialTreeQueue.poll();
            CharFreq root = new CharFreq('/', t1.getData().getFrequency() + t2.getData().getFrequency());
            initialTreeQueue.add(new BinaryTree<CharFreq>(root, t1, t2));
             return recursiveBuilding(initialTreeQueue);
        }
        return initialTreeQueue.poll();
    }

    /**
     * Creates an empty codeMap and then calls codeMapRecursion to fill the map
     * @param tree a BinaryTree constructed in createTree
     * @return codeMap a HashMap holding characters and their paths from the BinaryTree
     */
    public HashMap<Character, String> getCodeMap(BinaryTree<CharFreq> tree) {
        HashMap<Character, String> codeMap = new HashMap<Character, String>();
        return codeMapRecursion(tree, "", codeMap);

    }

    /**
     * Creates a codeMap holding characters and the paths to get to them in the tree
     * @param tree BinaryTree created by createTree() and passed in through getCodeMap()
     * @param path the path to each character, starts at "" and is built as the function recursively traverses
     *             the tree
     * @param codeMap the codeMap to be filled in by codeMapRecursion()
     * @return
     */
    private HashMap<Character, String> codeMapRecursion(BinaryTree<CharFreq> tree, String path, HashMap<Character, String> codeMap) {
        if (tree.hasLeft()) {
            codeMapRecursion(tree.getLeft(), path + "0", codeMap);
        }
        if (tree.hasRight()) {
            codeMapRecursion(tree.getRight(), path + "1", codeMap);
        }
        if (tree.isLeaf()) {
            codeMap.put(tree.getData().getCharacter(), path);
        }
        return codeMap;
    }

    /**
     * Calls createTree() and getCodeMap() to create a key for compression and then reads through each character
     * in the original text file and compresses it into bits written into a new file holding bytes
     * @throws IOException
     */
    private void compress() throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath));
        BufferedBitWriter bitOutput = new BufferedBitWriter(compressedFilePath);
        BinaryTree<CharFreq> tree = createTree();
        HashMap<Character, String> codeMap = getCodeMap(tree);
        while (input.ready()) {
            char currentC = (char) input.read();
            String code = codeMap.get(currentC);
            char[] codeArray = code.toCharArray();
            for (char c : codeArray) {
                if (c == '0') {
                    bitOutput.writeBit(false);
                }
                if (c == '1') {
                    bitOutput.writeBit(true);
                }
            }
        }
        input.close();
        bitOutput.close();
    }

    /**
     * Calls createTree() to create a key for decompression and then reads through each bit
     * in the compressed file and decompresses it into characters written into a new text file
     * @throws IOException
     */
    private void decompress() throws IOException {
        BufferedBitReader bitInput = new BufferedBitReader(compressedFilePath);
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedFilePath));
        BinaryTree<CharFreq> originaltree = createTree();
        BinaryTree<CharFreq> tree = originaltree;
        while (bitInput.hasNext()) {
            boolean bit = bitInput.readBit();
            if (bit) {
                tree = tree.getRight();
            }
            if (!bit) {
                tree = tree.getLeft();
            }
            if (tree.isLeaf()) {
                output.write(tree.getData().getCharacter());
                tree = originaltree;
            }
        }
        bitInput.close();
        output.close();
    }

    public static void main(String[] args) throws IOException {
        Compressor usConstitution = new Compressor("PS3/WarAndPeace.txt");
//        Object[] a = usConstitution.fillQueue().toArray();
//        for (int j = 0; j < a.length; j++)
//            System.out.println(a[j]);
//        System.out.println(usConstitution.getCodeMap(usConstitution.createTree()));
        usConstitution.compress();
        usConstitution.decompress();
    }
}
