package Chapter01_String.P06_CountOccurencesOfAcertainCharacter;

public class Main {
    private static final String TEXT = "My high school, the Illinois Mathematics and Science Academy, "
            + "showed me that anything is possible and that you're never too young to think big. "
            + "At 15, I worked as a computer programmer at the Fermi National Accelerator Laboratory, "
            + "or Fermilab. After graduating, I attended Stanford for a degree in economics and "
            + "computer science.";
    private static final char CHAR_TO_COUNT = 'u';

    public static void main(String[] args) {
        System.out.println(Strings.countOccurencesOfAcertainCharacter01(TEXT, CHAR_TO_COUNT));
        System.out.println(Strings.countOccurencesOfAcertainCharacter02(TEXT, String.valueOf(CHAR_TO_COUNT)));
        System.out.println(Strings.countOccurencesOfAcertainCharacter03(TEXT, CHAR_TO_COUNT));
        System.out.println(Strings.countOccurencesOfAcertainCharacter04(TEXT, CHAR_TO_COUNT));
    }
}
