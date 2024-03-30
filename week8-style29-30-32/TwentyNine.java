import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;



public class TwentyNine{

    // Method to send a message to an ActiveWFObject
    public static void send(ActiveWFObject r, Object[] messageObject) {
        // use blocking queue => wait for the queue to become non-empty when retrieving and removing an element, and wait for space to become available in the queue when adding an element
      
        r._queueMessage(messageObject); // send object[] to _queueMessage() 
    }

    // Abstract class representing an active object with message handling functionality
    static abstract class ActiveWFObject extends Thread {
        boolean should_stop = false;

        // Queue for storing messages
        BlockingQueue<Object[]> blockingQueue = new LinkedBlockingQueue<>(); 

        @Override
        public void run() {
            while (!should_stop) {
                Object[] message = blockingQueue.poll(); // Get object[] from queue

                if (message != null) {
                    _dispatch(message);
                    if (message[0].equals("done")) {
                        should_stop = true;
                    }
                }
            }
        }

        // Method to add a message to the queue
        public void _queueMessage(Object[] msgToQueue) {
            blockingQueue.add(msgToQueue);
        }

        // Method to end the processing
        public void end() {
            should_stop = true;
        }

        // Abstract method to dispatch messages
        abstract void _dispatch(Object[] messageObject);
    }


    // Start of WordFrequencyManager
    static class WordFrequencyManager extends ActiveWFObject {
        HashMap<String, Integer> wordcheck = new HashMap<>();
        List<Map.Entry<String, Integer>> listwords;

        // Method to handle different types of messages
        public void _dispatch(Object[] messageObject){
            if(messageObject[0].equals("word")){
                _increment_count_words(new Object[]{messageObject[1]});
            }
            else if(messageObject[0].equals("top25")){
                _top25(new Object[]{messageObject[1]});
            }

        }

        // Method to increment the count of words
        public void _increment_count_words(Object[] msg){
            String word = (String) msg[0];
            if(wordcheck.containsKey(word)){
                wordcheck.put(word, wordcheck.get(word) + 1);
            }
            else{
                wordcheck.put(word, 1);
            }
        }

        // Method to get the top 25 words by frequency
        public void _top25(Object[] message){
            List<Map.Entry<String, Integer>> sortedMap = sort_frequency();
            WordFrequencyController recipient = (WordFrequencyController) message[0];
            send(recipient, new Object[]{"top25",sortedMap});
        }

