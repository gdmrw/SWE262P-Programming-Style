import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Framework {
    public static void main(String[] args) throws IOException {

        String path = "../pride-and-prejudice.txt";
        Set<String> stopWords = new HashSet<>(Arrays.asList(new String(Files.readAllBytes(Paths.get("../stop_words.txt"))).split(",")));

        Properties properties = new Properties();
        try {
            // load properties
            properties.load(new FileInputStream("config.properties"));
            String extractClassName = properties.getProperty("extractClassName");
            String countingClassName = properties.getProperty("countingClassName");
            String extractMethod = properties.getProperty("extractMethod");
            String countingMethod = properties.getProperty("countingMethod");


            // reflection load class
            Class<?> extractClazz = Class.forName(extractClassName);
            Class<?> countingClazz = Class.forName(countingClassName);

            Method acquiredExtractMethod = extractClazz.getDeclaredMethod(extractMethod, String.class, Set.class);
            Method acquiredCountingMethod = countingClazz.getDeclaredMethod(countingMethod, Stream.class);


            Object extractInstance = extractClazz.getDeclaredConstructor().newInstance();
            Object countingInstance = countingClazz.getDeclaredConstructor().newInstance();

            @SuppressWarnings("unchecked")
            Stream<String> extractWordsFlow = (Stream<String>) acquiredExtractMethod.invoke(extractInstance, path, stopWords);
            @SuppressWarnings("unchecked")
            Map<String, Integer> finalWordMap =(Map<String, Integer>) acquiredCountingMethod.invoke(countingInstance, extractWordsFlow);

            finalWordMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(25)
                    .forEach(entry -> System.out.println(entry.getKey() + " - " + entry.getValue()));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}