## 3장 모든 객체의 공통 메서드

- Object는 객체를 만들 수 있는 클래스지만 기본적으로 상속해서 사용하도록 설계됨
- final이 아닌 메서드(equals, hashCode, toString, clone, finalize)는 모두 재정의(overrindg)를 염두해 두고 설계된 것이라 재정의 시 지켜야 하는 일반 규약이 명확히 정의되어 있다.

- Object 를 상속하느 클래스, 즉 모든 클래스는 이 메서드들을 일반 규약에 맞게 재정의해야 한다.

- 메서드를 잘못 구현하면 대상 클래스가 이 규약을 준수한다고 가정하는 클래스 (HasgMap, HashSet)을 오동작하게 만들 수 있다.

- final이 아닌 Object 메서드들을 언제 어떻게 재정의해야 하는지를 다루며 Comparable.compareTo 의 경우 Object 메서드는 아니지만 성격이 비슷하니 함께 다뤄보겠다.

### 아이템 10 - equals는 일반 규약을 지켜 재정의하라

- equals 메서드는 재정의하기 쉬워 보이지만 매우 위험하다. 잘모르겠으면 재정의하지 않는 것이 낫다.

- 다음에서 열거한 상황 중 하나에 해당한다면 재정의하지 않는 것이 최선이다.

  - **각 인스턴스가 본질적으로 고유할 때** 값을 표현하는게 아니라 동작하는 개체를 표현하는 클래스 일 경우, Thread

  - **인스턴스의 '논리적 동치성(logical equality)'을 검사할 일이 없는 경우.** java.util.regex.Pattern은 equals를 재정의해서 두 Pattern 의 인스턴스가 같은 정규표현식을 나타내는지를 검사하는 경우, 말하자면 논리적 동치성을 검사하는 방법도 있다. 클라이언트가 이 방식은 원하지 않거나 애초에 필요하지 않다고 판단할 수도 있다. 설계자가 후자로 판단했다면 Object의 기본 equals만으로 해결된다.

  - **상위 클래스에서 재정의한 equals 가 하위 클래스에도 딱 들어맞는 경우** Set 구현체가 AbstractSet이 구현한 equals를 상속받아 쓰고, List 구현체들은 AbstractList로부터, Map 구현체들은 AbstractMap으로부터 상속받아 그대로 쓴다.

  - **클래스가 private 이거나 package-private 이고 equals 메서드를 호출할 일이 없는 경우**, equals가 실수라도 호출되는 것을 막고 싶다면 다음 처럼 구현해 둘 수 있다.

  ```java
  @Override
  public boolean equals(Object o) {
      throw new AssertionError(); // 호출 금지!
  }
  ```

- **그렇다면 equals를 재정의는 언제 해야할까?** 객체 식별성(object identity, 두 객체가 물리적으로 같은가)이 아니라 논리적 동치성을 확인해야 하는데, 상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의되지 않았을 떄이다.
- 값 클래스 (Integer,String) 클래스가 보통 여기에 해당하는데, 두 값 객체가 equals 로 비교하는 프로그래머는 객체가 같은지가 아니라 값이 같은지를 알고싶어 한다.
- equals가 논리적 동치성을 확인하도록 재정의해두면, 그 인스턴스는 값을 비교하길 원하는 프로그래머 기대에 부응함은 물론 Map의 키와 Set의 원소로 사용할 수 있게 된다.

- 값 클래스라 해도, 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는 인스턴스 통제 클래스라면 equals 를 재정의하지 않아도 된다. Enum이 여기에 해당한다.

- 이런 클래스에서는 어차피 논리적으로 같은 인스턴스가 2개 이상 만들어지지 않으니 논리적 동치성과 객체 식별성이 사실상 똑같은 의미가 된다. 따라서 Object의 equals가 논리적 동치성까지 확인해준다고 볼 수 있다.

- equals 메서드를 재정의할 떄는 반드시 일반 규약을 따라야 하는데, Object 명세에 적힌 규약이다.

  - 반사성(reflextivity): null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true 이다.

  - 대칭성(symmetry): null이 아닌 모든 참조 값 x,y에 대해, x.equals(y)가 tru이면 y.equals(x)도 true이다.

  - 추이성(transitivity): null이 아닌 모든 참조 값 x,y,z에 대해 x.equals(y)가 true이고, y.equals(z)도 true면, x.equals(z)도 true 여야 한다

  - 일관성(consistency): null이 아닌 모든 참조 값 x,y에 대해, x.equals(y)를 반복해서 호출하면 항상 true를 반환하거나 항상 false를 반환해야 한다.

  - null-아님: null이 아닌 모든 참조 값 x에 대해, x.equals(null)은 false 여야 한다.

