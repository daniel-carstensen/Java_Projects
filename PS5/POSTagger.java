import jdk.jfr.Frequency;

import java.io.*;
import java.util.*;

import static java.lang.Math.log;

public class POSTagger {
    String trainSentenceFile;
    String trainTagFile;
    String testSentenceFile;
    String testTagFile;
    HashMap<String, HashMap<String, Double>> transitions;
    HashMap<String, HashMap<String, Double>> observations;

    public POSTagger (String trainSentenceFile, String trainTagFile, String testSentenceFile, String testTagFile) {
        if (new File(trainSentenceFile).length() == 0) System.out.println("The given file is empty: " + trainSentenceFile);
        if (new File(trainTagFile).length() == 0) System.out.println("The given file is empty: " + trainTagFile);
        if (new File(testSentenceFile).length() == 0) System.out.println("The given file is empty: " + testSentenceFile);
        if (new File(testTagFile).length() == 0) System.out.println("The given file is empty: " + testTagFile);

        this.trainSentenceFile = trainSentenceFile;
        this.trainTagFile = trainTagFile;
        this.testSentenceFile = testSentenceFile;
        this.testTagFile = testTagFile;
        this.transitions = new HashMap<>();
        this.observations = new HashMap<>();
    }

    private void trainModel () throws IOException {
        BufferedReader sentenceInput = new BufferedReader(new FileReader(trainSentenceFile));
        BufferedReader tagInput = new BufferedReader(new FileReader(trainTagFile));

        transitions.put("#", new HashMap<>());
        HashMap<String, Double> startTransition = transitions.get("#");

        while (sentenceInput.ready()) {
            String[] sentence = sentenceInput.readLine().toLowerCase().split("\\ ");
            String[] tags = tagInput.readLine().split("\\ ");
            String startTag = tags[0];

            if (!startTransition.containsKey(startTag)) {
                startTransition.put(startTag, 1.0);
            }
            else {
                startTransition.put(startTag, startTransition.get(startTag)+1);
            }

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

    private String[] Viterbi (String[] sentence) throws IOException {
        HashSet<String> currentStates = new HashSet<>();
        HashMap<String, Double> currentScores = new HashMap<>();
        Stack<HashMap<String, String>> backTrack = new Stack<>();
        String[] path = new String[sentence.length];
        currentStates.add("#");
        currentScores.put("#", 0.0);

        for (int i = 0; i < sentence.length; i ++) {
            String currentWord = sentence[i];
            HashSet<String> nextStates = new HashSet();
            HashMap<String, Double> nextScores = new HashMap<>();
            HashMap<String, String> predecessors = new HashMap<>();

            for (String currentState : currentStates) {
                HashMap<String, Double> transitionFrequency = transitions.get(currentState);
                if (transitionFrequency == null) continue;
                for (String transition : transitionFrequency.keySet()) {
                    if (!nextStates.contains(transition)) nextStates.add(transition);
                    double nextScore = currentScores.get(currentState) + transitionFrequency.get(transition);
                    if (!observations.get(transition).containsKey(currentWord)) {
                        nextScore += -100;
                    }
                    else {
                        nextScore += observations.get(transition).get(currentWord);
                    }
                    if (!nextScores.containsKey(transition) || nextScores.get(transition) < nextScore)  {
                        nextScores.put(transition, nextScore);
                        predecessors.put(transition, currentState);
                    }
                }
            }
            backTrack.push(predecessors);
            currentStates = nextStates;
            currentScores = nextScores;
        }
        String bestTransition = null;
        for (String transition : currentScores.keySet()) {
            if (bestTransition == null || currentScores.get(bestTransition) < currentScores.get(transition)) bestTransition = transition;
        }
        int i = path.length-1;
        while (!backTrack.empty()) {
            path[i] = bestTransition;
            HashMap<String, String> currentBackTrack = backTrack.pop();
            bestTransition = currentBackTrack.get(bestTransition);
            i --;
        }
//        for (String s : path) {
//            System.out.println(s);
//        }
        return path;
    }

    private void testModel () throws IOException {
        BufferedReader sentenceInput = new BufferedReader(new FileReader(testSentenceFile));
        BufferedReader tagInput = new BufferedReader(new FileReader(testTagFile));
        int correctTags = 0;
        int incorrectTags = 0;

        while (sentenceInput.ready()) {
            String[] sentence = sentenceInput.readLine().toLowerCase().split("\\ ");
            String[] testTags = tagInput.readLine().split("\\ ");
            String[] computedTags = Viterbi(sentence);
            for (int i = 0; i < testTags.length; i++) {
                if (testTags[i].equals(computedTags[i])) correctTags++;
                else {
                    System.out.println(sentence[i] + " was incorrectly tagged as " + computedTags[i] + " when it actually was " + testTags[i]);
                    incorrectTags++;
                }
            }
        }

        int totalTags = correctTags + incorrectTags;
        System.out.println("Total tags: " + totalTags);
        System.out.println("Correct tags: " + correctTags);
        System.out.println("Incorrect tags: " + incorrectTags);
    }

    public static void main(String[] args) throws IOException {
//        POSTagger test = new POSTagger("PS5/example-sentences.txt", "PS5/example-tags.txt", "PS5/example-sentences.txt", "PS5/example-tags.txt");
        //POSTagger test = new POSTagger("PS5/simple-train-sentences.txt", "PS5/simple-train-tags.txt", "PS5/simple-test-sentences.txt", "PS5/simple-test-tags.txt");
        POSTagger test = new POSTagger("PS5/brown-train-sentences.txt", "PS5/brown-train-tags.txt", "PS5/brown-test-sentences.txt", "PS5/brown-test-tags.txt");
        test.trainModel();
        System.out.println(test.observations);
        System.out.println(test.transitions);
//        String[] testString = {"will", "eats", "the", "fish"};
//        test.Viterbi(testString);
        test.testModel();
    }
}
