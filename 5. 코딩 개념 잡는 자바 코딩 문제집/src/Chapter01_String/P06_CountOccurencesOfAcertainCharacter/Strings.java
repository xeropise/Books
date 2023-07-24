package Chapter01_String.P06_CountOccurencesOfAcertainCharacter;

public final class Strings {
    public static int countOccurencesOfAcertainCharacter01(String str, char ch) {
        return str.length() - str.replaceAll(String.valueOf(ch), "").length();
    }

    public static int countOccurencesOfAcertainCharacter02(String str, String ch) {
        if (str == null || ch == null || str.isEmpty() || ch.isEmpty()) {
            // or throw IllegalArgumentException
            return -1;
        }

        if (ch.codePointCount(0, ch.length()) > 1) {
            return -1; // 주어진 문자열에 유니코드 문자가 둘 이상 일 경우
        }

        int result = str.length() - str.replace(ch, "").length();

        // ch.length() 가 2를 반환하면 유니코드 대리 쌍이라는 뜻
        return ch.length() == 2 ? result / 2 : result;
    }

    public static int countOccurencesOfAcertainCharacter03(String str, char ch) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }

        return count;
    }

    public static long countOccurencesOfAcertainCharacter04(String str, char ch) {
        return str.chars()
                .filter(it -> (char) it == ch)
                .count();
    }


}
