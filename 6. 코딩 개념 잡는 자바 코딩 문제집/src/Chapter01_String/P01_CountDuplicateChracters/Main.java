package Chapter01_String.P01_CountDuplicateChracters;

import java.io.*;
public class Main {
    private static final String TEXT = "Be strong, be fearless, be beautiful. "
            + "And believe that anything is possible when you have the right "
            + "people there to support you. ";

    public static void main(String[] args) throws IOException {
        System.out.println(Strings.countDuplicateCharacters1(TEXT));
        System.out.println(Strings.countDuplicateCharacters2(TEXT));
        System.out.println(Strings.countDuplicateCharacters3(TEXT));
    }



}