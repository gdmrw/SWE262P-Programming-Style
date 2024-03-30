import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

public interface ExtractInterface {
    public Stream<String> extract(String path, Set<String> stopWords);
}
