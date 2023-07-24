package Chapter01_String.P09_JoinMultipleString;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public final class Strings {

    public static String joinMultipleString01(char delimiter, String... args) {
        StringBuilder result = new StringBuilder();

        int i = 0;

        for (i = 0; i < args.length-1; i++) {
            result.append(args[i]).append(delimiter);
        }

        result.append(args[i]);

        return result.toString();
    }

    public static String joinMultipleString02(char delimiter, String... args) {
        StringJoiner joiner = new StringJoiner(String.valueOf(delimiter));

        for (String arg: args) {
            joiner.add(arg);
        }

        return joiner.toString();
    }

    public static String joinMultipleString03(char delimiter, String... args) {
        return Arrays.stream(args, 0, args.length)
                .collect(Collectors.joining(String.valueOf(delimiter)));
    }

    public static String joinMultipleString04(char delimiter, String... args) {
        return String.join(String.valueOf(delimiter), args);
    }
}
