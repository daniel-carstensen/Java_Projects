import jdk.jfr.Frequency;

import java.io.*;
import java.util.*;

import static java.lang.Math.log;

/**
 * POS tagger to train HMM to tag parts-of-speech, tag unknown sentence data, and test the performance of the model
 *
 * @author Daniel Carstensen, Winter 2022, Dartmouth CS10, pset 4
 * @author Max Lawrence, Winter 2022, Dartmouth CS10, pset 5
 */
public class POSTagger {
    // Create variables to hold the file paths
    String trainSentenceFile;
    String trainTagFile;
    String testSentenceFile;
    String testTagFile;
    // Create variables to hold the transitions and observations as a nested HashMap
    HashMap<String, HashMap<String, Double>> transitions;
    HashMap<String, HashMap<String, Double>> observations;

    /**
     * instantiate path variables using input paths and empty nested HashMaps
     *
     * @param trainSentenceFile
     * @param trainTagFile
     * @param testSentenceFile
     * @param testTagFile
     */
    public POSTagger (String trainSentenceFile, String trainTagFile, String testSentenceFile, String testTagFile) {
        // print out warnings if any of the files are empty
        if (new File(trainSentenceFile).length() == 0) System.out.println("The given file is empty: " + trainSentenceFile);
        if (new File(trainTagFile).length() == 0) System.out.println("The given file is empty: " + trainTagFile);
        if (new File(testSentenceFile).length() == 0) System.out.println("The given file is empty: " + testSentenceFile);
        if (new File(testTagFile).length() == 0) System.out.println("The given file is empty: " + testTagFile);

        // instantiate file paths and transition and observation maps
        this.trainSentenceFile = trainSentenceFile;
        this.trainTagFile = trainTagFile;
        this.testSentenceFile = testSentenceFile;
        this.testTagFile = testTagFile;
        this.transitions = new HashMap<>();
        this.observations = new HashMap<>();
    }

    /**
     * train the HMM using the input training data by filling up the observations and transitions HashMaps
     *
     * @throws IOException
     */
    private void trainModel () throws IOException {
        BufferedReader sentenceInput = new BufferedReader(new FileReader(trainSentenceFile)); // create a reader for the sentences
        BufferedReader tagInput = new BufferedReader(new FileReader(trainTagFile)); // create a reader for the tags

        transitions.put("#", new HashMap<>()); // put the start and an empty HashMap into transitions to account for the starting position
        HashMap<String, Double> startTransition = transitions.get("#"); // create variable for the map recording transitions from the start

        // fill the observations and transitions maps
        while (sentenceInput.ready()) { // while there are new sentences
            String[] sentence = sentenceInput.readLine().toLowerCase().split("\\ "); // create an array containing all words in the current sentence
            String[] tags = tagInput.readLine().split("\\ "); // create an array containing all corresponding tags in the current tag line
            String startTag = tags[0]; // create a variable for the first tag

            // account for the initial transition from the start
            if (!startTransition.containsKey(startTag)) { // if the initial transition has not been recorded yet
                startTransition.put(startTag, 1.0); // put the startTag in the starting transition with value 1
            }
            else { // if the initial transition has already been recorded at least once
                startTransition.put(startTag, startTransition.get(startTag)+1); // update the transition by 1
            }

            // loop through all other transitions after the initial transition
            for (int i = 0; i < sentence.length; i ++) { // for all words in the sentence
                String currentWord = sentence[i]; // create a variable for the current word
                String currentTag = tags[i]; // create a variable for the current tag

                // first fill the observations map with the current word
                if (!observations.containsKey(currentTag)) { // if a state has not been recorded yet
                    HashMap<String, Double> wordFrequency = new HashMap<>(); // create a variable that holds a wordFrequency HashMap
                    wordFrequency.put(currentWord, 1.0); // put the current word with value one into wordFrequency
                    observations.put(currentTag, wordFrequency); // put the current state with the wordFrequency map into observations
                }
                else { // if a state has already been recorded at least once
                    HashMap<String, Double> wordFrequency = observations.get(currentTag); // get the wordFrequency map
                    if (!wordFrequency.containsKey(currentWord)) { // if the current word has not been recorded yet
                        wordFrequency.put(currentWord, 1.0); // put the current word with value 1 into the wordFrequency map
                    }
                    else { // if the word bas already been recorded at least once
                        wordFrequency.put(currentWord, wordFrequency.get(currentWord)+1); // update the wordFrequency of the current word by 1
                    }
                }

                // next fill the transitions with the current transition
                if (i < sentence.length-1) { // as long as there is a transition from the current state to a next state
                    String nextTag = tags[i+1]; // create a variable for the next state
                    if (!transitions.containsKey(currentTag)) { // if the current state has not been recorded yet
                        HashMap<String, Double> transitionFrequency = new HashMap<>(); // create a variable that holds a transitionFrequency HashMap
                        transitionFrequency.put(nextTag, 1.0); // put the next state with value 1 into transitionFrequency
                        transitions.put(currentTag, transitionFrequency); // put the current state with the transitionFrequency map into transitions
                    }
                    else { // if the current state has already been recorded at least once
                        HashMap<String, Double> transitionFrequency = transitions.get(currentTag); // get the transitionFrequency map
                        if (!transitionFrequency.containsKey(nextTag)) { // if the transition to the next state has not been recorded yet
                            transitionFrequency.put(nextTag, 1.0); // put the next state with value 1 into the transitionFrequency map
                        }
                        else { // if the transition to the next state has already been recorded at least once
                            transitionFrequency.put(nextTag, transitionFrequency.get(nextTag)+1); // update the transition to the next state by 1
                        }
                    }
                }
            }
        }
        sentenceInput.close();
        tagInput.close();

        // convert the raw occurrence values of each word into a log based probability score
        for (String tag : observations.keySet()) { // loop through all states in the observations
            HashMap<String, Double> wordFrequency = observations.get(tag); // get the wordFrequency map
            double total = totalValue(wordFrequency); // create a double which holds the total occurrences of the current state
            for (String word : wordFrequency.keySet()) { // loop through all words in the wordFrequency map
                wordFrequency.put(word, log(wordFrequency.get(word)/total)); // update the value of the word to a log based probability score
            }
        }

        // convert the raw occurrence values of each transition into a log based probability score
        for (String tag : transitions.keySet()) { // loop through all states in the observations
            HashMap<String, Double> transitionFrequency = transitions.get(tag); // get the transitionFrequency map
            double total = totalValue(transitionFrequency); // create a double which holds the total occurrences of the current state
            for (String nextTag : transitionFrequency.keySet()) { // loop through all transitions in the transitionFrequency map
                transitionFrequency.put(nextTag, log(transitionFrequency.get(nextTag)/total)); // update the value of the word to a log based probability score
            }
        }
    }

