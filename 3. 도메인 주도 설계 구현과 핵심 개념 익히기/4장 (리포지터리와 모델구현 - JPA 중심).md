## JPA 를 이용한 리포지터리 구현

- 도메인 모델과 리포지터리를 구현할 때 선호하는 기술 - ORM 만 한 것이 없음



***

### 모듈 위치

- 리포지터리 인터페이스는 애그리게잇과 같이 도메인 영역에 속하고, 리포지터리를 구현한 클래스는 인프라스트럭처 영역에 속한다.

  

- 가능하면 리포지터리를 인프라스트럭처 영역에 위치시켜서 인프라스트럭처에 대한 의존을 낮춰야 한다.

<br>

***

### 리포지터리 기본 기능 구현

- 리포지터리 기본 기능은 다음의 2가지 
  - 아이디로 애그리거트 조회
  - 애그리거트 저장

```java
public interface OrderRepository  {
  public Order findById(OrderNo no);
  public void save(Order order);
}
```



- 인터페이스는 애그리거트 루트를 기준으로 작성
  - 주문 애그리거트는 Order 루트 엔티티를 비롯해 여러 엔티티가 있는데, 이 중 Order 루트 엔티티를 기준으로 리포지터리 인터페이스를 작성

```java
@Repository
public class JpaOrderRepository implements OrderRepository {
  @PersistenceContext
  private ENtityManager entityManager;
 
  @Override
  public Order findById(OrderNo id) {
    return entityManager.find(Order.class, id);
  }
  
  @Override
  public void save(Order order) {
    entityManager.persist(order);
  }
}
```



- 아이디 외에 다른 조건으로 애그리거트 조회할 때는 JPA의 Criteria 나 JPQL 을 사용하면 된다.

```java
@Override
public List<Order> findByOrderedId(String orderedId, int startRow, int fetchSize) {
	  TypedQuery<Order> query = entityManager.createQuery(
    	"select o from Order o " +
      "where o.orderer.memberId.id = :ordererId " +
      "order by o.number.number desc",
     	Order.class 
    );
  
  	query.setParameter("ordererId", ordererId);
  	query.setFirstResult(startRow);
	  query.setMaxResults(fetchSize);
  
  	return query.getResultList();
}
```



- 애그리거트를 삭제하는 기능이 필요할 수도 있다. 삭제 기능을 위한 메서드는 애그리거트 객체를 파라미터로 전달 받는다.

```java
public interface OrderRepository {
  ...
  public void delete(Order order);
}


// 구현 클래스는 EntityManager의 remove 이용

public class JpaOrderrepository implements OrderRepository {
   @PersistenceContext
	  private EntityManager entityManager;
  
  	...
      
		@Override
		public void remove(Order order) {
      entityManager.remove(order);
    }      
}
```



<br>

***

## 매핑 구현

### 엔티티와 밸류 기본 매핑 구현

- 애그리거트와 JPA 매핑을 위한 기본 규칙은 다음과 같다. 

  - 애그리거트 루트는 엔티티이므로 @Entity로 매핑 설정

    

  - 한 테이블에 엔티티와 밸류 데이터가 같이 있다면

    - 밸류는 @Embeddable 로 매핑 설정
    - 밸류 타입 프로퍼티는 @Embedded 로 매핑 설정



- 주문 애그리거트를 예로 들어보자.
  - 루트 엔티티는 Order 이다.
    - 루트 엔티티에 속한 Orderer, ShippingInfo 밸류가 있다.
    - ShippingInfo 에 포함된 Address 객체와 Receiver 객체가 있다.



- 주문 애그리거트의 루트 엔티티 Order 는 JPA의 @Entity 로 매핑한다.

```java
@Entity
@Table(name = "purchase_order")
public class Order {
  
 		...
    @Embedded
    private Orderer orderer;
  
  	@Embedded
  	private ShippingInfo shippingInfo;
  	...
     
}
```



- Order 에 속하는 Orderer는 밸류이므로 @Embeddable 로 매핑한다.

```java
@Embeddable // 객체 타입 선언
public class Orderer {
  
  // MemberId에 정의된 컬럼 이름을 변경하기 위해 @AttributeOverride 사용
  @Embedded // 다른 객체 타입을 가져옴, JPA 2부터는 @Embeddable 중첩을 허용
  @AttributeOverrides(
  	@AttributteOverride(name = "id", column = @Column(name = "orderer_id"))
  )
  private MemberId memberId;
  
  @Column(name = "orderer_name")
  private String name;  
}

///////////////////////////////////

@Embeddable
public class MemberId implements Serializable {
  @Column(name = "member_id")
  private String id
}
```



- Orderer 와 마찬가지로  ShippingInfo 밸류도 또 다른 밸류인 Address와 Receiver 를 포함 

