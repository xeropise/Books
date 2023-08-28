package Chapter01_String.P04_ContainsOnlyDigit;

public class Main {

    private static String onlyDigit = "123456789";

    public static void main(String[] args) {
        System.out.println(Strings.containsOnlyDigits01(onlyDigit));
        System.out.println(Strings.containsOnlyDigits02(onlyDigit));
        System.out.println(Strings.containsOnlyDigits03(onlyDigit));
    }
}
