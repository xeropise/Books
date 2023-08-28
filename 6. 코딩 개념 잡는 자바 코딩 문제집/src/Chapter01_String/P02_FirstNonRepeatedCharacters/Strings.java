package Chapter01_String.P02_FirstNonRepeatedCharacters;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Strings {
    // 문자열 내 모든 문자가 확장 아스키표 (256)에 속한다고 가정
    private static final int EXTENDED_ASCII_CODES = 256;

    // 배열 크기가 char 타입의 최대값, 즉 Character.MAX_VALUE (65535) 를 넘지 않는 한 잘 동작함
    // 유니코드 코드 포인트의 최대값인 1114111을 반환하는 Character.MAX_CODE_POINT의 경우 codePointAt()과 codePoints()에 기반한 다른 구현이 필요함
    public static char firstNonRepeatedCharacters01(String str) {
        // 256 보다 크면 그에 맞게 배열 크기를 늘려야 함
        int[] flags = new int[EXTENDED_ASCII_CODES];

        for (int i = 0; i < flags.length; i++) {
            flags[i] = -1;
        }

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (flags[ch] == -1) {
                flags[ch] = i;
            } else {
                flags[ch] = -2;
            }
        }

        int position = Integer.MAX_VALUE;

        for (int i = 0; i < EXTENDED_ASCII_CODES; i++) {
            if (flags[i] >= 0) {
                position = Math.min(position, flags[i]);
            }
        }

        return position == Integer.MAX_VALUE ? Character.MIN_VALUE : str.charAt(position);
    }

    // LinkedHashMap은 삽입한 순서를 유지하는 map 자료구조
    public static char firstNonRepeatedCharacters02(String str) {
        Map<Character, Integer> chars = new LinkedHashMap<>();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            chars.compute(ch, (k, v) -> (v == null) ? 1 : ++v);
        }

        for (Map.Entry<Character, Integer> entry: chars.entrySet()) {
            if (entry.getValue() == 1) {
                return entry.getKey();
            }
        }

        return Character.MIN_VALUE;
    }

    // 아스키코드, 16비트 유니코드, 유니코드 대리 쌍을 모두 지원하는 함수형 스타일

    public static String firstNonRepeatedCharacters03(String str) {
        Map<Integer, Long> chs = str.codePoints()
                .mapToObj(cp -> cp)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        int cp = chs.entrySet().stream()
                .filter(e -> e.getValue() == 1L)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(Integer.valueOf(Character.MIN_VALUE));

        return String.valueOf(Character.toChars(cp));
    }
}
