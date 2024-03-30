package style10;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class FunctionChain<T, R> {
    private Function<T, R> chain;

    public FunctionChain(Function<T, R> initialFunction) {
        this.chain = initialFunction;
    }

    public <V> FunctionChain<T, V> bind(Function<R, V> nextFunction) {
        Function<T, V> newChain = chain.andThen(nextFunction);
        return new FunctionChain<>(newChain);
    }

    public R apply(T value) {
        return chain.apply(value);
    }
}

class Main {
    public static void main(String[] args) throws IOException {

        FunctionChain<String, ArrayList<String>> chain = new FunctionChain<>(Main::storyBookReader)
                .bind(Main::stopWordFilter)
                .bind(Main::wordFrequancyCounter)
                .bind(Main::wordSorter);


        ArrayList<String> ret = chain.apply(args[0]);
        for (String content : ret) {
            System.out.println(content);
        }
        System.out.println("Output Successfully");
    }

    public static String storyBookReader(String filePath) {
        assert filePath != null;
        try {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


        public static ArrayList<String> stopWordFilter (String content) {
            List<String> stopWordsList = null;
            ArrayList<String> wordsList = null;
            try {
                BufferedReader br = new BufferedReader(new FileReader("../stop_words.txt"));
                String stopWords = br.readLine();
                br.close();
                String[] stopWordsArray = stopWords.split(",");
                stopWordsList = Arrays.asList(stopWordsArray);
                wordsList = new ArrayList<>();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String[] words = (content.toLowerCase().split("[\\W_]+"));

            for (String word : words
            ) {
                if (word.length() >= 2 && !stopWordsList.contains(word)) {
                    wordsList.add(word);
                }
            }
            return wordsList;
    }


    public static HashMap<String, Integer> wordFrequancyCounter (ArrayList<String> wordList) {

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
        return hm;
    }

    public static ArrayList<String> wordSorter (HashMap<String, Integer> hm) {
        ArrayList<String> result = new ArrayList<>();
        hm.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(25)
                .forEach(stringIntegerEntry -> {
                    result.add(stringIntegerEntry.getKey() + " - " + stringIntegerEntry.getValue());
                });
        return result;
    }

}
