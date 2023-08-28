- 지금까지의 내용을 종합으로 양질의 equals 메서드 구현 방법을 단계별로 정리해 보자.

  1. **== 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.** 이는 단순한 성능 최적화용으로, 비교 작업이 복잡한 상황일 때 값어치를 한다.

  ```java
  this == o
  ```

  2. **instanceof 연산자로 입력이 올바른 타입인지 확인한다.** 올바른 타입은 equals가 정의된 클래스인 것이지만, 가끔은 클래스가 구현한 특정 인터페이스거나 어떤 인터페이스를 구현한 클래스끼리 비교할수도 있다.

  ```java
  if(!(o instanceof class))
  ```

  3. **입력을 올바른 타입으로 형변환한다.** 앞에서 instanceof 검사를 했으므로 100% 성공한다.

  ```java
  Class castClass = (Class) o;
  ```

  4. **입력 객체와 자신 자신에 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한다.** 모든 필드가 일치하면 true를, 하나라도 다르면 false를 반환한다. 2단계에서 인터페이스를 사용했다면 입력의 필드 값을 가져올 떄도 그 인터페이스의 메서드를 사용해야 한다. 타입이 클래스라면 접근 권한에 따라 해당 필드에 직접 접근할 수도 있다.

  ```java
  cp.point.equals(point) && cp.color.equals(color);
  ```

- float과 double을 제외한 기본 타입 필드는 == 연산자로 비교하고, 참조 타입필드는 각각의 equals 메서드로, float과 double 필드는 각각 정적 메서드인 Float.compare(float, float)와 Double.compare(double, double)로 비교한다. (float 과 double을 특수한 부동소수 값<Float.NaN, -0.0f> 등을 다뤄야하기 떄문)

- Float.equals 와 Double.equals 메서드를 대신 사용할 수도 있지만, 이 메서드들은 오토박싱을 수반할 수 있으니 성능상 좋지 않다.

- 배열 필드는 원소 각각을 앞서의 지침대로 비교하면 된다. 배열의 모든 원소가 핵심필드라면 Arrays.equals 메서드들 중 하나를 사용하면된다.

- 때론 null도 정상 값으로 취급하는 참조 타입 필드도 있는데, 이런 필드는 정적 메서드인 Object.equals(Object, Object)로 비교해 NullPointerException 발생을 예방하면 된다.

- 어떤 필드를 먼저 비교하느냐가 equals의 성능을 좌우하기도 하는데, 최상의 성능을 바란다면 다를 가능성이 더 크거나 비교하는 비요이 싼 필드를 먼저 비교하자. 동기화용 락 필드 같이 객체의 논리적 상태와 관련 없는필드는 비교하면 안 된다.

- equals를 다 구현했다면 3가지만 자문해보자. 대칭적인가? 추이성이 있는가? 일관적인가? 이런 경우 단위 테스트를 작성해 돌려보자.

- [AutoValue](https://www.baeldung.com/introduction-to-autovalue) 를 이용해 작성하면 테스트를 생략해도 안심할 수는 있다.

- 세 요건 중 하나라도 실패한다면 원인을 찾아서 고치도록 하자. 물론 나머지 요건인 반사성과 null-아님도 만족해야 하지만, 이 둘이 문제되는 경우는 별로 없다.

- 위의 모든 요건에 따라 작성해본 PhoneNumber 클래스용 equals 메서드이다.

```java
public final class PhoneNumber {
  private final short areaCode, prefix, lineNum;

  public PhoneNumber(int areaCode, int prefix, int lineNum) {
    this.areaCode = rangerCheck(areaCode, 999, "지역코드");
    this.prefix = rangeCheck(prefix, 999, "프리픽스");
    this.lineNum = rangeCheck(lineNum, 9999, "가입자 번호");
  }

  private static short rangeCheck(int val, int max, String arg) {
    if (val < 0 || val > max)
      throw new IllegalArgumentException(arg + ": "+ val);
    return (short) val;
  }

  @Override
  public boolean equals(Object o ) {
    if (o == this)
      return true;
    if (!(o instanceof PhoneNumber))
      return false;
    PhoneNumber pn = (PhoneNumber) o;
    return pn.lineNum == lineNum
            && pn.prefix == prefix
            && pn.areaCode == areaCode;
  }
  ...나머지 생략
}
```

- 마지막 주의사항이다.

  - equals 를 재정의할 떈 hashCode도 반드시 재정의하자.

  - 너무 복잡하게 해결하려 들지 말자. 필드들의 동치성만 검사해도 equals 규약을 어렵지 않게 지킬 수 있다. 예로 File 클래스라면, 심볼릭 링크를 비교해 같은 파일을 가리키는지를 확인하려 들면 안 된다. 다행히 File 클래스는 이런 시도를 하지 않는다.

  - Object 외의 타입을 매개변수로 받는 equals 메서드는 선언하지 말자. 많은 프로그래머가 equals를 다음과 같이 작성해놓고 문제의 원인을 찾아 헤맨다.

  ```java
  // 잘못된 예 - 입력 타입은 반드시 Object여야 한다
  public boolean equals(MyClass o) {
    ...
  }
  ```

  - 이 메서드는 Object.equals를 재정의한게 아니다. 입력 타입이 Object가 아니므로 재정의가 아니라 다중정의 한 것이다. 기본 equals를 그대로 둔 채로 추가한 것일지라도, 이처럼 '타입을 구체적으로 명시한' equals는 오히려 해가 된다.

- 이 메서드는 하위 클래스에서의 @Override 애너테이션이 긍정 오류(false positive; 거짓 양성)을 내게 하고 보안 측면에서도 잘못된 정보를 준다.

- 이번 절 예제 코드들에서처럼 @Override 애너테이션을 일관되게 사용하면 이러한 실수를 예방할 수 있다. 예를 들어 다음 equals 메서드는 컴파일되지 않고, 무엇인 문제인지를 정확히 알려주는 오류 메시지를 보여줄 것이다.

```java
// 여전히 잘못된 예 - 컴파일되지 않음
@Override
public boolean equals(MyClass o) {
  ...
}
```

- equals(hashCode도 마찬가지)를 작성하고 테스트하는 일은 지루하고 이를 테스트하는 코드도 항상 뻔하다. 다행히 이 작업을 대신해줄 오픈소스가 있으니, 구글이 만든 AutoValue 프레임워크다. 클래스에 애너테이션 하나만 추가하면 AutoValue가 이 메서드들을 알아서 작성해주며, 직접 작성하는 것과 근본적으로 똑같은 코드를 만들어 줄것이다.

- 대다수의 IDE도 같은 기능을 제공하지만 생성도니 코드가 AutoValue 만큼 깔끔하거나 읽기 좋지는 않다. 또한 IDE는 나중에 클래스가 수정된 걸 자동으로 알아채지는 못하니 테스트 코드를 작성해둬야 한다. 이런 단점을 감안하더라도 사람이 직접 작성하는 것보다는 IDE에 맡기는 편이 낫다. 부주의한 실수가 나지 않기 때문이다.

- 꼭 필요한 경우가 아니라면 equals를 재정의하지 말자. 재정의해야 할 때는 그 클래스의 핵심 필드 모두를 빠짐없이, 다섯 가지 규약을 확실히 지켜가며 비교해야 한다.
