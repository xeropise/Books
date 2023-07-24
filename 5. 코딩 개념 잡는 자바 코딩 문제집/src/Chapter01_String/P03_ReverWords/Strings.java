package Chapter01_String.P03_ReverWords;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Strings {
    private static final String WHITE_SPACE = " ";
    private static final Pattern PATTERN = Pattern.compile(" +");

    public static String reverseWords01(String str) {
        String[] words = str.split(WHITE_SPACE);

        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            StringBuilder reverseWord = new StringBuilder();

            for (int i = word.length() - 1; i >= 0; i--) {
                reverseWord.append(word.charAt(i));
            }

            sb.append(reverseWord).append(reverseWord).append(WHITE_SPACE);
        }

        return sb.toString();
    }

    public static String reverseWords02(String str) {
        return PATTERN.splitAsStream(str)
                .map(w -> new StringBuilder(w).reverse())
                .collect(Collectors.joining(" "));
    }
}