- 위 규약을 어기면 프로그램이 이상하게 동작하거나 종료될 것이니 주의해야한다.

- 컬렉션 클래스들을 포함해 수많은 클래스는 전달받은 객체가 equals 규약을 지킨다고 가정하고 동작한다.

- **Object 명세에서 말하는 동치관계란?** 집합을 서로 같은 원소들로 이뤄진 부분집합으로 나누는 연산이다. 이 부분집합을 동치류(equivalence class; 동치클래스)라 한다. equals 메서드가 쓸모 있으려면 모든 원소가 같은 동치류에 속한 어떤 원소와도 서로 교환할 수 있어야 한다.

- 동치관계를 만족시키기 위한 다섯 요건을 하나씩 살펴보자.

  1. 반사성은 단순히 말하면 객체는 자기 자신과 같아야 한다는 뜻이다. 만족시키지 못할일이 거의 없다.

  2. 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다는 뜻이다. 반사성 요건과 달리 대칭성 요건은 자칫하면 어길 수 있는데, 대소문자를 구별하지 않는 문자열을 구현한 클래스를 예로 살펴보면 toString 메서드는 원본 문자열의 대소문자를 그대로 돌려주지만 equals에서는 대소문자를 무시한다.

  ```java
  public final class CaseInsensitiveString {
      private final String s;

      public CaseInsensitiveString(String s) {
          this.s = Objects.requireNonNull(s);
      }

      // 대칭성 위배!
      @Override
      public boolean equals(Object o) {
          if (o instanceof CaseInsensitiveString)
            return s.equalsIgnoreCase(
                ((CaseInsensitiveString) o).s);

          if (o instanceof String) // 한 방향으로만 작동!
            return s.equalsIgnoreCase((String) o);

          return false;
      }
      ... // 나머지 코드 생략
  }
  ```

  ```java
  CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
  String s = "polish";
  ```

  - cis.equals(s)는 true를 반환하나, s.equals(cis)는 false를 반환하여 대칭성을 명백히 위반한다.

  - CaseInsensitiveString을 컬렉션에 넣어보자.

  ```java
  List<CaseInsensitiveString> list = new ArrayList<>();
  list.add(cis);
  ```

  - list.contains(s)를 호출하면 false를 반환한다. (Open JDK 버전에 따라 다르게 동작), **equals 규약을 어기면 그 객체를 사용하는 다른 객체들이 어떻게 반응할지 알 수없다.**

  - 이 문제를 해결하려면 CaseInsensitiveString의 equals를 String과도 연동하겠다는 생각을 버려야한다.

  ```java
  @Override
  public boolean equals(Object o) {
      return o instanceof CaseInsensitiveString &&
        ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
  }
  ```

  - 추이성은 첫 번째 객체와 두 번쨰 객체가 같고, 두 번쨰 객체와세 번째 객체가 같다면, 첫 번쨰 객체와 세 번쨰 객체도 같아야 한다는 뜻이다. 이것도 어기기 쉬운데 상위 클래스에는 없는 새로운 필드를 하위클래스에 추가하는 상황을 생각해보자. equals 비교에 영향을 주는 정보를 추가한 것이다. 2차원에서의 점을 표현하는 클래스를 예로 들어보자.

  ```java
  public class Point {
      private final int x;
      private final in y;

      public Point(int x, int y) {
          this.x = x;
          this.y = y;
      }

      @Override
      public boolean equals(Object o) {
          if (!(o instanceof Point))
            return false;
          Point p = (Point)o;
          return p.x == x && p.y == y;
      }

      ... // 나머지 코드 생략
  }
  ```

  - 이제 이 클래스를 확장해서 점에 색상을 더해보자.

  ```java
  public class ColorPoint extends Point {
      private final Color color;

      public ColorPoint(int x, int y, Color color) {
          super(x, y);
          this.color = color;
      }

      ... // 나머지 코드 생략
  }
  ```

  - equals 메서드는 어떻게 해야할까? 그대로 둔다면 Point의 구현이 상속되어 색상(Color) 정보는 무시한 채 비교를 수행한다. equals 규약을 어긴 것은 아니지만, 중요한 정보를 놓치게 되니 받아들일 수 없는 상황이다. 다음 코드처럼 비교 대상이 또 다른 ColorPoint이고 위치와 색상이 같을 때만 true를 반환하는 equals를 생각해보자.

  > 잘못된 코드

  ```java
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof ColorPoint))
        return false;
      return super.equals(o) && ((ColorPoint) p).color == color;
  }
  ```

  - 이 메서드는 일반 Point를 ColorPoint에 비교한 결과와 그 둘을 바꿔 비교한 결과가 다를 수 있다. Point의 equals 는 색상을 무시하고, ColorPoint의 equals는 입력 매개변수의 클래스 종류가 다르다며 매번 false만 반환할 것이다. 각각의 인스턴스를 하나씩 만들어 실제로 동작하는 모습을 확인해보자.

  ```java
  Point p = new Point(1, 2);
  ColorPoint cp = new ColorPoint(1, 2, Color.RED);
  ```

  - p.equals(cp)는 true, cp.equals(p) 는 false 를 반환한다. ColorPoint.equals 가 Point와 비교할 떄는 색상을 무시하도록 하면 해결이 될까?

  > 추이성 위배!

  ```java
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof Point))
        return false;

      // o가 일반 Point면 색상을 무시하교 비교한다.
      if (!(o instanceof ColorPoint))
        return o.equals(this);

      // o가 ColorPoint면 색상까지 비교한다.
      return super.equals(o) && ((ColorPoint) o).color == color;
  }
  ```

  - 이 방식은 대칭성은 지켜주지만, 추이성을 꺠버린다.

  ```java
  ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
  Point p2 = new Point(1, 2);
  ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
  ```

  - 이제 p1.equals(p2)와 p2.equals(p3)는 true를 반환하지만 p1.equals(p3) 가 false 를 반환하므로 추이성에 명백히 위배된다. 이 방식은 무한 재귀에 빠질 위험도 있다. Point의 또 다른 하위 클래스로 SmellPoint를 만들고, equals는 같은 방식으로 구현했다고 해보자. 그런 다음 myColorPoint, equals(mySmellPoint)를 호출하면 StackOverflowError 를 일으킨다.

  - 그럼 해법은 무엇일까? 사실 이 현상은 모든 객체 지향 언어의 동치관계에서 나타내는 근본적인 문제다. **구체 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지 않는다.** 객체 지향적 추상화의 이점을 포기하지 않는 한 말이다.

  - 이 말은 얼핏, equals 안의 instanceof 검사를 getClass 검사로 바꾸면 규약도 지키고 값도 추가하면서 구체 클래스를 상속할 수 있다는 뜻으로 들린다.

  > 리스코프 치환원칙 위배!

  ```java
  @Override
  public boolean equals(Object o) {
      if( o == null || o.getClass() != getClass())
        return false;
      Point p = (Point) o;
      return p.x == x && p.y == y;
  }
  ```

  - 이번 equals는 같은 구현 클래스의 객체와 비교할 떄만 true를 반환한다. 괜찮아 보이지만 실제로 활용할 수는 없다. Point의 하위 클래스는 정의상 여전히 Point이므로 어디서든 Point로써 활용될 수 있어야 한다. 그러나 주어진 점이 (반지름이 1인) 단위 원 안에 있는지를 판별하는 메서드가 필요하다고 해보자.

  ```java
  // 단위 원 안의 모든 점을 포함하도록 unitCircle을 초기화한다.
  private static final Set<Point> unitCircle = Set.of(
        new Point( 1, 0), new Point( 0, 1),
        new Point(-1, 0), new Point( 0, -1));
  public static boolean onUnitCircle(Point p){
      return unitCircle.contains(p);
  }
  ```

  - 이 기능을 구현하는 가장 빠른 방법은 아니지만, 어쨌든 동작은 한다. 이제 값을 추가하지 않는 방식으로 Point를 확장해보자. 만들어진 인스턴스의 개수를 생성자에서 세보도록 하자.

  ```java
  public class CountPoint extends Point {
      private static final AtomicInteger counter = new AtomicInteger();

      public CountPoint(int x, int y){
          super(x, y);
          counter.incrementAndGet();
      }
      public static int numberCreated() { return counter.get(); }
  }
  ```

