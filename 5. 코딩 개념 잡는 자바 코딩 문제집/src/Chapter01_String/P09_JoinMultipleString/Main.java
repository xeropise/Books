package Chapter01_String.P09_JoinMultipleString;

public class Main {
    private static final String TEXT_1 = "Illinois";
    private static final String TEXT_2 = "Mathematics";
    private static final String TEXT_3 = "and";
    private static final String TEXT_4 = "Science";
    private static final String TEXT_5 = "Academy";

    public static void main(String[] args) {
        System.out.println(Strings.joinMultipleString01(',', TEXT_1,TEXT_2,TEXT_3,TEXT_4,TEXT_5));
        System.out.println(Strings.joinMultipleString02(',', TEXT_1,TEXT_2,TEXT_3,TEXT_4,TEXT_5));
        System.out.println(Strings.joinMultipleString03(',', TEXT_1,TEXT_2,TEXT_3,TEXT_4,TEXT_5));
        System.out.println(Strings.joinMultipleString04(',', TEXT_1,TEXT_2,TEXT_3,TEXT_4,TEXT_5));
    }
}