    /**
     * create a helper method to count the total value stored in a HashMap
     *
     * @param map input HashMap
     * @return the total value
     */
    private double totalValue (HashMap<String, Double> map) {
        int total = 0; // create a variable to hold to total value set to 0

        for (String key : map.keySet()) { // for all keys in the input map
            total += map.get(key); // add the value associated with the key to total
        }

        return total;
    }

    /**
     * Viterbi algorithm to go through a sentence, consider all transitions base on their score in the trained HMM
     * and determine the most likely tag for each word through backtracking
     *
     * @param sentence input sentence
     * @return the most likely path as an array
     * @throws IOException
     */
    private String[] Viterbi (String[] sentence) {
        HashSet<String> currentStates = new HashSet<>(); // create an empty HashSet to hold the current states
        HashMap<String, Double> currentScores = new HashMap<>(); // create an empty HashMap to hold currentScores
        Stack<HashMap<String, String>> backTrack = new Stack<>(); // create an empty Stack to enable backtracking the most likely path
        String[] path = new String[sentence.length]; // create an empty array to hold the path of the states
        currentStates.add("#"); // start with start as the current state
        currentScores.put("#", 0.0); // assign value 0 to start as the current score

        for (int i = 0; i < sentence.length; i ++) { // loop of all words in the sentence
            String currentWord = sentence[i]; // create a variable which holds the current word of the sentence
            HashSet<String> nextStates = new HashSet(); // create an empty HashSet to hold the next states
            HashMap<String, Double> nextScores = new HashMap<>(); // create an empty HashMap to hold the next scores
            HashMap<String, String> predecessors = new HashMap<>(); // create an empty HashMap to keep track of the two states in a transition

            for (String currentState : currentStates) { // loop over all current states
                HashMap<String, Double> transitionFrequency = transitions.get(currentState); // get the transition frequency
                if (transitionFrequency == null) continue; // if the transition frequency of the current state is null, skip this iteration
                for (String transition : transitionFrequency.keySet()) { // loop over all possible transitions
                    if (!nextStates.contains(transition)) nextStates.add(transition); // if the next state of the current transition has not been encountered yet, add it to the next states
                    double nextScore = currentScores.get(currentState) + transitionFrequency.get(transition); // create a double which is the sum of the current score and the value of the current transition
                    if (!observations.get(transition).containsKey(currentWord)) { // if the current word is not recorded in the observations for the next state
                        nextScore += -100; // subtract 100 from the score
                    }
                    else { // if the current word is recorded in the observations for the next state
                        nextScore += observations.get(transition).get(currentWord); // add the observational score of the current word to the score
                    }
                    // if nextScores does not have a score for the current transition or the recorded score is lower than the current score
                    if (!nextScores.containsKey(transition) || nextScores.get(transition) < nextScore)  {
                        nextScores.put(transition, nextScore); // put the transition-score pair into nextScores
                        predecessors.put(transition, currentState); // put the next state and the current state into predecessors
                    }
                }
            }
            backTrack.push(predecessors); // push predecessors onto backtrack stack
            currentStates = nextStates; // update the current states to the next states
            currentScores = nextScores; // update the current scores to the next states
        }
        String bestTransition = null; // create a null string to hold the best transition
        for (String transition : currentScores.keySet()) { // loop over all transitions in the current scores
            // if there is no best transition yet or the score of the best transition is lower than the score of the current transition, reassign the best transition to the current transition
            if (bestTransition == null || currentScores.get(bestTransition) < currentScores.get(transition)) bestTransition = transition;
        }
        int i = path.length-1; // create an index integer of path length - 1
        while (!backTrack.empty()) { // while backtrack stack isn't empty
            path[i] = bestTransition; // assign the ith index in the path array to the best transition
            HashMap<String, String> currentBackTrack = backTrack.pop(); // pop the next HashMap on backtrack
            bestTransition = currentBackTrack.get(bestTransition); // reassign the best transition to the best transition in the current backtrack map
            i --;
        }
        return path;
    }

