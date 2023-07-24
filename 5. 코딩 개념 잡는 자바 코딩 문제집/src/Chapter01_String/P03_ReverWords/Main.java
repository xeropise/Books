package Chapter01_String.P03_ReverWords;

public class Main {
    private static final String TEXT = "My high school, the Illinois Mathematics and Science Academy, "
            + "showed me that anything is possible and that you're never too young to think big. "
            + "At 15, I worked as a computer programmer at the Fermi National Accelerator Laboratory, "
            + "or Fermilab. After graduating, I attended Stanford for a degree in economics and "
            + "computer science.";

    public static void main(String[] args) {
        System.out.println(Strings.reverseWords01(TEXT));
        System.out.println(Strings.reverseWords02(TEXT));
    }

}
