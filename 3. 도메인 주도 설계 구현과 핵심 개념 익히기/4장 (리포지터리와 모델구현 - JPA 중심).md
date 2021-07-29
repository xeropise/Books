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

### 밸류를 이용한 아이디 매핑

- 기본 타입을 사용하는 것이 나쁘진 않지만 식별자라는 의미를 부각시키기 위해 식별자 자체를 별도 밸류 타입으로 만들 수 있다.

  

- @EmbeddedId 어노테이션을 사용하여 밸류 타입을 식별자로 매핑할 수 있다.

  - JPA에서 식별자 타입은 Serializable 타입이어야 하므로, 식별자로 사용될 밸류 타입은 Serializable 인터페이스를 상속 받아야 한다.

```java
@Entity
@Table(name = "purchaes_order")
public class Order {
  @EmbeddedId
  private OrderNo number;
  ...
}

@Embeddable
public class OrderNo implements Serializable {
  @Column(name="order_number")
  private String number;
  ...
}
```



- 밸류 타입으로 식별자를 구현할 때 얻을 수 잇는 장점은 식별자에 기능을 추가할 수 있다는 점이다.

```java
@Embeddable
public class OrderNo implements Serializable {
  @Column(name="order_number")
  private String number;
  
  public boolean is2ndGeneration() {
    return number.starsWith("W")
  }
}
```



<br> 

***

### 별도 테이블에 저장하는 밸류 매핑

- 애그리거트에서 루트 엔티티를 뺀 나머지 구성요소는 대부분 밸류

  

- 루트 엔티티 외에 또 다른 엔티티가 있다면 진짜 엔티티인지 의심해봐야 한다.

  

- 단지 별도 테이블에 데이터를 저장한다고 해서 엔티티인 것은 아니다.

  

- 밸류가 아니라 엔티티가 확실한다면 다른 애그리거트는 아닌지 확인해야 한다.

  - 자신만의 독자적인 라이프사이클을 갖는다면 , 다른 애그리거트일 가능성이 높다. 

    - (상품과 고객 리뷰,  함께 생성되지도 않고, 함께 변경되지도 않는다. 하지만 화면에 같이 있을 수 있다.)

      

- 애그리거트에 속한 객체가 밸류인지 엔티티인지 구분하는 방법 

  - 고유 식별자는 갖는지 여부를 확인하는 것

    - 식별자를 찾을 때 매핑되는 테이블의 식별자를 애그리거트의 구성요소의 식별자와 동일한 것으로 착각하면 안됨.

      

    - 별도 테이블로 저장되고 테이블에 PK가 있다고 해서 테이블과 매핑되는 애그리거트 구성요소가 고유 식별자를 갖는 것은 아니다.

      - Article, ArticleContent  두 테이블이 있고 각각 ID 를 가지고 있다고 생각해서 엔티티 간 일대일 연관으로 매핑하는 실수

        

      - ArticleContent를 엔티티로 생각할 수 있지만 ArticleContent는 Article 의 내용을 담고 있는 밸류

        

      - ArticleContent 밸류이므로 @Embeddable로 매핑하고,  ArticleContet 와 매핑되는 테이블은 Article 과 매핑되는 테이블과 다르므로, 밸류를 매핑한 테이블을 지정하기 위해 @SecondaryTable 과 @AttributeOverride 를 사용하면 된다.

```java
import javax.persistence.*;

@Entity
@Table(name = "article")
@SecondaryTable(
		name = "article_content",
  	pkJoinColumns = @PrimaryKeyJoinColumn(name = "id")
)
public class Article {
  @Id
  private Long id;
  private String title;
  ...
  @AttributeOverride({
    @AttributeOverride(name = "content", column = @Column(table = "article_content")),
    @AttributeOverride(name = "contentType", column = @Column(table = "article_content"))
  })
  private ArticleContent content;
  ...
}

//////////////////////////////////////

// @SecondaryTable 로 매핑된 article_content 테이블을 조인
// 조인해서 가져오므로 원하지 않는 데이터를 가져올 수도 있으므로, 조회 전용 기능이나 지연 로딩 방식을 사용할 수 있다.
Article article = entityManager.find(Article.class, 1L);
```



