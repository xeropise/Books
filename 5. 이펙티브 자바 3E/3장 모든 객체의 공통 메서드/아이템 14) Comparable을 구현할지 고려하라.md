- Comparable 인터페이스의 compareTo 메서드를 알아보자.

- Object 의 equals 와 같은 비교에서 차이점이 있는데 compareTo 는 단순 동치성 비교에 더해 순서까지 비교할 수 있고 제네릭하다.

- Comparable 을 구현했다는 것은 그 클래스의 인스턴스들에는 자연적인 순서(natural order)가 있음을 뜻한다. 그래서 Comparable 을 구현한 객체들의 배열은 다음처런 손쉽게 정렬할 수 있다.

```java
Arrays.sort(a);
```

- 검색, 극단값 계산, 자동 정렬되는 컬렉션 관리도 역시 쉽게 할 수 있다. 다음 프로그램은 명령줄 인수들을 (중복은 제거하고) 알파벳순으로 출력한다. String이 Comparable 을 구현한 덕분이다.

```java
public class WordList {
    public static void main(String[] args) {
        Set<String> s = new TreeSet<>();
        Collections.addAll(s, args);  // args = [ b, c, a ];
        System.out.println(s);  // [ a, b, c ];
    }
}
```

- Comparable 을 구현하여 수많은 제네릭 알고리즘과 컬렉션의 힘을 누릴 수 있으니 알파벳, 숫자, 연대 같이 순서가 명확한 값 클래스를 작성한다면 반드시 Comparable 인터페이스를 구현하자.

```java
public interface Comprable<T> {
    int compareTo(T t);
}
```

- compareTo 메서드의 일반 규약은 equals 의 규약과 비슷하다.

> 이 객체와 주어진 객체의 순서를 비교한다. 이 객체가 주어진 객체보다 작으면 음의 정수를, 같으면 0을, 크면 양의 정수를 반화한다. 이 객체와 비교할 수 없는 타입의 객체가 주어지면 ClassCastException 을 던진다. <br><br>
> 다음 설명에서 sgn(표현식) 표기는 수학에서 말하는 부호 함수(signum function)를 뜻하며, 표현식의 값이 음수, 0, 양수일 때 -1, 0, 1 을 반환하도록 정의했다.<br><br>
>
> - Comparable을 구현한 클래스는 모든 x,y 에 대해 sgn(x.compareTo(y)) == -sgn(y.compareTo(x)) 여야 한다. x.compareTo(y) 는 y.compareTo(x) 가 예외를 던질 때에 한해 예외를 던져야 한다.<br><br>
> - Comparable을 구현한 클래스는 추이성을 보장해야 한다. 즉, (x.compareTo(y) > 0 && y.compareTo(z) > 0) 이면 x.compareTo(z) > 0 이다.<br><br>
> - Comparable 을 구현한 클래스는 모든 z에 대해 x.compareTo(y) == 0 이면 sgn(x.compareTo(z)) == sgn(y.compareTo(z)) 다.<br><br>
> - 이번 권고가 필수는 아니지만 꼭 지키는게 좋다. (x.compareTo(y) == 0) == (x.equals(y)) 여야 한다. Comparable 을 구현하고 이 권고를 지키지 않는 모든 클래스는 그 사실을 명시해야 한다. 다음과 같이 명시하면 적당하다.<br><br>
>   "주의: 이 클래스의 순서는 equals 메서드와 일관되지 않다."

- 모든 객체에 대해 전역 동치관계를 부여하는 equals 메서드와 달리, **compareTo 는 타입이 다른 객체를 신경 쓰지 않아도 된다.** 타입이 다른 객체가 주어지면 간단히 ClassCastException 을 던져도 되며, 대부분 그렇게 한다.

- hashCode 규약을 지키 못하면 해시를 사용하는 클래스와 어울리지 못하듯, compareTo 규약을 지키지 못하면 비교를 활용하는 클래스와 어울리지 못한다.

- 비교를 활용하는 클래스의 예로는 정렬된 컬렉션인 TreeSet 과 TreeMap, 검색과 정렬 알고리즘을 활용하는 유틸리티 클래스인 Collections 와 Arrays 가 있다.

- equals 와 비슷하게 동치성 검사도 반사성, 대칭성, 추이성을 충족해야 하며 주의사항도 똑같다. 기존 클래스를 확장한 구체 클래스에서 새로운 값 컴포넌트를 추가했다면 compareTo 규약을 지킬 방법이 없다. 객체 지향적 추상화의 이점을 포기할 생각이 아니라면 말이다.

