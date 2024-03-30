import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Iterator {
    public static void main(String[] args) throws IOException {
        String filename = args[0];
        for (Map.Entry<String, Integer> entry : new WordFrequencies(filename).countAndSort()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }
}

class CharacterStream implements java.util.Iterator<Character> {
    private BufferedReader reader;
    private int nextChar = -1;

    public CharacterStream(String filename) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filename));
        advance();
    }

    private void advance() {
        try {
            this.nextChar = this.reader.read();
        } catch (IOException e) {
            this.nextChar = -1;
        }
    }

    @Override
    public boolean hasNext() {
        return nextChar != -1;
    }

    @Override
    public Character next() {
        int charValue = nextChar;
        advance();
        return (char)charValue;
    }
}

class WordStream implements java.util.Iterator<String> {
    private CharacterStream characterStream;
    private String nextWord;

    public WordStream(String filename) throws FileNotFoundException {
        this.characterStream = new CharacterStream(filename);
        advance();
    }

    private void advance() {
        StringBuilder wordBuilder = new StringBuilder();
        while (characterStream.hasNext()) {
            char c = characterStream.next();
            if (Character.isLetterOrDigit(c)) {
                wordBuilder.append(Character.toLowerCase(c));
            } else if (wordBuilder.length() > 0) {
                break;
            }
        }
        nextWord = wordBuilder.length() > 0 ? wordBuilder.toString() : null;
    }

    @Override
    public boolean hasNext() {
        return nextWord != null;
    }

    @Override
    public String next() {
        String word = nextWord;
        advance();
        return word;
    }
}

class NonStopWords implements java.util.Iterator<String> {
    private WordStream wordStream;
    private String nextWord;
    private Set<String> stopWords;

    public NonStopWords(String filename) throws IOException {
        this.wordStream = new WordStream(filename);
        this.stopWords = new HashSet<>(Arrays.asList(new Scanner(new File("stop_words.txt")).useDelimiter("\\Z").next().split(",")));
        Collections.addAll(stopWords);
        advance();
    }

    private void advance() {
        while (wordStream.hasNext()) {
            String word = wordStream.next();
            if (!stopWords.contains(word) && word.length() > 1) {
                nextWord = word;
                return;
            }
        }
        nextWord = null;
    }

    @Override
    public boolean hasNext() {
        return nextWord != null;
    }

    @Override
    public String next() {
        String word = nextWord;
        advance();
        return word;
    }
}

class WordFrequencies {
    private NonStopWords nonStopWords;

    public WordFrequencies(String filename) throws IOException {
        this.nonStopWords = new NonStopWords(filename);
    }

    public List<Map.Entry<String, Integer>> countAndSort() {
        Map<String, Integer> frequencies = new HashMap<>();
        while (nonStopWords.hasNext()) {
            String word = nonStopWords.next();
            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
        }
        return frequencies.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(25)
                .collect(Collectors.toList());
    }
}
