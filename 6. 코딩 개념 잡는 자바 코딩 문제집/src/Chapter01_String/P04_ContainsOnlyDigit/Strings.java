package Chapter01_String.P04_ContainsOnlyDigit;

public final class Strings {
    public static boolean containsOnlyDigits01(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    // 근데 이건 allMatch하면 되지 않나?
    public static boolean containsOnlyDigits02(String str) {
        return !str.chars()
        .anyMatch(c -> !Character.isDigit(c));

        /*
        str.chars()
                .allMatch(c -> Character.isDigit(c));
        */
    }

    public static boolean containsOnlyDigits03(String str) {
        return str.matches("[0-9]+");
    }

}