```java
@Embeddable
public class ShippingInfo {
  @Embedded
  @AttributeOverrides({
  	@AttributteOverride(name = "zipCode", column = @Column(name = "shipping_zipcode")),
	  @AttributeOverride(name = "address1", column = @Column(name = "shipping_addr1")),
    @AttributeOverride(name = "address2", column = @Column(name = "shipping_addr2"))
  })
  private Address address;
  
  @Column(name = "shipping_message")
  private String message;
  
  @Embedded
  private Receiver receiver; 
```



<br>

***

### 기본 생성자

- 엔티티와 밸류의 생성자는 객체를 생성할 때 필요한 것을 전달 받음.

  

- 위의 Receiver 밸류 타입의 경우, 생성 시점에 수취인 이름과 연락처를 생성자 파라미터로 전달받는다.

```java
public class Receiver {
  private String name;
  private String phone;
  
  public Receiver(String name, String phone) {
    this.name = name;
    this.phone = phone;
  }
}
```



- Receiver 가 불변 타입이면 생성 시점에 필요한 값을 모두 전달받는다.

  - 값을 변경하는 set 메서드 제공 필요치 않음

    

  - 기본 생성자를 추가할 필요가 없다.



- JPA의 @Entity @Embeddable 로 클래스를 매핑하려면 기본 생성자를 제공해야 한다

  - 하이버네이트와 같은 JPA Provider 는 DB에서 데이터를 읽어와 매핑된 객체를 생성할 때 기본 생성자를 사용해서 객체를 생성한다.

    

  - Receiver 와 같은 불변 타입은 기본 생성자가 필요 없음에도 불구하고 다음과 같이 기본 생성자를 추가해야 한다.

  ```java
  @Embeddable
  public class Receiver {
    @Column(name = "reciever_name")
    private String name;
    
    @Column(name = "receiver_phone")
    private String phone;
    
    protected Receiver() {} // JPA 를 적용하기 위해 기본 생성자 추가 , Provider 만 사용하도록 protected 로 선언
    
    public Receiver(String name, String phone) {
      	this.name = name; 
      	this.phone = phone;
    }
    
    ...
  }
  ```



<br> 

***

### 필드 접근 방식 사용

- JPA는 필드와 메서드의 2가지 방식으로 매핑을 처리할 수 있다.

  - 메서드 방식을 사용하려면 프로퍼티를 위한 get/set 메서드를 구현해야 한다.

  ```java
  @Entity
  @Access(AcessType.PROPERTY) // 엔티티가 멤버 변수에 get/set 메서드를 통해 접근한다.
  public class Order {
    
    @Column(name = "state")
    @Enumerated(EnumType.String)
    public OrderState getState() {
      return state;
    }
    
    public void setState(OrdeState state) {
      this.state = state; 
    }
  }
  ```

  

  - 엔티티에 프로퍼티를 위한 공개 get/set 메서드를 추가하면 도메인의 의도가 사라지고 객체가 아닌 데이터 기반으로 구현할 가능성이 높다.

    - 특히 set 메서드는 내부 데이터를 외부에서 변경할 수 있는 수단이 되어, 캡슐화를 깨는 원인이 된다.

      

    - 밸류 타입을 불변으로 구현하고 싶은 경우에는 더더욱이 필요가 없다. 

      

  - 엔티티를 객체가 제공할 기능 중심으로 구현하려면 필드 방식으로 선택해서 불필요한 get/set 메서드를 구현하지 말아야 한다.

  ```java
  @Entity
  @Access(AccessType.FIELD)
  public class Order {
    
    @EmbeddedId
    private OrderNo number;
    
    @Column(name = "state")
    @Enumerated(EnumType.String)  // Enum 타입을 글자 그대로 매핑,  반대로는 EnumType.Ordinal 이 있다.
    private OrderState state;
    
    ... // cancel(), changeShippingInfo() 등 도메인 기능 구현
    ... // 필요한 get 메서드 제공
  }
  ```



<br> 

***

### AttributeConverter 를 이용한 밸류 매핑 처리

- 밸류 타입의 프로퍼티를 한 개의 칼럼에 매핑해야 할 때도 있다. Length 가 길이, 단위의 두 프로퍼티를 가지고 있으면 DB 테이블에는 한 개의 칼럼에 '1000mm' 와 같은 형식으로 저장해야 할 수 있다.

```java
public class Length {
  private int value;			// WIDTH VARCHAR(20)
  private String unit;
}


```



