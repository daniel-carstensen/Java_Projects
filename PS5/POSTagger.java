import jdk.jfr.Frequency;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;

import static java.lang.Math.log;

public class POSTagger {
    String trainSentenceFile;
    String trainTagFile;
    HashMap<String, HashMap<String, Double>> transitions;
    HashMap<String, HashMap<String, Double>> observations;

    public POSTagger (String trainSentenceFile, String trainTagFile) {
        if (new File(trainSentenceFile).length() == 0) System.out.println("The given file is empty: " + trainSentenceFile);
        if (new File(trainTagFile).length() == 0) System.out.println("The given file is empty " + trainTagFile);
        this.trainSentenceFile = trainSentenceFile;
        this.trainTagFile = trainTagFile;
        transitions = new HashMap<>();
        observations = new HashMap<>();
    }

    private void trainModel () throws IOException {
        BufferedReader sentenceInput = new BufferedReader(new FileReader(trainSentenceFile));
        BufferedReader tagInput = new BufferedReader(new FileReader(trainTagFile));

        while (sentenceInput.ready()) {
            String[] sentence = sentenceInput.readLine().toLowerCase().split("\\ ");
            String[] tags = tagInput.readLine().split("\\ ");
            for (int i = 0; i < sentence.length; i ++) {
                String currentWord = sentence[i];
                String currentTag = tags[i];

                if (!observations.containsKey(currentTag)) {
                    HashMap<String, Double> wordFrequency = new HashMap<>();
                    wordFrequency.put(currentWord, 1.0);
                    observations.put(currentTag, wordFrequency);
                }
                else {
                    HashMap<String, Double> wordFrequency = observations.get(currentTag);
                    if (!wordFrequency.containsKey(currentWord)) {
                        wordFrequency.put(currentWord, 1.0);
                    }
                    else {
                        wordFrequency.put(currentWord, wordFrequency.get(currentWord)+1);
                    }
                }

                if (i < sentence.length-1) {
                    String nextTag = tags[i+1];
                    if (!transitions.containsKey(currentTag)) {
                        HashMap<String, Double> transitionFrequency = new HashMap<>();
                        transitionFrequency.put(nextTag, 1.0);
                        transitions.put(currentTag, transitionFrequency);
                    }
                    else {
                        HashMap<String, Double> transitionFrequency = transitions.get(currentTag);
                        if (!transitionFrequency.containsKey(nextTag)) {
                            transitionFrequency.put(nextTag, 1.0);
                        }
                        else {
                            transitionFrequency.put(nextTag, transitionFrequency.get(nextTag)+1);
                        }
                    }
                }
            }
        }

        for (String tag : observations.keySet()) {
            HashMap<String, Double> wordFrequency = observations.get(tag);
            double total = totalValue(wordFrequency);
            //System.out.println(tag + ", total: " + total);
            for (String word : wordFrequency.keySet()) {
                wordFrequency.put(word, log(wordFrequency.get(word)/total));
            }
        }

        for (String tag : transitions.keySet()) {
            HashMap<String, Double> transitionFrequency = transitions.get(tag);
            double total = totalValue(transitionFrequency);
            //System.out.println(tag + ", total: " + total);
            for (String nextTag : transitionFrequency.keySet()) {
                transitionFrequency.put(nextTag, log(transitionFrequency.get(nextTag)/total));
            }
        }
    }

    private double totalValue (HashMap<String, Double> map) {
        int total = 0;

        for (String key : map.keySet()) {
            total += map.get(key);
        }

        return total;
    }

    private void Viterbi () throws FileNotFoundException {

    }

    public static void main(String[] args) throws IOException {
        POSTagger test = new POSTagger("PS5/simple-train-sentences.txt", "PS5/simple-train-tags.txt");
        test.trainModel();
        System.out.println(test.observations);
        System.out.println(test.transitions);
    }
}