- 우회법도 비슷한데 Comparable 을 구현한 클래스를 확장해 값 컴포넌트를 추가하고 싶다면, 확장하는 대신 독립된 클래스를 만들고, 이 클래스에 원래 클래스의 인스턴스를 가리키는 필드를 두자. 그런 다음 내부 인스턴스를 반환하는 '뷰' 메서드를 제공하면 된다. (컴포지션)

- compareTo 의 마지막 규약은 필수는 아니지만 꼭 지키길 권한다. 이를 잘 지키면 compareTo 로 줄지은 순서와 equals 의 결과가 일관되게 된다. compareTo 의 순서와 equals 의 결과가 일관되지 않은느 클래스도 여전히 동작은 하는데 단, 이 클래스의 객체를 정렬된 컬렉션에 넣으면 해당 컬렉션이 구현한 인터페이스(Collection, Set 혹은 Map) 에 정의된 동작과 엇박자를 낼 것이다. **이 인터페이스들은 equals 메서드의 규약을 따른다고 되어 있지만, 정렬된 컬렉션들은 동치성을 비교할 때 equals 대신 compareTo 를 사용하기 때문이다.** 큰 문제는 아니지만, 주의해야 한다.

- 예로 compareTo 와 equals 가 일관되지 않는 BigDecimal 클래스를 예로 생각해보자. 빈 HashSet 인스턴스를 생성한 다음 new BigDecimal("1.0")과 new BigDecimal("1.00") 을 차례로 추가해 보자.

```java
		// HashSet은 equals 로 비교한다.
		Set<BigDecimal> hashSetTest = new HashSet<>();

		hashSetTest.add(new BigDecimal("1.0"));
		hashSetTest.add(new BigDecimal("1.00"));

		System.out.println(hashSetTest.size()); // 2

		// TreeeSet은 compareTo 로 비교한다.
		Set<BigDecimal> treeSetTest = new TreeSet<>();

		treeSetTest.add(new BigDecimal("1.0"));
		treeSetTest.add(new BigDecimal("1.00"));

		System.out.println(treeSetTest.size()); // 1
```

- HashSet은 equals 로 메서드를 비교하고, TreeSet 은 compareTo 메서드로 비교하기 때문에 size 의 결과가 다르다.

- compareTo 메서드 작성 요령은 equals와 비슷한데 몇 가지 차이점만 주의하면 된다. Comparable 은 타입을 인수로 받는 제네릭 인터페이스이므로 compareTo 메서드의 인수 타입은 컴파일 타입에 정해진다. 입력 인수의 타입을 확인하거나 형변환할 필요가 없다는 뜻이다. 인수의 타입이 잘못됐다면 컴파일 자체가 되지 않는다. 또한 null 을 인수로 넣어 호출하면 NullPointerException 을 던져야 한다. 물론 실제로도 인수 (이 경우 null)의 멤버에 접근하려는 순간 이 예외가 던져질 것이다.

- compareTo 메서드는 각 필드가 동치인지를 비교하는게 아니라 그 순서를 비교한다. 객체 참조 필드를 비교하려면 compareTo 메서드를 재귀적으로 호출한다. Comparable 을 구현하지 않은 필드나 표준이 아닌 순서로 비교해야 한다면 비교자(Comparator)를 대신 사용한다. 비교자는 직접 만들거나 자바가 제공하는 것 중에 골라 쓰면 된다.

> String 의 String.CASE_INSENSITIVE_ORDER 는 Compartor 객체를 반환한다.

```java
public final class CaseInsensitiveString implements Comparable<CaseInsensitiveString> {
    public int compareTo(CaseInsensitiveString cis) {
        return String.CASE_INSENSITIVE_ORDER.compare(s, cis.s);
    }
}
```

- **compareTo 메서드에서 관게 연산자 < 와 > 를 사용하는 이전 방식은 거추장스럽고 오류를 유발하니, 추천하지 않는다. 박싱된 기본 타입 클래스들에 새로 추가된 정적 메서드인 compare 를 이용하여 기본 타입 필드를 비교하도록 하자.**

- 클래스에 핵심 필드가 여러 개라면 어느 것을 먼저 비교하느냐가 중요해지므로 가장 핵심적인 필드부터 비교해나가자. 비교 결과가 0이 아니라면, 즉 순서가 결정되면 거기서 끝이므로 그 결과를 곧장 반환하자. 가장 핵심이 되는 필드가 똑같다면, 똑같지 않은 필드를 찾을 때까지 그 다음으로 중요한 필드를 비교해나간다.

```java
public int compareTo(PhoneNumber pn) {
    int result = Short.compare(areaCode, pn.areaCode); // 가장 중요한 필드
    if (result == 0) {
        result = Short.compare(prefix, pn.prefix);      // 두 번째로 중요한 필드
        if (result == 0) {
            result = Short.compare(lineNum, pn.lineNum); // 세 번째로 중요한 필드
        }
    }
    return result;
}
```

