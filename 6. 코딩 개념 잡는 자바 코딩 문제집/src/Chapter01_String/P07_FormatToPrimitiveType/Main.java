package Chapter01_String.P07_FormatToPrimitiveType;

public class Main {

    private static final String TO_INT = "453";
    private static final String TO_LONG = "45235223233";
    private static final String TO_FLOAT = "45.823F";
    private static final String TO_DOUBLE = "13.83423D";

    public static void main(String[] args) {
        int a = Strings.toInt(TO_INT);
        long b = Strings.toLong(TO_LONG);
        float c = Strings.toFloat(TO_FLOAT);
        double d = Strings.toDouble(TO_DOUBLE);
    }
}
