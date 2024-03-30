import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;

public class Thirty {

    // Two data spaces (shared spaces)
    static Queue<String> wordSpace = new LinkedList<>();
    static Queue<Map<String, Integer>> freqSpace = new LinkedList<>();

    static Set<String> stopwords = new HashSet<>();

    // Worker function that consumes words from the word space
    // and sends partial results to the frequency space
    static class Worker implements Runnable {
        @Override
        public void run() {
            Map<String, Integer> wordFreqs = new HashMap<>();
            while (true) {
                String word;
                synchronized (wordSpace) {
                    if (wordSpace.isEmpty()) {
                        break;
                    }
                    word = wordSpace.poll();
                }
                if (!stopwords.contains(word)) {
                    wordFreqs.put(word, wordFreqs.getOrDefault(word, 0) + 1);
                }
            }
            synchronized (freqSpace) {
                freqSpace.add(wordFreqs);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Load stopwords
        BufferedReader stopWordsReader = new BufferedReader(new FileReader("stop_words.txt"));
        String stopWordsLine = stopWordsReader.readLine();
        while (stopWordsLine != null) {
            String[] stopWordsArray = stopWordsLine.split(",");
            for (String stopWord : stopWordsArray) {
                stopwords.add(stopWord);
            }
            stopWordsLine = stopWordsReader.readLine();
        }
        stopWordsReader.close();

        // Let's have this thread populate the word space
        BufferedReader fileReader = new BufferedReader(new FileReader(args[0]));
        String line = fileReader.readLine();
        while (line != null) {
            String[] words = line.toLowerCase().split("[^a-z]+");
            for (String word : words) {
                if (word.length() >= 2) {
                    synchronized (wordSpace) {
                        wordSpace.add(word);
                    }
                }
            }
            line = fileReader.readLine();
        }
        fileReader.close();

        // Let's create the workers and launch them at their jobs
        Thread[] workers = new Thread[5];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(new Worker());
            workers[i].start();
        }

        // Let's wait for the workers to finish
        for (Thread worker : workers) {
            worker.join();
        }

        // Let's merge the partial frequency results by consuming
        // frequency data from the frequency space
        Map<String, Integer> wordFreqs = new HashMap<>();
        synchronized (freqSpace) {
            while (!freqSpace.isEmpty()) {
                Map<String, Integer> freqs = freqSpace.poll();
                for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                    String key = entry.getKey();
                    int value = entry.getValue();
                    wordFreqs.put(key, wordFreqs.getOrDefault(key, 0) + value);
                }
            }
        }

        // Sort and print the top 25 word frequencies
        wordFreqs.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(25)
                .forEach(entry -> System.out.println(entry.getKey() + " - " + entry.getValue()));
    }
}