<br> 

***

### 밸류 컬렉션을 @Entity 로 매핑하기

- 개념적으로 밸류인데 구현 기술의 한계나 팀 표준 때문에 @Entity 를 사용해야 할 때가 있다.

  

- JPA는 @Embeddable 타입의 클래스 상속 매핑을 지원하지 않으므로, 상속 구조를 갖는 밸류 타입을 사용하려면 @Embeddable  대신  @Entity를 이용한 상속 매핑으로 처리해야 한다. 

  

- 밸류 타입을 @Entity 로 매핑하므로 식별자 매핑을 위한 필드도 추가해야 한다. 또, 구현 클래스를 구분하기 위한 타입 식별(discriminator) 컬럼을 추가해야 한다.

  

- 제품의 이미지 업로드 방식에 따라 이미지 경로와 썸네일 이미지 제공 여부가 달라져  Image 밸류타입을 상속해야 하는 InternalImage, ExternalImage 가 있다고 하자.

  - 한 테이블에 Image 및 하위 클래스를 매핑하므로  Image 클래스에 @Inheritance 를 적용

    

  - strategy 값으로 SINGLE_TABLE 을 사용

    

  - @DiscriminatorColumn 을 이용해서 타입을 구분하는 용도로 사용할 컬럼을 지정 한다.

```java
import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "image_type")
@Table(name = "image")
public abstract class Image {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id")
  private Long id;
  
  @Column(name = "image_path")
  private String path;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "upload_time")
  private Date uploadTIme;
  
  protected Image() {}
  
  public Image(String path) {
    this.path = path;
    this.uploadTime = new Date();
  }
  
  protected String getPath() {
    return path;
  }
  
  public Date getUploadTime() {
    return uploadTime;
  }
  
  public abstract String getURL();
  public abstract boolean hasThumbnail();
  public abstract String getThumbnailURL();
  
}
```



- Image 를 상속받은 클래스는 다음과 같이 @Entity 와 @Discriminator 를 사용해서 매핑을 설정한다.

```java
@Entity
@DiscriminatorValue("II")
public class InternalImage extends Image {
  ...
}

@Entity
@DiscriminatorValue("EI")
public class ExternalImage extends Image {
  ...
}
```



- Image 가 @Entity 이므로 목록을 담고 있는 Product 와 같이 @OneTo-Many 를 이용해서 매핑을 처리한다.

  - 단, 밸류이므로 독자적인 라이프사이클을 갖지 않고, Product 에 완전히 의존한다.

    

  - casecade 속성을 사용해서 Product를 저장할 때, 함께 저장되고, 삭제할 때 함께 삭제되도록 설정한다. 리스트에서  Image 객체를 제거하면 DB에서 함께 삭제되도록 orphanRemoval 도 true 로 설정한다.

```java
@Entity
@Table(name = "product")
public class Product {
  @EmbeddedId
  private ProductId id;
  private String name;
  
  @Convert(converte = MoneyConverter.class)
  private Money price;
  
  private String detail;
  
  @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
            , orphanRemoval = true)
  @JoinColumn(name = "product_id")
  @OrderColumn(name = "list_idx")
  private List<Image> images = new ArrayList<>();
  
  ...
    
  public void changeImages(List<Image> newImage) {
    images.clear(); // select 쿼리로 대상 엔티티를 로딩하고, 각 개별 엔티티에 대해 delete 쿼리를 실행하므로 성능에 문제가 될 수 있다.
    images.addAll(newImages);
  }  
    
}
```



- 하이버네이트는 @Embeddable 타입에 대한 컬렉션의 clear() 메소드를 호출하면 컬렉션에 속한 객체를 로딩하지 않고, 한 번의 delete 쿼리로 삭제 처리를 수행하므로, 애그리거트의 특성을 유지하면서 문제를 해소하려면 결국 상속을 포기해야한다.

  

