package Chapter01_String.P10_permuteAndPrint;

import java.util.Set;
import java.util.stream.Stream;

public class Main {
    private static final String TEXT = "ABC";

    public static void main(String[] args) {

        Set<String> collector = Strings.permuteAndStore(TEXT);
        System.out.println(collector);

        Strings.permuteAndPrint(TEXT);

        Stream<String> result = Strings.permuteAndReturnStream(TEXT);
        result.forEach(System.out::println);

        Strings.permuteAndPrintStream(TEXT);
    }
}
