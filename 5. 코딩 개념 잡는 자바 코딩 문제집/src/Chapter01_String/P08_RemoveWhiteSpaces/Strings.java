package Chapter01_String.P08_RemoveWhiteSpaces;

public class Strings {
    /*
        \s (공백문자 표현) 는 \t, \n, \r 처럼 보이지 않는 엽개을 포함해 모든 여백을 제거 가능하다.

     */
    public static String removeWhiteSpaces(String str) {
        return str.replaceAll("\\s", "");
    }

}
