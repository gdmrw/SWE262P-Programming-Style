package style09;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import java.io.IOException;

@FunctionalInterface
interface TextProcessor<T, U> {
    void process(T input, TextProcessor<U, ?> nextProcessor) throws IOException;
}


class StoryBookReader implements TextProcessor<String, String> {
    @Override
    public void process(String filePath, TextProcessor<String, ?> nextProcessor) throws IOException {
        assert filePath != null;
        try {
            String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
            if (nextProcessor != null) {
                nextProcessor.process(content, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e; // Rethrow the exception to allow for external handling
        }
    }
}


class StopWordFilter implements TextProcessor<String, ArrayList<String>> {
    @Override
    public void process(String content, TextProcessor<ArrayList<String>, ?> nextProcessor) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader("../stop_words.txt"));
        String stopWords = br.readLine();
        br.close();
        String[] stopWordsArray = stopWords.split(",");
        List<String> stopWordsList = Arrays.asList(stopWordsArray);
        ArrayList<String> wordsList = new ArrayList<>();

        String[] words = (content.toLowerCase().split("[\\W_]+"));

        for (String word : words
        ) {
            if (word.length() >= 2 && !stopWordsList.contains(word)) {
                wordsList.add(word);
            }
        }


        if (nextProcessor != null) {
            try {
                nextProcessor.process(wordsList, null);
            } catch (IOException e) {
                e.printStackTrace(); // Consider a more sophisticated error handling here
            }
        }
    }
}

class WordCounter implements TextProcessor<ArrayList<String>, HashMap<String,Integer>> {

    @Override
    public void process(ArrayList<String> wordList, TextProcessor<HashMap<String, Integer>, ?> nextProcessor) throws IOException {
        HashMap<String, Integer> hm = new HashMap<>();

        for (String word : wordList) {
            if (hm.containsKey(word)) {
                int count = hm.get(word);
                count++;
                hm.put(word, count);
            } else {
                hm.put(word, 1);
            }
        }

        if (nextProcessor != null) {
            try {
                nextProcessor.process(hm, null);
            } catch (IOException e) {
                e.printStackTrace(); // Consider a more sophisticated error handling here
            }
        }

    }
}


class PrintProcessor implements TextProcessor<HashMap<String,Integer>, Void> {
    @Override
    public void process(HashMap<String,Integer> hm, TextProcessor<Void, ?> nextProcessor) {
        hm.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(25)
                .forEach(stringIntegerEntry -> {
                    System.out.println(stringIntegerEntry.getKey() + " - " + stringIntegerEntry.getValue());
                });
        System.out.println("count and output successfully");
    }
}

public class Main {
    public static void main(String[] args) {
        TextProcessor<String, String> reader = new StoryBookReader();
        TextProcessor<String, ArrayList<String>> processor = new StopWordFilter();
        TextProcessor<ArrayList<String>,HashMap<String,Integer>> wordCounter = new WordCounter();
        TextProcessor<HashMap<String,Integer>, Void> SortPrinter = new PrintProcessor();

        try {
            reader.process(args[0], (content, next)
                    -> processor.process(content, (words, next2)
                    -> wordCounter.process(words,(hm,next3)
                    -> SortPrinter.process(hm, null))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
