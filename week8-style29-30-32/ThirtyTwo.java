import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class ThirtyTwo {

    static class Pair {
        String word;
        int count;

        public Pair(String word, int count) {
            this.word = word;
            this.count = count;
        }
    }

    static List<String> stopWords = new ArrayList<>();

    static {
        try {
            BufferedReader stopWordsReader = new BufferedReader(new FileReader("stop_words.txt"));
            String line;
            while ((line = stopWordsReader.readLine()) != null) {
                String[] words = line.split(",");
                stopWords.addAll(Arrays.asList(words));
            }
            stopWords.addAll(Arrays.asList("abcdefghijklmnopqrstuvwxyz".split("")));
            stopWordsReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static List<String> scan(String strData) {
        Pattern pattern = Pattern.compile("[\\W_]+");
        String[] words = pattern.matcher(strData.toLowerCase()).replaceAll(" ").split("\\s+");
        return Arrays.asList(words);
    }

    static List<String> removeStopWords(List<String> wordList) {
        List<String> filteredList = new ArrayList<>();
        for (String word : wordList) {
            if (!stopWords.contains(word)) {
                filteredList.add(word);
            }
        }
        return filteredList;
    }

    static List<Pair> splitWords(String dataStr) {
        List<Pair> result = new ArrayList<>();
        List<String> words = removeStopWords(scan(dataStr));
        for (String word : words) {
            result.add(new Pair(word, 1));
        }
        return result;
    }

    static Map<String, List<Pair>> regroup(List<Pair> pairsList) {
        Map<String, List<Pair>> mapping = new HashMap<>();
        for (Pair p : pairsList) {
            String word = p.word;
            if (mapping.containsKey(word)) {
                mapping.get(word).add(p);
            } else {
                List<Pair> newList = new ArrayList<>();
                newList.add(p);
                mapping.put(word, newList);
            }
        }
        return mapping;
    }

    static Pair countWords(Map.Entry<String, List<Pair>> entry) {
        int frequency = 0;
        for (Pair pair : entry.getValue()) {
            frequency += pair.count;
        }
        return new Pair(entry.getKey(), frequency);
    }

    static List<Pair> sort(Map<String, List<Pair>> wordFreq) {
        List<Pair> sortedList = new ArrayList<>();
        for (Map.Entry<String, List<Pair>> entry : wordFreq.entrySet()) {
            Pair pair = countWords(entry);
            sortedList.add(pair);
        }
        sortedList.sort((p1, p2) -> Integer.compare(p2.count, p1.count));
        return sortedList;
    }

    static String readfile(String pathToFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pathToFile));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws IOException {
        List<List<Pair>> splits = new ArrayList<>();
        String fileData = readfile(args[0]);
        String[] lines = fileData.split("\\n");
        int chunkSize = 200;
        for (int i = 0; i < lines.length; i += chunkSize) {
            List<Pair> split = splitWords(String.join("\n", Arrays.copyOfRange(lines, i, Math.min(i + chunkSize, lines.length))));
            splits.add(split);
        }

        List<Pair> mergedPairs = new ArrayList<>();
        for (List<Pair> split : splits) {
            mergedPairs.addAll(split);
        }

        Map<String, List<Pair>> splitsPerWord = regroup(mergedPairs);
        List<Pair> wordFreqs = sort(splitsPerWord);

        for (int i = 0; i < Math.min(25, wordFreqs.size()); i++) {
            Pair pair = wordFreqs.get(i);
            System.out.println(pair.word + " - " + pair.count);
        }
    }
}