- [리스코프 치환 원칙](https://pizzasheepsdev.tistory.com/9)에 따르면, 어떤 타입에 있어 중요한 속성이라면 그 하위 탕비에서도 마찬가지로 중요하므로 그 타입의 모든 메서드가 하위 타입에서도 똑같이 잘 작동해야 한다.

- CounterPoint 인스턴스를 onUnitCircle 메서드에 넘기면, Point 클래스의 equals 를 getClass를 사용해 작성했다면 onUnitCircle 은 false를 반환할 것이다.

- 컬렉션 구현체에서 주어진 원소를 담고 있는지를 확인하는 방법에 있는데 onUnitCircle 에서 Set을 포함한 대부분의 컬렉션은 이 작업에 equals 메서드를 사용하는데, CounterPoint 의 인스턴스는 어떤 Point와도 같을 수 없기 떄문이다. 반면, Point의 equals를 instanceof 기반으로 올바르게 구현했다면 CounterPoint 인스턴스를 건네줘도 onUnitCircle 메서드가 제대로 동작할 것이다.

- 구체 클래스의 하위 클래스에서 값을 추가할 방법은 없지만 괜찮은 우회방법이 있는데, 컴포지션을 이용하는 방법이 있긴하다.

```Java
public class ColorPoint {
  private final Point point;
  private final Color clor;

  public ColorPoint(int x, int y, Color color) {
    point = new Point(x, y);
    this.color = Objects.requireNonNull(color);
  }

  public Point asPoint() {
    return point;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ColorPoint))
      return false;
    ColorPoint cp = (ColorPoint) o;
    return cp.point.equals(point) && cp.color.equals(color);
  }
}
```

- 비슷한 방법을 이용한 클래스가 자바 라이브러리에도 있는데 java.sql.Timestamp 는 java.util.Date 를 확장한 후 nanoseconds 필드를 추가했다. 그 결과로 Timestamp의 equals 는 대칭성을 위배하며, Date 객체와 한 컬렉션에 넣거나 서로 섞어 사용하면 엉뚱하게 동작할 수 있다.

- 그래서 Timestamp의 API 설명에는 Date와 섞어 쓸때의 주의사항을 언급하고 있다.

- 일관성은 두 객체가 같다면 (어느 하나 혹은 모두가 수정되지 않는 한 ) 앞으로도 영원히 같아야 한다는 뜼으로, 가변 객체는 비교 시점에 따라 서로 다를수도 혹은 같을 수도 있는 반면에, 불변 객체는 한번 다르면 끝까지 달라야 한다.

- **클래스가 불변이든 가변이든 equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안 된다.**
  예컨대 java.net.URL 의 equals 는 주어진 URL과 매핑된 호스트의 IP 주소를 이용해 비교하는데. 호스트 이름을 IP주소로 바꾸려면 네트워크를 통해야 하는데, 그 결과가 항상 같다고 보장할 수 없으므로 이렇게 구현하는 것은 큰 실수이다.

- equals는 항시 메모리에 존재하는 객체만을 사용한 결정적 계산만 수행해야 한다.

- 마지막 요건은 null-아님 으로 모든 객체가 null과 같지 않아야 한다는 뜻이다. 의도해서 o.equals(null) 이 true를 반환하는 경우야 없기는 하지만, 실수로 NullPointerException 을 던지는 코드는 흔하므로 보통 많은 클래스가 다음 처럼 null인지를 확인 해 자신을 보호한다.

```java
// 명시적 null 검사 - 필요 없다
@Override
public boolean equals(Object o) {
  if (o == null)
    return false;
  ...
}
```

- 동치성을 검사하려면 equals는 건네받은 객체를 적절히 형변환한 후 필수 필드들의 값을 알아내야 한다. 그러려면 형변환에 앞서 instanceof 연산자로 입력 매개변수가 올바른 타입인지 검사해야 한다.

```java
@Override
public boolean equals(Object o){
  if (!(o instanceof MyType))
    return false;
  MyType mt = (MyType) o;
  ...
}
```

- equals가 타입을 확인하지 않으면 잘못된 타입이 인수로 주어졌을 떄 ClassCastException 을 던져서 일반 규약을 윕하게 된다. 그런데 instanceof 는 첫 번쨰 피연산자가 null이면 false를 반환한다. 따라서 null이면 타입 확인 단계에서 false를 반환하기 떄문이 null 검사를 명시적으로 하지 않아도 된다.