- 2개 이상의 프로퍼티를 가진 밸류 타입을 한 개 칼럼에 매핑해야 할때는  @Embeddable 로 처리할 수는 없다.

  - JPA 버전 별로 변환 처리를 하는 방법이 있는데, 2.1에서는 추가된 AttributeConver 를 사용해서 변환 처리를 할 수 있다.

  ```java
  package javax.persistence;
  
  public interface AttributeConverter<X,Y> {
    	public Y convertToDatabaseColumn (X attribute); // 밸류 타입을 디비 칼럼 값으로
    	public X convertToEntityAttribute (Y dbData); // 디비 칼럼값을 밸류 타입으로
  }
  ```

  ```java
  package shop.infra;
  
  import shop.common.Money
    
  import javax.persitence.AttributeConverter;
  import javax.persistence.Converter;
  
  @Converter(autoAppluy = true) // 모델에 출현하는 모든 Money 타입의 프로퍼티에 대해 MoneyConvert를 자동으로 적용
  public class MoneyConverter implement AttributeConverter<Money, Integer> {
    
    	@Override
    	public Integer convertToDatabaseColumn(Money money) {
        if(money == null)
          	return null;
        else
          	return money.getValue();
      }
    
    	@Override
    	public Money convertToEntityAttribute(Integer value) {
        if(value == null) return null;
        else return new Money(value);
      }
  }
  
  /////////////////////////////////////////////////////
  
  @Entity
  @Table(name = "purchase_order")
  public class Order {
    	...
        
      @Column(name = "total_amounts")
      private Money totalAmounts; 	// MoneyConverter 를 적용해서 값 변환
    
    	...
  }
  ```

  

  - @Converter 의 autoApply 속성이 false 일 경우, 프로퍼티 값을 변환할 때 사용할 컨버터를 직접 지정할 수 있다.

  ```java
  import javax.persistence.Convert;
  
  public class Order {
  
      @Column(name = "total_amounts")
      @Conver(converter = MoneyConverter.class)
      private Money totalAmounts;
  }
  ```



<br> 

***

### 밸류 컬렉션: 별도 테이블 매핑

- 밸류 컬렉션을 별도 테이블로 매핑할 때는 @ElementCollection, @CollectionTable 을 함께 사용해야 한다.

  

- @OrderColumn 으로 지정한 칼럼에 리스트의 인덱스를 저장할 수 있다. 

```java
import javax.persistence.*;

@Entity
@Table(name = "purchase_order")
public class Order {
  	...
	  @ElementCollection
    @CollectionTable(name = "order_line", 
                    joinColumns = @JoinColumn(name = "order_number"))
		@OrderColumn(name = "line_idx") // 인덱스 칼럼 추가
    private List<OrderLine> orderLines;
  
  	...
}

@Embeddable
public class OrderLine {
  
  	@Embedded
  	private ProductId productId;
  
  	@Column(name = "price")
  	private Money price;
  
  	@Column(name = "quantity")
  	private int quantity;
  
  	@column(name = "amounts")
  	private Money amounts;
  
  	...
}
```



<br> 

***

### 밸류 컬렉션: 한 개 컬럼 매핑

- 밸류 컬렉션을 별도 테이블이 아닌 한 개 컬럼에 지정해야 할 때가 있다. 

  - 이메일 주소 목록을 Set 으로 DB에 한 개 컬럼으로 , 로 구분해서 저장

    

- AttributeConverter 를 사용하면 밸류 컬렉션을 한개 컬럼에 쉽게 매핑할 수 있다.

  - 단, 밸류 컬렉션을 표현하는 새로운 밸류 타입을 추가해야 한다.

```java
// 새로운 밸류 타입 추가
public class EmailSet {
  	private Set<Email> emails = new HashSet<>();
  
  	private EmailSet() {}
  
  	private EmailSet(Set<Email> emails) {
      	this.emails.addAll(emails);
    }
  
  	public Set<Email> getEmails() {
      	return Collections.unmodifiableSet(emails);
    }
}

// AttributeConver 를 구현
@Converter
public class EmailSetConverter implements AttributeConverter<EmailSet, String> {
  
  	@Override
  	public String convertTodatabaseColumn(EmailSet attribute) {
      	if(Attribute == null)	return null;
      
      	return attribute.getEmails().stream()
          				.map(Email::toString)
          				.collect(Collectors.joining(","));
    }
  
  	@Override
  	public EmailSet convertToEntityAttribute(String dbData) {
      	if (dbData == null) return null;
      
      	String[] emails = dbData.split(","); 
      
      	Set<Email> emailSet = Arrays.stream(emails)
          					.map(value -> new Email(value))
          					.collect(toSet());
      
      	return new EmailSet(emailSet);
      
    }
}


//////////////////////////////////

// EmailSet을 밸류로 사용하는 엔티티
...
@Column(name = "emails")  
@Convert(converter = EmailSetConverter.class)  
private EmailSet emailSet;  
```



<br> 

***

