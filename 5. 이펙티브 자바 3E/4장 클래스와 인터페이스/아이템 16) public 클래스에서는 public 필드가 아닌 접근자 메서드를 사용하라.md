- 클래스를 작성할 때는 필드들을 private 로 모두 바꾸고 public 접근자(getter)를 추가해야 한다.

<br>

```java
class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
```

- 패키지 바깥에서 접근할 수 있는 클래스라면 접근자를 제공함으로써 클래스 내부 표현 방식은 언제든 바꿀 수 있는 유연성을 얻을 수 있다. (public 클래스가 필드를 공개하면 이를 사용하는 클라이언트가 생겨날 것이므로 내부 표현 방식을 마음대로 바꿀 수 없게 된다.)

<br>

- default (package-private) 클래스 혹은 private 중첩 클래스라면 데이터 필드를 노출한다 해도 하등의 문제가 없다. ( 그 클래스가 표현하려는 추상 개념만 올바르게 표현해주면 된다. 접근자 방식보다 훨씬 깔끔하다. 솔직히 이해가 잘 안간다...)

<br>

- 자바 플랫폼 라이브러리에도 public 클래스의 필드를 직접 노출하지 말라는 규칙을 어기는 사례가 있다. (java.awt.package의 point와 Dimension 클래스)

<br>

- public 클래스 필드가 불변이라면 직접 노출할 때의 단점이 조금은 줄어들지만, 여전히 결코 좋은 생각이 아니다. API 를 변경하지 않고는 표현 방식을 바꿀 수 없고, 필드를 읽을 때 부수 작업을 수행할 수 없다는 단점은 여전하다.

```JAVA
public final class Time {
    private static final int HOUR_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    public final int hour;
    public final int minute;

    public Time(int hour, int minute) {
        if (hour < 0 || hour >= HOURS_PER_DAY)
            throw new IllegalArgumentException("시간: " + hour);
        if (minute < 0 || minute >= MINUTES_PER_HOUR)
            throw new IllegalArgumentException("분: " + minute);
        this.hour = hour;
        this.minute = minute;
    }
    ... // 나머지 코드 생략
}
```

- public 클래스는 절대 가변 필드를 직접 노출해서는 안 된다. 불변 필드라면 노출해도 덜 위험하지만 완전히 안심할 수는 없다.

- 하지만 default (package-private) 클래스나 private 중첩 클래스에는 종종 (불변이든 가변이든) 필드를 노출하는 편이 나을 때도 있다.
