### 아이템 3 - private 생성자나 열거 타입으로 싱글턴임을 보증하라

- [싱글턴](https://medium.com/webeveloper/%EC%8B%B1%EA%B8%80%ED%84%B4-%ED%8C%A8%ED%84%B4-singleton-pattern-db75ed29c36) 이란 인스턴스를 오직 하나만 생성할 수 있는  
  클래스를 말한다. 클래스를 싱글턴으로 만들면 이를 사용하는 클라이언트를 테스트하기가 어려워 질 수 있다.

  > 타입을 인터페이스로 정의한 다음, 그 인터페이스를 구현해서 만든 싱글턴이 아니라면, 싱글턴 인스턴스를 가짜(mock) 구현으로 대체할 수 없기 때문

- 싱글턴을 만드는 방식은 보통 2가지 이다.  
  두 방식 모두 생성자는 private으로 감춰두고, 유일한 인스턴스에 접근할 수 있는 수단으로 public static 멤버를 하나 마련해 둔다

#### 1. 첫번째인 public static 멤버가 final 필드인 방식이다.

```Java
public class Elvis {
     public static final Elvis INSTANCE = new Elvis();
     private Elvis() { ... }

     public void leaveTheBuilding() { ... }
}
```

> private 생성자는 public static final 필드인 Elvis.INSTANCE를 초기화할 때 한 번만 호출된다. public 이나 protected 생성자가 없으므로 Elvis 클래스가 초기화될 때 만들어진 인스턴스가 전체 시스템에서 하나뿐임이 보장된다.

     단, 예외는 있음 리플렉션 API인 AccessibleObject.setAccessible 을 사용해 private 생성자를 호출 가능하다.
     이것을 방어하려면 생성자를 수정하여 두번째 객체가 생성되려 할 때, 예외를 던지게 하면 된다.

#### 2. 두번째로는 정적 팩토리 메서드를 이용해 public static 멤버를 제공한다.

```Java
public class Elvis {
     private static final Evis INSTANCE = new Elvis();
     private Elvis() { ... }
     public static Elvis getInstance() { return INSTANCE; }

     public void leaveTheBuilding() { ... }
}
```

- 장점
  - API를 바꾸지 않고도, 싱글턴이 아니게 변경할 수 있다는 점
  - 원한다면 정적 팩토리를 제네릭 싱글턴 팩토리로 만들 수 있다는 점
  - 정적 팩토레 메소드 참조를 공급자(supplier)로 사용할 수 있다는 점

#### 둘 중 하나의 방식으로 만든 싱글턴 클래스를 직렬화하려면 단순히 Serializable을 구현한다고 선언하는 것만으로 부족하다.

- 모든 인스턴스 필드를 일시적(transient)라고 선언하고, readResovle 메서드를 제공해야 한다. [참조](https://madplay.github.io/post/what-is-readresolve-method-and-writereplace-method)

- 이렇게 하지 않는 경우, 직렬화된 인스턴스를 역직렬화할 때마다 새로운 인스턴스가 만들어지게 된다.

#### 3. 세 번째 방법으로는 원소가 하나인 열거 타입을 선언 하는 방법이 있다.

```Java
 public enum Elvis {
     INSTANCE;

     public void leaveTheBuilding() { ... }
 }
```

- public 필드 방식과 유사하나, 직렬화가 쉬우며, 리플렉션 공격으로 인스턴스가 추가 생성되는 일을 완벽히 맞아 준다.

- 대부분 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법이다.

- 단, 만들려는 싱글턴이 Enum 외의 클래스를 상속해야 한다면 이 방법은 사용할 수 없음.


    (열거 타입이 다른 인터페이스를 구현하도록 선언할 수는 있음)
