package Chapter01_String.P05_allVowels;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Strings {

    public static Set<Character> allVowels = new HashSet(Arrays.asList('a', 'e', 'i', 'o', 'u'));

    public static Pair<Integer, Integer> countVowelsAndConsonants01(String str) {
        str = str.toLowerCase();

        int vowels = 0;
        int consonants = 0;

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (allVowels.contains(ch)) {
                vowels++;
            } else if ((ch >= 'a' && ch <= 'z')) {
                consonants++;
            }
        }

        return Pair.of(vowels, consonants);
    }

    public static Pair<Long, Long> countVowelsAndConsonants02(String str) {
        long vowels = str.chars()
                .filter(it -> allVowels.contains(it))
                .count();

        long consonants = str.chars()
                .filter(c -> !allVowels.contains((char) c))
                .filter(ch -> (ch >= 'a' && ch <= 'z'))
                .count();

        return Pair.of(vowels, consonants);
    }

    public static Pair<Long, Long> countVowelsAndConsonants03(String str) {
        Map<Boolean, Long> result = str.chars()
                .mapToObj(c -> (char) c)
                .filter(ch -> (ch >= 'a' && ch <= 'z'))
                .collect(Collectors.partitioningBy(c -> allVowels.contains(c), Collectors.counting()));

        return Pair.of(result.get(true), result.get(false));
    }
}