- @Embeddable 로 매핑된 단일 클래스로 구현해야 한다. 물론, 이 경우 타입에 따라 다른 기능을 구현하려면 if-else 로 구현해야 한다.

  - 코드 유지보수와 성능의 두 가지 측면을 고려해서 구현 방식을 선택해야 한다.

~~~java
@Embeddable
public class Image {
  @Column(name = "image_type")
  private String imageType;
  
  @Column(name = "image_path")
  private String imagePath;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "upload_time")
  private Date uploadTime;
  ...
    
  public boolean hasThumbnail() {
    // 성능을 위해 다형을 포기하고 if-else 로 구현
    if ("II".equals(imageType)){
      return true;
    } else {
      return false;
    }
  }
}
~~~



<br>



***

### ID 참조와 조인 테이블을 이용한 단방향 M:N 매핑

- 애그리거트 간 집합 연관은 성능상의 이유로 피해야 한다고 했으나 요구사항을 구현하는 데 집합 연관을 사용하는 것이 유리하다면?
  - ID 참조를 이용한 단방향 집합 연관을 적용해 볼 수 있다.

```java
@Entity
@Table(name = "product")
public class Product {
  @EmbeddedId
  private ProductId id;
  
  // 단방향 M:N 연관을 ID 참조 방식으로
  @ElementCollection
  @CollectionTable(name = "product_category",
                  joinColumns = @joinColumn(name = "product_id"))
  private Set<CategoryId> categoryIds;
  ...
}
```



- @ElementCollection 을 이요하기 때문에 Product 를 삭제할 때 매핑에 사용한 조인 테이블의 데이터도 함께 삭제 된다.
  - 애그리거트를 직접 참조하는 방식을 사용했다면 영속성 전파나 로딩 전략을 고민해야 하지만, ID 참조 방식을 사용함으로써 고민할 필요가 없다.



<br> 

***

### 애그리거트 로딩 전략

- JPA 매핑을 설정할 때 항상 기억해야 할 점은 애그리거트에 속한 객체가 모두 모여야 완전한 하나가 된다는 것이다.

  

- 애그리거트 루트를 로딩하면 루트에 속한 모든 객체가 완전한 상태여야 함을 의미

```java
// product는 완전한 하나여야 한다.
Product product = productRepository.findById(id);
```



- 조회 시점에서 애그리거트를 완전한 상태가 되도록 하려면?

  - 애그리거트 루트에서 연관 매핑의 조회 방식을 즉시 로딩으로 설정하면 된다. (fetch = FetchType.EAGER)

    

- 즉시 로딩 방식으로 설정하면 애그리거트 루트를 로딩하는 시점에 애그리거트에 속한 모든 객체를 함께 로딩할 수 있는 장점이 있지만, 이 장점이 항상 좋은것은 아니다.

  - 컬렉션에 대해 로딩전략을 설정하면, 오히려 즉시 로딩 방식이 문제 될 수 있다. 카타시안 조인을 이용하므로 카타시안 곱 문제 발생 (중복 발생)

    

- 애그리거트는 개념적으로 하나여야 하지만 루트 엔티티를 로딩하는 시점에 애그리거트에 속한 객체를 모두 로딩해야 하는 것은 아님

  

