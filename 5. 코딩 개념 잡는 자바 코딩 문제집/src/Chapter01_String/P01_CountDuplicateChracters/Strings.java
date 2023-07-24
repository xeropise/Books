package Chapter01_String.P01_CountDuplicateChracters;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class Strings {
    public static Map<Character, Integer> countDuplicateCharacters1(String str) {
        Map<Character, Integer> result = new HashMap<>();

        for(char ch: str.toCharArray()) {
            result.compute(ch, (k,v) -> (v == null) ? 1 : ++v);
        }

        return result;
    }

    public static Map<Character, Long> countDuplicateCharacters2(String str) {
        Map<Character, Long> result = str.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        return result;
    }

    public static Map<String, Long> countDuplicateCharacters3(String str) {
        Map<String, Long> result = str.codePoints()
                .mapToObj(c -> String.valueOf(Character.toChars(c)))
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        return result;
    }
}
