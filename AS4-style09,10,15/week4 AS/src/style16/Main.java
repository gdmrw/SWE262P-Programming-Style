package style16;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


class Loader extends Observable {

    public void setLoad(String filePath) {
        setChanged(); // 标记状态已改变
        notifyObservers(filePath); // 通知所有观察者
    }
}

class Starter extends Observable {

    public void setStart() {
        setChanged(); // 标记状态已改变
        notifyObservers(); // 通知所有观察者
    }
}

class Word extends Observable {

    public void setWord(String word) {
        setChanged(); // 标记状态已改变
        notifyObservers(word); // 通知所有观察者
    }
}

class ValidWord extends Observable {

    public void setValidWord(String validWord) {
        setChanged(); // 标记状态已改变
        notifyObservers(validWord); // 通知所有观察者
    }
}


class Runner extends Observable {

    public void setRun(String filepath) {
        setChanged(); // 标记状态已改变
        notifyObservers(filepath); // 通知所有观察者
    }
}

class EOF extends Observable {

    public void setEOF() {
        setChanged(); // 标记状态已改变
        notifyObservers(); // 通知所有观察者
    }
}

class Printer extends Observable {
    public void setPrint() {
        setChanged(); // 标记状态已改变
        notifyObservers(); // 通知所有观察者
    }
}





class DataStorage implements Observer {
    private String content = null;
    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof Loader) {
            String filePath =(String) arg;
            try {
                content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else if (o instanceof Starter && content != null) {
            String[] words = (content.toLowerCase().split("[\\W_]+"));
            for (String w : words) {
                Main.word.setWord(w);
            }
            Main.eof.setEOF();
        }
    }
}

class StopWordsFilter implements Observer {
    private List<String> stopWordsList;
    private ArrayList<String> wordsList = new ArrayList<>();


    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Loader) {

            try {
                BufferedReader br = new BufferedReader(new FileReader("stop_words.txt"));
                String stopWords = br.readLine();
                br.close();
                String[] stopWordsArray = stopWords.split(",");
                stopWordsList = Arrays.asList(stopWordsArray);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else if (o instanceof Word) {
            String word = (String) arg;
            if (word.length() >= 2 && !stopWordsList.contains(word)) {
                Main.validWord.setValidWord(word);
            }

        }
    }
}

class WordFrequencyCounter implements Observer {
    private HashMap<String, Integer> hm = new HashMap<>();
    private  Integer zWordCounter = 0;

    @Override
    public void update(Observable o, Object arg) {
        String word = (String) arg;
        if (o instanceof ValidWord) {
            if (word.contains("z")){
                zWordCounter++;
            }
            if (hm.containsKey(word)) {
                int count = hm.get(word);
                count++;
                hm.put(word, count);
            } else {
                hm.put(word, 1);
            }
        }else if (o instanceof Printer) {
            hm.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(25)
                    .forEach(stringIntegerEntry -> {
                        System.out.println(stringIntegerEntry.getKey() + " - " + stringIntegerEntry.getValue());
                    });
            System.out.println("words contain Z:" + zWordCounter);
            System.out.println("count and output successfully");
        }
    }
}

class WordFrequencyApplication implements Observer {

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Runner) {
            Main.loader.setLoad((String) arg);
            Main.starter.setStart();


        } else if (o instanceof EOF) {
            Main.printer.setPrint();

        }
    }
}


// 主类
public class Main {
    static Loader loader = new Loader();
    static Starter starter = new Starter();
    static Word word = new Word();
    static ValidWord validWord = new ValidWord();
    static Runner runner = new Runner();
    static EOF eof = new EOF();
    static Printer printer = new Printer();


    public static void main(String[] args) {

        DataStorage dataStorage =  new DataStorage();
        loader.addObserver(dataStorage);
        starter.addObserver(dataStorage);

        StopWordsFilter stopWordsFilter = new StopWordsFilter();
        loader.addObserver(stopWordsFilter);
        word.addObserver(stopWordsFilter);

        WordFrequencyCounter wordFrequencyCounter = new WordFrequencyCounter();
        validWord.addObserver(wordFrequencyCounter);
        printer.addObserver(wordFrequencyCounter);

        WordFrequencyApplication wordFrequencyApplication = new WordFrequencyApplication();
        runner.addObserver(wordFrequencyApplication);
        eof.addObserver(wordFrequencyApplication);

        runner.setRun(args[0]);


    }
}
