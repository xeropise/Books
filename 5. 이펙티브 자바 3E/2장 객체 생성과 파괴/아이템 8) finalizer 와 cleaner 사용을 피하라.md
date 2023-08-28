- 자바에는 두 가지 객체 소멸자가 있다.

  1.  finalizer

      - finalizer 메서드를 Override 하면 해당 객체가 JVM 에게 가비지 컬렉션을 해야 할 대상이 될 때 호출 된다. 객체가 없어지기 전 다른 연관 자원을 정리하려는 의도로 사용된다.

  2.  cleaner

      - 자바 9 에서 도입된 소멸자로 생성된 Cleaner 가 더 이상 사용되지 않을 때 등록된 스레드에서 정의된 클린 작업을 수행한다. 명시적으로 clean 을 수행할 수도 있는데, AutoCloseable 을 구현해서 try-with-resource 와 같이 사용한다.

> finalize

```java
protected void finalize() throws Throwable { }
```

> cleaner

```java
ublic class CleaningRequiredObject implements AutoCloseable {

    private static final Cleaner cleaner = Cleaner.create​();

    private static class CleanData implements Runnable {

        @Override
        public void run() {
            // 여기서 클린 작업 수행
        }
    }

    private final CleanData;
    private final Cleaner.Cleanable cleanable

    public CleaningRequiredObject() {
        this.cleanData = new CleanData();

        // 등록
        this.cleanable = cleaner.register(this, state);
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}
```

- **finalizer 는 예측할 수 없고, 상황에 따라 위험할 수 있어 일반적으로 불필요하다.**

  - 자바 9 에서는 finalizer 를 사용 자제(deprecated) API 로 지정하고 cleaner 를 그 대안으로 소개한다.

- **cleaner 는 finalizer 보다 덜 위험하지만, 여전히 예측할 수 없고, 느리고, 일반적으로 불필요하다.**

  - C++ 의 파괴자(Destructor) 와는 다른 개념이라고 하며, 같은 동작을 하려면 자바에서는 try-with-resources, try-finally 를 사용해 해결할 것은 권장한다.

  [근데 2개가 어디에 쓰이는 걸까? - 생활코딩](https://youtu.be/QdqUtyq7EJA)

  [자바 가비지 컬렉션은 어떻게 동작할까? - 네이버 D2](https://d2.naver.com/helloworld/1329)

  <br>

- **finalizer 와 cleaner 는 즉시 수행된다는 보장이 없어, 제떄 실행되어야 하는 작업은 절대 할 수 없다.**

  - 예로 파일 닫기를 이 둘에게 맡기는 경우 중대한 오류가 일어날 수 있다 (동시에 열 수 있는 파일 개수에는 한계가 있는데 실행을 게을리해서 파일을 계속 열어 둔다던지..)
  - finalizer 와 cleaner 는 얼마나 신속히 수행할지는 전적으로 가비지 컬렉터의 알고리즘에 달려 있다.

- **상태를 영구적으로 수정하는 작업에서는 절대 finalizer 나 cleaner 에 의존해서는 안 된다.**

  - 예를 들어 데이터베이스 같은 공유 자원의 영구 락 해제를 finalizer 나 cleaner 에 맡겨 놓으면 분산 시스템 전체가 서서히 멈출 것이다.

- **finalizer 와 cleaner 는 심각한 성능 문제도 동반한다.**

- **finalizer 를 사용한 클래스는 finalizer 공격에 노출되어 심각한 보안 문제를 일으킬 수도 있다.**

  - finalizer 의 공격 원리는 생성자나 직렬화 과정에서 예외가 발생하면, 이 생성되다 만 객체에서 악의적인 하위 클래스의 finalizer 가 수행될 수 있게 되는데 이는 있어서는 안되는 일이다. 이 finalizer 는 정적 필드에 자신의 참조를 할당하여 가비지 컬렉터가 수집하지 못하게 막을 수 있다. 이렇게 일그러진 객체가 만들어지고 나면, 이 객체의 메서드를 호출해 애초에는 허용되지 않았을 작업을 수행하는 건 일도 아니다.

- **객체 생성을 막으려면 생성자에서 예외를 던지는 것만으로도 충분하지만, finalizer 가 있다면 그렇지도 않다.**

- **final 이 아닌 클래스를 finalizer 공격으로부터 방어하려면 아무 일도 하지 않는 finalizer 메서드를 만들고 final 로 선언하자.**

## 그렇다고 쳐.. 그럼 왜 사용하는거야?

- 가비지 컬렉터가 회수하지 못하는 네이티브(native) 자원의 정리에 사용한다. 자바 객체가 아니므로 가비지 컬렉터가 관리하는 대상이 아니기 때문이다. finalizer 를 명시적으로 호출함으로 자원을 회수할 수 있다.

- finalizer 는 개발자가 객체의 close 를 명시적으로 호출하지 않은 경우에 사용한다.(안정망 역할) clenaer 와 finalizer 가 즉시 호출되리라는 보장은 없지만, 클라이언트가 하지 않은 자원 회수를 늦게라도 해주는 것이 아예 안 하는 것보다는 낫다.

- 안전망 역할의 finalizer 를 작성할 때는 그럴만한 값어치가 있는지 심사숙고하자. 자바 라이브러리의 일부 클래스는 안전망 역할의 finalizer 를 제공하는데 FileInputStream, FileOutPutStream, ThreadPollExecutor 가 대표적이다.

- **AutoCloseable 을 구현하거나, 클라이언트에서 인스턴스를 다 쓰고 나면 close 메소드를 호출하도록 하자.**
