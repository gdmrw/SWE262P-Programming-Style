import java.util.Map;
import java.util.stream.Stream;

public interface CountingInterface {
    public Map<String, Integer> count(Stream<String> extractWordsFlow);
}
