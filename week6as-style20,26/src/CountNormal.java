import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CountNormal implements CountingInterface{
    @Override
    public Map<String, Integer> count(Stream<String> extractWordsFlow) {
        return extractWordsFlow.collect(Collectors.toMap(word -> word, word -> 1, Integer::sum));
    }
}
