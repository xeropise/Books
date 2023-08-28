- clone 메서드는 Object에 정의된 메서드로, 원본 객체의 필드값과 동일한 값을 가지는 새로운 객체를 생성한다. **복제** 하는 메서드 인데, clone 메서드를 사용하면 같은 필드를 가지고 있는 객체를 생성해 준다.

- 복제 라고 표현하고 있는데, 사실 개발을 하면서 비슷한 "복사" 라고 착각 하는 방법으로 얕은 복사를 이용했었다.

<br>

## 얕은 복사(Shallow Copy) vs 깊은 복사(Deep Copy)

**1. 얕은 복사(Shallow Copy)**

- 객체를 복사할 때, 해당 객체만 복사하여 새 객체를 생성한다.

- 복사된 객체의 인스턴스 변수는 원본 객체의 인스턴스 변수와 같은 메모리 주소를 참조한다.

- 따라서, 해당 메모리 주소의 값이 변경되면 원본 객체 및 복사 객체의 인스턴스 변수 값이 같이 변경 된다.

- 일반적인 객체 할당의 방법이 이러한 방법이다.

![캡처](https://user-images.githubusercontent.com/50399804/120071955-5eb95e00-c0cc-11eb-8330-6514583b4c5e.JPG)  
![캡처2](https://user-images.githubusercontent.com/50399804/120071957-5fea8b00-c0cc-11eb-9738-d23dbad472e4.JPG)

**2. 깊은 복사(Deep Copy)**

- 객체를 복사할 때, 해당 객체의 필드값과 동일한 값을 가지는 새로운 객체를 생성한다. 객체를 구현한 클래스에 따라 복사의 범위가 다를 수 있다.

- 얕은 복사와 달리 주소 값을 참조하는게 아닌 새 주소에 담으므로 원본 객체
  혹은 복사된 객체를 변경해도 값이 변경되지 않는다.

- Object의 [Clone](https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#clone--) 메소드 사용이 이에 해당한다.

```java
@HotSpotIntrinsicCandidate
protected native Object clone() throws CloneNotSupportedException;
```

- Object의 clone 메소드는 접근 제어자가 protected 로 되어있어, 리플렉션을 쓰지 않는 이상은 오버라이딩한 메서드에 접근할 수 있다는 게 보장되지 않는다.

- Object Clone 메소드를 오버라이딩하여 사용하면 CloneNotSupportedException 이 발생하는데 이유는 다음과 같다.

```java
/* Thrown to indicate that the clone method in class Object has been called to clone an object,
 * but that the object's class does not implement the Cloneable interface.
 */
```

- Cloneable 인터페이스를 구현하여야 Object의 Clone 메소드를 사용할 수 있는 것이다. ([마커 인터페이스](http://wonwoo.ml/index.php/post/1389)로 작동)

- 일반적인 Clone 메소드의 규약은 다음과 같다. 어떤 객체 x에 대해 다음 식은 참이지만 필수는 아니다.

```java
1. x.clone() != x

// 관례상, 이 메서드가 반환하는 객체는 super.clone 을 호출해 얻어야 한다.
2. x.clone().equals(x)

// 이 클래스와 (Object를 제외한) 모든 상위 클래스는 다음식이 참이다.
3. x.clone().getClass() == x.getClass()
```

- clone 메서드가 super.clone 이 아닌, 생성자를 호출해 얻은 인스턴스를 반환해도 컴파일러는 불평하지 않을 것이다.

```java
@Overrdie
public TestClass clone() {
    return new TestClass(.....);
}
```

- 하지만 이 클래스의 하위 클래스에서 super.clone 을 호출한다면 잘못된 클래스 객체가 만들어져, 하위 클래스의 clone 메서드가 제대로 동작하지 않게 될 수 있다. ( clone 을 재정의한 클래스가 final 이라면 하위클래스가 없어 무시할 수 있다. )

- 제대로 동작하는 clone 메서드를 가진 상위 클래스르 상속해 Cloneable 을 구현하고 싶다고 하면 기존의 PhoneNumber 클래스를 다음과 같이 구현할 수 있다.

```java
@Override
public PhoneNumber clone() {
    try {
        return (PhoneNumber) super.clone();
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

- Object의 clone 메서드는 Object 를 반환하지만 PhoneNumber의 clone 메서드는 PhoneNumber 를 반환하게 했다. 재정의한 메서드의 반환 타입은 상위 클래스의 메서드가 반환하는 타입의 하위 타입일 수 있으므로 클라이언트가 형변환하지 않아도 되게끔 하였다.

- **clone 메서드는 사실상 생성자와 같은 효과를 내는데, 원본 객체에 아무런 해를 끼치지 않는 동시에 복제된 객체의 불변식을 보장해야 한다.**

- 다음의 Stack 클래스의 경우, Clone 을 사용할 경우 문제가 생길 수 있다.

```java
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPAICTY = 16;

    public Stack() {
        this.elemtns = new Object[DEFAULT_INITIAL_CAPAICTY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size]    ;
        elements[size] = null ; // 다 쓴 참조 해제
        return result;
    }

    // 원소를 위한 공간을 적어도 하나 이상 확보한다.
    private void ensureCapacity() {
        if (elements.length == size)
        elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

- 위 Stack 클래스가 단순히 super.clone 의 결과를 반환하면 어떻게 될까? 반환된 Stack 인스턴스의 size 필드는 올바른 값을 갖게되나, **elements 필드는 원본 Stack 인스턴스와 똑같은 배열을 참조하게 된다.** 원본이나 복제본 중 하나를 수정하면 다른 하나도 수정되어 불변식을 해치게 된다. (얕은 복사)

- Stack의 clone 메서드는 제대로 동작하려면 스택 내부 정보를 복사해야 하는데, 가장 쉬운 방법은 elements 배열의 clone을 재귀적으로 호출해주는 것이다.

```java
@Overrid
public Stack clone() {
    try {
        Stack result = (Stack) super.clone();
        result.elements = elements.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        thorw new AssertionError();
    }
}
```

- elements.clone 의 결과를 Object[] 로 형변환할 필요는 없다. **배열의 clone 은 런타임 타입과 컴파일타임 타입 모두가 원본 배열과 똑같은 배열을 반환한다. 따라서 배열을 복제할 때는 배열의 clone 메서드를 사용하라고 권장한다. (사실, 배열은 clone 기능을 제대로 사용하는 유일한 예라 할 수 있다.)**

- elements 필드가 final 이었다면 앞 방식은 작동하지 않는데 final 필드에는 새로운 값을 할당할 수 없기 때문이다. 직렬화와 마찬가지로 Cloneable 아키텍처는 '가변 객체를 참조하는 필드는 final로 선언하라' 는 일반 용법과 충돌한다. 그래서 복제할 수 있는 클래스를 만들기 위해 일부 필드에서 final 한정자를 제거해야 할 수도 있다.

- 이번엔 해시테이블용 clone 메서드를 생각해보자. 해시테이블 내부는 버킷들의 배열이고, 각 버킷은 키-값 쌍을 담는 연결 리스트의 첫 번째 엔트리를 참조한다.

```java
public class HashTable implements Cloneable {
    private Entry[] buckets = ...;

    private static class Entry {
        final Object key;
        Object value;
        Entry next;

        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    ... // 나머지 코드는 생략
}
```

- Stack에서처럼 단순히 버킷 배열의 clone을 재귀적으로 호출해보자.

```java
@Override
public HashTable clone() {
    try {
        HashTable result = (HashTable) super.clone();
        result.buckets = buckets.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

- 복제본은 자신만의 버킷 배열을 갖지만, 이 배열을 원본과 같은 연결 리스트를 참조하여 원본과 복제본 모두 예기치 않게 동작할 가능성이 생긴다. 이를 해결하려면 각 버킷을 구성하는 연결 리스트를 복사해야 한다.

```java
public Class HashTable implements Cloneable {
    private Entry[] buckets = ...;

    private static class Entry {
        final Object key;
        Object value;
        Entry next;

        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        // 이 엔트리가 가리키는 연결 리스트를 재귀적으로 복사
        Entry deepCopy() {
            return new Entry(key, value, next == null ? null : next.deepCopy());
        }

        @Override
        public HashTable clone() {
            try {
                HashTable result = (HashTable) super.clone();
                result.buckets = new Entry[buckets.length];
                for (int i=0; i<buckets.length; i++)
                    if (buckets[i] != null)
                        result.buckets[i] = buckets[i].deepCopy();
                return result;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
        ... // 나머지 코드는 생략
    }
}
```

- private 클래스인 HashTable.Entry 는 깊은 복사를(deep copy)를 지원하도록 보강했다. 버킷이 너무 길지 않다면 잘 작동하는데 연결 리스트를 복제하는 방법으로는 그다지 좋지 않다. 재귀 호출 때문에 리스트의 원소 수만큼 스택 프레임을 소비하여, 리스트가 길면 스택 오버플로우를 일으킬 위험이 있다. 이 문제를 피하려면 깊은 복사를 재귀 호출 대신 반복자를 써서 순회하는 방향으로 수정해야 한다.

```java
Entry deepCopy() {
    Entry result = new Entry(key, value, next);
    for (Entry p = result; p.next != null; p = p.next)
        p.next = new Entry(p.next.key, p.next.value, p.next.next);
}
```

- 이제 복잡한 가변 객체를 복제하는 마지막 방법을 알아보자. 먼저 super.clone 을 호출하여 얻은 객체의 모든 필드를 초기 상태로 설정한 다음, 원본 객체의 상태를 다시 생성하는 고수준 메서드들을 호출한다.

- HashTable 예의 경우라면, buckets 필드를 새로운 버킷 배열로 초기화한 다음 원본 테이블에 담긴 모든 키-값 쌍 각각에 대해 복제본 테이블의 put 메서드를 호출해 둘의 내용이 똑같게 해주면 된다.

- 이처럼 고수준 API 를 활용해 복제하면 보통은 간단하고 제법 우아한 코드를 얻게 되지만, 아무래도 저수준에서 바로 처리할 때보다는 느리다. 또한 Cloneable 아키텍처의 기초가 되는 필드 단위 객체 복사를 우회하기 때문에 전체 Cloneable 아키텍처와는 어울리지 않는 방식이기도 하다.

- 생성자에서 재정의될 수 있는 메서드를 호출하지 않아야 하는데 clone 메서드도 마찬가지다. 만약 clone이 하위 클래스에서 재정의한 메서드를 호출하면, 하위 클래스는 복제 과정에서 자신의 상태를 교정할 기회를 잃게 되어 원본과 복제본의 상태가 달라질 가능성이 크다. **따라서 앞의 예제에서 얘기한 put 메서드는 final 이거나 private 이여야 한다.**

- Object의 clone 메서드는 CloneNotSupportedException 을 던진다고 선언했지만 재정의한 메서드는 그렇지 않다. **public 인 clone 메서드에서는 throws 절을 없애야 한다.** 검사 예외를 던지지 않아야 그 메서드를 사용하기 편하기 때문이다.

- 상속해서 쓰기 위한 클래스 설계 방식 두가지 중 어느 쪽에서든, **상속용 클래스는 Cloneable 을 구현해서는 안 된다.** Object의 방식을 모방할 수도 있다. 제대로 동작하는 clone 메서드를 구현해 protected로 두고 CloneNotSupportedException 도 던질 수 있다고 선언하는 것이다. 이 방식은 Object를 바로 상속할 때처럼 Cloneable 구현 여부를 하위 클래스에서 선택하도록 해준다. 다른 방법으로는, clone 을 동작하지 않게 구현해놓고 하위 클래스에서 재정의하지 못하게 할 수도 있다.

```java
@Override
protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
}
```

- 마지막으로 Cloneable 을 구현한 스레드 안전 클래스를 작성할 때는 clone 메서드 역시 적절히 동기화해줘야 한다는 점이다. 그러니 super.clone 호출 외에 다른 할 일이 없더라도 clone 을 재정의하고 동기화해줘야 한다.

- 요악하자면, Cloneable 을 구현하는 모든 클래스는 clone을 재정의해야 한다. 이떄 접근 제한자는 public 으로 반환 타입은 클래스 자신으로 변경한다. 이 메서드는 가장 먼저 super.clone 을 호출한 후, 필요한 필드를 전부 적절히 수정한다. 이 말은 그 객ㄱ체의 내부 깊은 구조에 숨어 있는 모든 가변 객체를 복사하고, 복사본이 가진 객체 참조 모두가 복사된 객체들을 가리키게 함을 뜻한다. 이러한 내부 복사는 clone을 재귀적으로 호출해 구현하지만, 이 방식이 항상 최선인 것은 아니다. 기본 타입 필드와 불변 객체 참조만 갖는 클래스라면 아무 필드도 수정할 필요가 없다. 단, 일련번호나 고유 ID는 비록 기본 타입이나 불변일지라도 수정해줘야 한다.

- 그런데 위의 작업이 모두 필요한 걸까? 다행이도 이처럼 복잡한 경우는 드물며 Cloneable 을 이미 구현한 클래스를 확장한다면 어쩔 수 없이 clone 을 작 작동하도록 구현해야 한다. 그렇지 않은 상황에서는 **복사 생성자와 복사 팩토리라는 더 나은 객체 복사 방식을 제공할 수 있다.** 복자 생성자란 단순히 자신과 같은 클래스의 인스턴스를 인수로 받는 생성자를 말한다.

> 복사 생성자

```java
public Yum(Yum yum) { ... };
```

> 복사 팩토리

```java
public static Yum newInstance(Yum yum) { ... };
```

- 복사 생성자와 그 변형인 복사 팩토리는 Cloneable/clone 방식보다 나은 면이 많다. 언어 모순적이고 위험천만한 객체 생성 메커니즘 (생성자를 쓰지 않는 방식)을 사용하지 않으며, 엉성하게 문서화된 규약에 기대지 않고, 정상적인 final 필드 용법과도 충돌하지 않으며, 불필요한 검사 예외를 던지지 않고, 형변환도 필요치 않다.

- 복사 생성자와 복새 팩토리는 해당 클래스가 구현한 '인터페이스' 타입의 인스턴스를 인수로 받을 수 있다. 모든 범용 컬렉션 구현체는 Collection 이나 Map 타입을 받는 생성자를 제공한다. 인터페이스 기반 복사 생성자나 복사 팩토리의 더 정확한 이름은 '변환 생성자(conversion constrcutor)'와 '변환 팩토리(conversion factory)' 이다.

- 클라이언트는 이 두가지를 이용하면 원본 타입에 얽매이지 않고 복제본의 타입을 직접 선택할 수 있다. 예로 HashSet 객체 s 를 TreeSet 타입으로 복제할 수 있다. clone 으로 불가능한 이 기능을 변환 생성자로는 간단히 new TressSet<>(s) 로 처리할 수 있다.

### 핵심 정리

- 새로운 인터페이스를 만들 때는 절대 Cloneable 을 확장해서는 안 되며, 새로운 클래스도 이를 구현해서는 안 된다.

- final 클래스라면 Cloneable 을 구현해도 위험이 크지 않지만, 성능 최적화 관점에서 검토한 후 별다른 문제가 없을 때만 드물게 허용해야 한다.

- 기본 원칙은 '복제 기능은 생성자와 팩토리를 이용하는 게 최고' 라는 것이다. 단, 배열만은 clone 메서드 방식이 가장 깔끔한, 이 규칙의 합당한 에외라 할 수 있다.
