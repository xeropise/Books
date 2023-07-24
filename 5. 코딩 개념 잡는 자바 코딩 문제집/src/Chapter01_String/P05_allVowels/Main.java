package Chapter01_String.P05_allVowels;

public class Main {
    // 14 vowels, 19 consonants
    private static final String TEXT = " ... Illinois Mathematics & Science Academy ... ";

    public static void main(String[] args) {
        System.out.println(Strings.countVowelsAndConsonants01(TEXT));
        System.out.println(Strings.countVowelsAndConsonants02(TEXT));
        System.out.println(Strings.countVowelsAndConsonants03(TEXT));

    }
}