        // Method to sort words by frequency
        public List<Map.Entry<String, Integer>> sort_frequency(){
            Set<Map.Entry<String, Integer>> set = wordcheck.entrySet(); // Get key and values from hash map
            listwords = new ArrayList<Map.Entry<String, Integer>>(set); // List of key and values from hash map
            Collections.sort(listwords, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> o1,
                        Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue()); // Sort list from greatest to least frequency
                }
            });
            return listwords;
        }
    }

    // Start of StopWordsManager
    static class StopWordsManager extends ActiveWFObject {
        ArrayList<String> stoplist;
        WordFrequencyManager word_freqs_manager;

        // Method to initialize the stop words manager
        public void _init(Object[] message){
            word_freqs_manager = (WordFrequencyManager) message[0];
            stoplist = new ArrayList<String>();

            // Load and add stop words 
            try{
                File file = new File("stop_words.txt"); 
                Scanner stop = new Scanner(file); 

                while (stop.hasNext()) {
                    String[] stopline = stop.nextLine().toString().split(",");
                    for (int i = 0; i < stopline.length; i++) {
                        stoplist.add(stopline[i]);
                    }
                }
                stop.close();
            }
            catch(FileNotFoundException e){
                System.out.println(e);
            }
        }

        // Method to handle different types of messages
        public void _dispatch(Object[] messageObj){
            if(messageObj[0].equals("init")){
                _init(new Object[]{messageObj[1]});
            }
            else if(messageObj[0].equals("filter")){
                _filter(new Object[]{messageObj[1]});
            }
            else{
                send(word_freqs_manager, messageObj);
            }
        }

        // Method to filter out stop words
        public void _filter(Object[] message){
            String word = (String) message[0];
            if(!stoplist.contains(word) && word.length() >= 2){ // Found a word to send 
                send(word_freqs_manager, new Object[]{"word", word});
            }
        }
    }

  // Start of DataStorageManager
  static class DataStorageManager extends ActiveWFObject {
      String fileName; 
      ArrayList<String> lineOfWords = new ArrayList<>();
      StopWordsManager swManager;

      // Method to handle different types of messages
      public void _dispatch(Object[] messageObject){

          if(messageObject[0].equals("init")){
              _init(new Object[]{messageObject[1], messageObject[2]});
          }
          else if(messageObject[0].equals("frequency")){
              _process_words(new Object[]{messageObject[1]});
          }
          else{
              send(swManager, messageObject);
          }
      }

      // Method to initialize the data storage manager
      public void _init(Object[] message){
          fileName = (String) message[0];
          swManager = (StopWordsManager) message[1];

          try{
              File file = new File(fileName);
              Scanner reader = new Scanner(file);

              while (reader.hasNext()) {
                  String[] list_of_words = reader.nextLine().split("[^a-zA-Z0-9]+"); // Regex to split words
                  for (String word : list_of_words) {
                      lineOfWords.add(word.toLowerCase());
                  }
              }
              reader.close();
          }
          catch(IOException e){ 
              System.out.println(e); 
          }
      }

      // Method to process words for frequency analysis
      public void _process_words(Object[] message){
          WordFrequencyController wfController = (WordFrequencyController) message[0];
          for(String word: lineOfWords){
              send(swManager, new Object[]{"filter", word}); // Tell stop words to start filtering
          }
          send(swManager, new Object[]{"top25", wfController});
      }
  }

   // Start of WordFrequencyController
    static class WordFrequencyController extends ActiveWFObject{
        DataStorageManager swManager;

        // Method to handle different types of messages
        public void _dispatch(Object[] messageObject){
            if(messageObject[0].equals("run")){
                run(new Object[]{messageObject[1]});
            }
            else if(messageObject[0].equals("top25")){
                _display(new Object[]{messageObject[1]});
            }
        }

        // Method to initiate the word frequency analysis process
        public void run(Object[] message){
            swManager = (DataStorageManager) message[0];
            send(swManager, new Object[]{"frequency", this});
        }

        // Method to display the top 25 most frequent words
        public void _display(Object[] msg){

            // Cast the sorted list from the object[]
            List<Map.Entry<String, Integer>> listwords = (List<Map.Entry<String, Integer>>) msg[0];

            int count = 0;
            for (Map.Entry<String, Integer> val : listwords) {
                if (count < 25) { // Get the top 25 words
                    System.out.println(val.getKey() + " - " + val.getValue());
                    count++;
                }
            }
            System.out.println("");

            // Finished with execution process
            end();
            send(swManager, new Object[]{"done"});
        }
    }


    // Main Method
    public static void main(String[] args){

        WordFrequencyManager wfManager = new WordFrequencyManager();

        StopWordsManager swManager = new StopWordsManager();
        send(swManager, new Object[]{"init", wfManager}); // Send an object array with "init" and wfManager

        DataStorageManager storageManager = new DataStorageManager();
        send(storageManager, new Object[]{"init", args[0], swManager}); // Send an object array with "init" and swManager

        WordFrequencyController wfController = new WordFrequencyController();
        send(wfController, new Object[]{"run", storageManager}); // Send an object array with "run" and storageManager

        // Start each manager as a thread individually
        try{
            wfManager.start();
            swManager.start();
            storageManager.start();
            wfController.start();

        }catch(IllegalThreadStateException e){
            System.out.println(e);
        }  
    }
}