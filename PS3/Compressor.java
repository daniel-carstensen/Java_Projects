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
        this.compressedFilePath = "PS3/compressedFile.txt"; // default file name for testing
        this.decompressedFilePath = "PS3/decompressedFile.txt"; // default file name for testing
    }

    /**
     * Constructor
     * @param filePath the path name of the file to open
     * @param compressedFilePath the path name of the file to write the compressed file into
     * @param decompressedFilePath the path name of the file to write the decompressed file into
     */
    public Compressor(String filePath, String compressedFilePath, String decompressedFilePath) {
        this.filePath = filePath;
        this.compressedFilePath = compressedFilePath;
        this.decompressedFilePath = decompressedFilePath;
    }

    /**
     * Reads through each character in the text file and constructs a frequency map for each character used
     * and that character's frequency in the file.
     * @return frequencyMap a HashMap holding characters in the text file and their frequencies
     * @throws IOException
     */
    private HashMap<Character, Integer> fillTable () throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath)); // creating a reader
        // create a HashMap to hold characters and frequencies
        HashMap<Character, Integer> frequencyMap = new HashMap<>();
        while (input.ready()) { // loop through all characters in the input file
            char currentC = (char) input.read();
            if (frequencyMap.containsKey(currentC)) { // if this character has already been added ...
                int newFrequency = frequencyMap.get(currentC) + 1; // increment the frequency
                frequencyMap.put(currentC, newFrequency); // and override it with the newFrequency
            }
            else frequencyMap.put(currentC, 1); // if the character was not already added, add it with frequency 1
        }
        input.close();
        return frequencyMap; // return the final HashMap
    }

    /**
     * Creates and returns a PriorityQueue of BinaryTree nodes where nodes with lower character frequencies
     * receive lower priority
     * @return initialTreeQueue a PriorityQueue which holds BinaryTree nodes that hold CharFreq objects
     * @throws IOException
     */
    private PriorityQueue fillQueue () throws IOException {
        // Using an anonymous comparator
        PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue = new PriorityQueue<>((b1, b2) -> {
            if (b1.getData().getFrequency() < b2.getData().getFrequency()) return -1;
            else if (b1.getData().getFrequency() == b2.getData().getFrequency()) return 0;
            else return 1;
        });

        HashMap<Character, Integer> frequencyMap = fillTable(); // get the character and frequency map using fillTable()

        Set<Character> allCharacters = frequencyMap.keySet(); // creating a set from the map to index through
        for (Character key : allCharacters) {
            // add each CharFreq object to the initialTreeQueue with the comparator organizing them by frequency
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
        PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue = fillQueue(); // get the Queue by calling fillQueue()
        // if there was only one character type (either repeater or one instance) in the original file,
        // construct the binary tree by simply adding this CharFreq object to a root and returning the tree
        if (initialTreeQueue.size() == 1) {
            BinaryTree<CharFreq> t1 = initialTreeQueue.poll();
            CharFreq root = new CharFreq('/', t1.getData().getFrequency());
            return new BinaryTree<CharFreq>(root, t1, null);
        }
        else if (initialTreeQueue.isEmpty()) { // if the original file was empty, return an empty tree
            return new BinaryTree<CharFreq>(null);
        }
        return recursiveBuilding(initialTreeQueue); // otherwise, call recursiveBuilding() to construct the tree
    }

    /**
     * Constructs a BinaryTree using the initialTreeQueue.  The leaves of the BinaryTree hold CharFreq objects with
     * from initialTreeQueue and are organized so that each CharFreq object has a unique path through the tree and
     * so that the characters with the highest frequencies have the shortest paths.
     * @param initialTreeQueue the PriorityQueue created by fillQueue()
     * @return a BinaryTree
     */
    private BinaryTree recursiveBuilding(PriorityQueue<BinaryTree<CharFreq>> initialTreeQueue) {
        if (initialTreeQueue.size() >= 2) { // if there is not yet only 1 binary tree held in the queue
            // pop off and store the first two BinaryTrees in the queue
            BinaryTree<CharFreq> t1 = initialTreeQueue.poll();
            BinaryTree<CharFreq> t2 = initialTreeQueue.poll();
            // create a root with frequency value equal to the sum of t1.frequency and t2.frequency and an arbitrary
            // character value
            CharFreq root = new CharFreq('/', t1.getData().getFrequency() + t2.getData().getFrequency());
            initialTreeQueue.add(new BinaryTree<CharFreq>(root, t1, t2)); // add the root back into the Queue
             return recursiveBuilding(initialTreeQueue); // recursively call the method with the new initialTreeQueue
        }
        return initialTreeQueue.poll(); // remove and return the final tree from initialTreeQueue
    }

    /**
     * Creates an empty codeMap and then calls codeMapRecursion to fill the map
     * @param tree a BinaryTree constructed in createTree
     * @return codeMap a HashMap holding characters and their paths from the BinaryTree
     */
    public HashMap<Character, String> getCodeMap(BinaryTree<CharFreq> tree) {
        HashMap<Character, String> codeMap = new HashMap<Character, String>(); // create empty HashMap, codeMap
        if (tree.getData() == null) { // if the tree is empty, return an empty codeMap
            return codeMap;
        }
        return codeMapRecursion(tree, "", codeMap); // otherwise, call codeMapRecursion() to fill the codeMap

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
        // recursively traversing the tree
        if (tree.hasLeft()) {
            codeMapRecursion(tree.getLeft(), path + "0", codeMap);
        }
        if (tree.hasRight()) {
            codeMapRecursion(tree.getRight(), path + "1", codeMap);
        }
        if (tree.isLeaf()) {
            // each leaf is a real CharFreq node, so when a leaf is reached return its character and the path to
            // get to it
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
        BufferedReader input = new BufferedReader(new FileReader(filePath)); // create text reader
        BufferedBitWriter bitOutput = new BufferedBitWriter(compressedFilePath); // create bit writer
        BinaryTree<CharFreq> tree = createTree(); // create the tree using createTree()
        HashMap<Character, String> codeMap = getCodeMap(tree); // create the codeMap by calling getCodeMap() on the tree
        while (input.ready()) { // for each character in the file
            char currentC = (char) input.read();
            String code = codeMap.get(currentC); // get the path or "code" for the character from the codeMap
            char[] codeArray = code.toCharArray(); // convert the code to an Array so that it can be easily looped through
            for (char c : codeArray) {
                // write out each character in codeArray as bits (false for '0' and true for '1') into the new file
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
        BufferedBitReader bitInput = new BufferedBitReader(compressedFilePath); // create a bit reader
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedFilePath)); // create a text writer
        BinaryTree<CharFreq> originaltree = createTree(); // creates the tree to use as a code for decompression
        BinaryTree<CharFreq> tree = originaltree;
        while (bitInput.hasNext()) {
            // for each bit, progress through the tree
            boolean bit = bitInput.readBit();
            if (bit) {
                tree = tree.getRight();
            }
            if (!bit) {
                tree = tree.getLeft();
            }
            if (tree.isLeaf()) { // when you hit a leaf, write it's character into the decompressed file
                output.write(tree.getData().getCharacter());
                tree = originaltree; // reset the tree for the next character
            }
        }
        bitInput.close();
        output.close();
    }

    public static void main(String[] args) throws IOException {
        Compressor emptyFile = new Compressor("PS3/emptyFile.txt", "PS3/compressedEmptyFile.txt", "" +
                "PS3/decompressedEmptyFile.txt");

        System.out.println("emptyFile Tree: " + emptyFile.createTree());

        System.out.println("emptyFile HashMap: " + emptyFile.getCodeMap(emptyFile.createTree()));

        emptyFile.compress();
        emptyFile.decompress();

        Compressor singleLetterFile = new Compressor("PS3/singleLetterFile.txt", "PS3/compressedSingleLetterFile.txt", "" +
                "PS3/decompressedSingleLetterFile.txt");

        System.out.println("singleLetterFile Tree: " + singleLetterFile.createTree());

        System.out.println("emptyFile HashMap: " + singleLetterFile.getCodeMap(singleLetterFile.createTree()));

        singleLetterFile.compress();
        singleLetterFile.decompress();

        Compressor usConstitution = new Compressor("PS3/USConstitution.txt",
                "PS3/compressedUSConstitution.txt",
                "PS3/decompressedUSConstitution.txt");
        System.out.println("USConstitution tree : " + usConstitution.createTree()); // print Tree
        System.out.println("USConstitution codeMap: " + usConstitution.getCodeMap(usConstitution.createTree())); // print codeMap
        usConstitution.compress();
        usConstitution.decompress();

        Compressor repeatedCharacter = new Compressor("PS3/RepeatedCharacter.txt",
                "PS3/compressedRepeatedCharacter.txt",
                "PS3/decompressedRepeatedCharacter.txt");
        System.out.println("RepeatedCharacter tree : " + repeatedCharacter.createTree()); // print Tree
        System.out.println("RepeatedCharacter codeMap: " + repeatedCharacter.getCodeMap(repeatedCharacter.createTree())); // print codeMap
        repeatedCharacter.compress(); // compress
        repeatedCharacter.decompress(); // decompress
    }
}