- 애그리거트가 완전해야 하는 이유?

  - 상태를 변경하는 기능을 실행할 때 애그리거트 상태가 완전해야함

    - 조회 시점에 즉시 로딩을 이용해서 애그리거트를 완전한 상태로 로딩할 필요는 없음

      

    - JPA는 트랜잭션 범위 내에서 지연 로딩을 허용하기 때문에 실제로 상태를 변경하는 시점에 필요한 구성 요소만 로딩해도 문제가 되지 않는다.

    ```java
    @Transactional
    public void removeOptions(ProductId id, int optidxTobeDeleted) {
      // Product를 로딩, 컬렉션은 지연 로딩으로 설정했다면, Option은 로딩하지 않음
      Product product = productRepository.findById(id);
      
      // 트랜잭션 범위이므로 지연 로딩으로 설정한 연관 로딩 가능
      product.removeOption(optidxTobeDeleted);
    }
    
    ///////////////////////////////////////////////
    
    @Entity
    public class Product {
      
      @ElementCollection(fetch = FetchType.LAZY)
      @CollectionTable(name = "product_option",
                      joinColumns = @JoinColumn(name = "product_id"))
      @OrderColumn(name = "list_idx")
      private List<Option> options = new ArrayList<>();
      
      public void removeOption(int optIdx) {
        // 실제 컬렉션에 접근할 때 로딩
        this.options.remove(optIdx);
      }
    }
    ```

    

    - 애플리케이션의 상태를 변경하는 기능보다 조회하는 기능이 빈도가 훨씬 높음
      - 상태 변경을 위해 지연로딩을 사용할 때 발생하는 추가 쿼리로 인한 실행 속도 저하는 문제가 되지 않음

    

  - 표현 영역에서 애그리거트의 상태 정보를 보여줄 때 필요하기 때문

    - 별도의 조회 전용 기능을 구현하는 방식을 사용하는 것이 매우 유리함



<br> 

***

### 애그리거트의 영속성 전파

- 애그리거트가 완전한 상태여야 한다는 것은 애그리거트 루트를 조회할 때뿐만 아니라 저장하고 삭제할 때도 하나로 처리해야 함을 의미

  - 저장 하는 경우, 애그리거트에 속한 모든 객체를 저장

  - 삭제 하는 경우, 애그리거트 루트 뿐만 아니라 애그리거트에 속한 모든 객체 삭제

    

- @Embeddable 매핑 타입의 경우, 함께 저장되고 삭제되므로 cascade 속성을 추가로 설정하지 않아도 된다.

  

- 애그리거트에 속한 @Entity 타입에 대한 매핑은 cascade 속성을 사용해서 저장과 삭제 시에 함께 처리되도록 설정해야 한다.

```java
@OneToMany(cascade = {CacadeType.PERSIST, CascadeType.REMOVE},
          orphanRemoval = true))  // @OneToOne, @OneToMany 는 cascadeType의 기본값이 없다.
@JoinColumn(name = "product_id")  
@OrderColumn(name = "list_idx")  
private List<Image> images = new ArrayList<>();
```



<br> 

***

### 식별자 생성 기능

- 식별자의 생성 방법은 3가지가 있다. 

  - 사용자가 직접 생성

    - 도메인 영역에 식별자 생성 기능을 구현할 필요가 없다.

      

  - 도메인 로직으로 생성

    - 식별자 생성 규칙이 있는 경우, 엔티티를 생성할 때 이미 생성한 식별자를 전달

      

    - 식별자 생성 규칙을 구현하기 적합한 곳은 도메인 말고도 리포지터리가 있음

      

  - DB를 이용한 일련번호 사용

    - @GeneratedValue 를 사용하여 식별자 생성에 사용할 수 있다.

      

    - DB의 insert 쿼리를 실행해야 식별자가 생성되므로 도메인 객체를 리포지터리에 저장할 때 식별자가 생성된다.

      - 도메인 객체를 생성하는 시점에는 식별자를 알 수 없고, 도메인 객체를 저장한 뒤에 식별자를 구할 수 있다.

        

  ```java
  @Entity
  @Table(name = "article")
  ..
  public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public Long getId() {
      return id;
    }
  }  
  
  /////////////////////////////
  
  public class WriteArticleService {
    private ArticelRepository articleRepository;
    
    public Long write(NewArticleRequest req) {
      Articel article = new Article("제목", new ArticelContent("content", "type"));
      articleRepository.save(article); // EntityManager#save()
      																 // 실행 시점에 식별자 생성
      return article.getId();					// 저장 이후 식별자 사용 가능
    }
  }
  ```

  

  