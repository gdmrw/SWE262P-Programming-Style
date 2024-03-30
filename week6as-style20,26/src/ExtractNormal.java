import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public class ExtractNormal implements ExtractInterface{
    @Override
    public Stream<String> extract(String path, Set<String> stopWords){
        try {
            return Files.lines(Paths.get(path))
                    .map(line -> line.toLowerCase().split("[\\W_]+"))
                    .flatMap(Arrays::stream)
                    .filter(word -> word.length() >= 2 && !stopWords.contains(word));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
