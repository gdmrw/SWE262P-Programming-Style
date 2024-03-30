import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CountWordsBasedOnFirstLetter implements CountingInterface {

    @Override
    public Map<String, Integer> count(Stream<String> extractWordsFlow) {
        return extractWordsFlow
                .filter(word -> word.length() > 0 && Character.isLetter(word.charAt(0)))
                .collect(Collectors.toMap(
                        word -> "words start with:" + word.charAt(0),
                        word -> 1,
                        Integer::sum));
    }
}