    /**
     * test the trained HMM by letting it tag the part of speech of unknown sample sentences and comparing the result
     * to the actual tags
     *
     * @throws IOException
     */
    private void testModel () throws IOException {
        BufferedReader sentenceInput = new BufferedReader(new FileReader(testSentenceFile)); // create a reader for the sentences
        BufferedReader tagInput = new BufferedReader(new FileReader(testTagFile)); // create a reader for the tags
        int correctTags = 0; // create a correct tags variable and set it to 0
        int incorrectTags = 0; // create an incorrect tags variable and set it to 0

        while (sentenceInput.ready()) { // loop over all sentences from the input
            String[] sentence = sentenceInput.readLine().toLowerCase().split("\\ "); // create an array of the words in the current sentence
            String[] testTags = tagInput.readLine().split("\\ "); // create an array of the tags of the current tag input
            String[] computedTags = Viterbi(sentence); // create an array of the tag computed by Viterbi implementing the HMM
            for (int i = 0; i < testTags.length; i++) { // loop over all tags
                if (testTags[i].equals(computedTags[i])) correctTags++; // if the computed tag equals the actual tag, add 1 to the correct tags
                else {
                    System.out.println(sentence[i] + " : " + computedTags[i] + " : " + testTags[i]);
                    System.out.println(Arrays.toString(sentence));
                    incorrectTags++; // if the computed tag does not match the actual tag, add 1 to the incorrect tags
                }
            }
        }

        sentenceInput.close();
        tagInput.close();

        int totalTags = correctTags + incorrectTags; // compute the total tags as the sum of the correct and incorrect tags
        // print out number of the total tags, correct tags, and incorrect tags
        System.out.println("Total tags: " + totalTags);
        System.out.println("Correct tags: " + correctTags);
        System.out.println("Incorrect tags: " + incorrectTags);
    }

    // train the model and run the tests
    public static void main(String[] args) throws IOException {
        // POSTagger test = new POSTagger("PS5/example-sentences.txt", "PS5/example-tags.txt", "PS5/example-sentences.txt", "PS5/example-tags.txt");
        // POSTagger test = new POSTagger("PS5/simple-train-sentences.txt", "PS5/simple-train-tags.txt", "PS5/simple-test-sentences.txt", "PS5/simple-test-tags.txt");
         POSTagger test = new POSTagger("PS5/brown-train-sentences.txt", "PS5/brown-train-tags.txt", "PS5/brown-test-sentences.txt", "PS5/brown-test-tags.txt");
         //POSTagger test = new POSTagger("PS5/brown-train-sentences.txt", "PS5/brown-train-tags.txt", "PS5/report-test-sentences.txt", "PS5/report-test-tags.txt");
         test.trainModel();
        System.out.println(test.observations);
        System.out.println(test.transitions);
//        String[] testString = {"will", "eats", "the", "fish"};
//        test.Viterbi(testString);
        test.testModel();
    }
}