- 자바 8에서는 Comparator 인터페이스가 일련의 비교자 생성 메서드 (compartor construction method)와 팀을 꾸려 메서드 연쇄 방식으로 비교자를 생성할 수 있게 되었다. 간결하지만, 약간의 성능 저하가 있다.

- 자바의 정적 임포트 기능을 이용하면 정적 비교자 생성 메서드들을 그 이름만으로 사용할 수 있어 코드가 훨씬 깔끔해진다.

```java
private static final Comparator<PhoneNumber> COMPARATOR =
        comparingInt((PhoneNumber pn) -> pn.areaCode)
            .thenComparingInt(pn -> pn.prefix)
            .thenComparingInt(pn -> pn.lineNum);

public int compareTo(PhoneNumber pn) {
    return Comparator.compare(this, pn);
}
```

- 이 코드는 클래스를 초기화할 때 비교자 생성 메서드 2개를 이용해 비교자를 생성한다. 그 첫 번째인 comparingInt 는 객체 참조를 int 타입 키에 매핑하는 키 추출 함수(key extractor function)를 인수로 받아, 그 키를 기준으로 순서를 정하는 비교자를 반환하는 정적 메서드이다.

- 앞의 예에서 comparingInt 는 람다를 인수로 받으며, 이 람다는 PhoneNumber에서 추출한 지역 코드를 기준으로 전화번호의 순서를 정하는 Comparator<PhoneNumber>를 반환한다. 이 람다에서 입력 인수의 타입을 명시한 점에 주목하자. 자바의 타입 추론 능력이 이 상황에서 타입을 알아낼 만큼 강력하지 않기 때문에 프로그램이 컴파일되도록 도와 준 것이다.

- 두 전화번호의 지역 코드가 같을 수 있으니, 비교 방식을 더 다듬어야 한다. 이 일은 두 번쨰 비교자 생성 메서드인 thenComparingInt 가 수행한다. thenCOmparingInt 는 Comparator의 인스턴스 메서드로, int 키 추출자 함수를 입력 받아 다시 비교자를 반환한다. (추가 비교 수행) 연달아 호출하여 여러 키를 비교할 수 있다.

- Compartor 는 수많은 보조 생성 메서드들로 중무장하고 있는데 long 과 double 용으로는 comparingInt 와 thenComparingInt 의 변형 메서드들이 있다. short 처럼 더 작은 정수 타입에는 int 용 버전을 사용하면 된다. 마찬가지로 float은 double 용을 이용해 수행한다. 이런 식으로 자바의 숫자용 기본 타입을 모두 커버한다.

- 객체 참조용 비교자 생성 메서드도 준비되어 있다. 우선 [comparing](https://docs.oracle.com/javase/8/docs/api/java/util/Comparator.html) 이라는 정적 메서드 2개가 다중정의되어 있다. 첫 번째는 키 추출자를 받아서 그 키의 자연적 순서를 사용한다. 두 번째는 키 추출자 하나의 추출된 키를 비교할 비교자까지 총 2개의 인수를 받는다. 또한, thenComparing 이란 인스턴스 메서드가 3개 다중정의되어 있다.

- '값의 차'를 기준으로 첫 번째 값이 두 번째 값보다 작으면 음수를, 두 값이 같으면 0을, 첫 번째 값이 크면 양수를 반환하는 compareTo 나 compare 메서드와 마주할 것이다.

```java
static Comparator<Object> hashCodeOrder = new Compartor<>() {
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode();
    }
}
```

- 이 방식은 사용해서는 안된다. 정수 오버플로우를 일으키거나 부동소수점 계산 방식에 따른 오류르 낼 수 있다. 그렇다고 속도도 월등히 빠르지도 않을 것이다. 다음의 방법을 사용하자.

```java
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }
}
```

```java
static Comparator<object> hashCodeOrder =
            Comparator.comparingInt(o -> o.hashCode());
```

### 핵심 정리

- 순서를 고려해야 하는 값 클래스를 작성한다면 Comparable 인터페이스를 구현하여, 그 인스턴스를 쉽게 정렬하고 , 검색하교, 비교 기능을 제공하는 컬렉션과 어우러지도록 해야 한다.

- compareTo 메서드에서 필드의 값을 비교할 떄 < 와 > 연산자는 쓰지 말아야한다. 그 대신 박싱된 기본 타입 클래스가 제공하는 정적 compare 메서드난 Compartor 인터페이스가 제공하는 비교자 생성 메서드를 사용하자.
