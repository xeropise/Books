## 검색을 위한 스펙

- 리포지터리는 애그리거트의 저장소, 애그리거트를 저장하고 찾고 삭제하는 것이 기본 기능

  

- 식별자를 이용하는 것이 기본 기능이지만, 식별자 외에 여러 다양한 조건으로 애그리거트를 찾아야 할 때가 있음.

  

- 검색 조건이 고정되어 있고, 단순하면 특정 조건으로 조회하는 기능을 만들면 됨.

  - 검색 조건의 조합이 다양해지면 모든 조합별로  find 메서드를 정의할 수 없음. 너무 많아짐 => 스펙을 이용해서 해결

    

- 스펙(specification)은 애그리거트가 특정 조건을 충족하는지 여부를 검사 

```java
public interface Specification<T> {
  public boolean isSatisfiedBy(T arg)
}
```



```java
public class OrderSpec implements Specification<Order> {
  private String ordererId;
  public OrdererSpec(String ordererId) {
    this.ordererId = ordererId;
  }
  
  public boolean isSatisfiedBy(Order arg) {
    return agg.getOrdererId().getMemberId().getId().equals(ordererId);
  }
}
```



- 리포지터리는 스펙을 전달받아 애그리거트를 걸러내는 용도로 사용

```java
public class memoryOrderRepository implements OrderRepository {
  public List<Order> findAll(Specification spec) {
    List<Order> allOrders = findAll();
    return  allOrders.stream().filter(order -> spec.isSatisfiedBy(order)).collect(toList());
  }
}
```



- 특정 조건을 충족하는 애그리거트를 찾으려면 원하는 스펙을 생성해서 리포지터리에 전달

```java
Specification<Order> ordererSpec = new OrdererSpec("madvirus");
List<Order> orders = orderRepository.findAll(ordererSpec);
```



<br>

***

### 스펙 조합

- 스펙의 장점은 조합에 있음,  AND나 OR 연산자를 조합해서 새로운 스팩을 만들고, 더 복잡한 스펙을 만들 수 있음

```java
public class AndSpec<T> implements Specification<T> {
  private List<Specification<T>> sepcs;
  
  public AndSpecification(Specification<T> ... specs) {
    this.specs = Arrays.asList(specs);
  }
  
  public boolean isSatisfiedBy(T agg) {
    for (Specification<T> spec : sepcs) {
      if (!spec.isSatisfiedBy(agg)) return false;
    }
    return true;
  }
}
```

```java
Specifcation<Order> ordererSpec = new OrdererSpec("madvirus");
Specification<Order> orderDateSpec = new OrderDateSpec(fromDate, todate);
AndSpec<T> spec = new AndSpec(ordererSpec, orderDateSpec);
List<Order> orders = orderRepository.findAll(spec);
```



<br> 

***

### JPA 를 위한 스펙 구현

- 앞의 스펙은 좋지만 실행 속도에 문제가 있음

  - 애그리거트가 10만개인 경우, 데이터를 DB에서 메모리로 로딩 한 뒤에 10만 개를 루프 돌면서 스펙을 검사, 이는 시스템 성능을 참을 수 없을 만큼 느리게 만듬

    

  - Criteria-Builder 와 Predicate 를 사용하여 검색 조건을 구현해야 함

    

- __JPA 스펙 구현__

  - JPA 를 사용하는 리포지터리를 위한 스펙의 인터페이스는 다음과 같다.

  ```java
  package com.myshop.common.jpaspec;
  
  import javax.persistence.criteria.CriteriaBuilder;
  import javax.persistence.criteria.Prdicate;
  import javax.persistence.criteria.Root;
  
  public interface Specification<T> {
    Predicate toPredicate(Root<T> root, CriteriaBuilder cb);
  }
  
  ////////////////////////////////////
  
  package com.myshop.order.domain;
  
  import com.myshop.common.jpaspec.Specification;
  import com.myshop.member.domain.MemberId_;
  import javax.persistence.criteria.CriteriaBuilder;
  import javax.persistence.criteria.Predicate;
  import javax.persistence.criteria.Root;
  
  public class OrdererSpec implements Specification<Order> {
    private String ordererId;
    
    public OrdererSpec(String ordererId) {
      this.ordererid = ordererid;
    }
    
    @Override
    public Predicate toPredicate(Root<Order> root, CriteriaBuilder cb) {
      // Orderdml orderer.memeberId.id 가 생성자로 전달받은 ordererId 와 같은지 비교
      // JPA 정적 메타 모델을 이용
      return cb.equal(root.get(Order_.orderer)
                     	.get(Orderer_.memberId).get(MemberId_.id), ordererId);
    }
  }
  ```

  

  - Specification 구현 클래스를 개별적으로 만들지 않고, 별도 클래스에 스펙 생성 기능을 모아도 된다.

```java
package com.myship.order.domain;

import com.myshop.common.jpaspec.Specificationl;
import com.myshop.member.domina.MemberId_;

import java.util.Date;

public class OrderSpecs {
  public static Specification<Order> orderer(String ordererId) {
    return (root, cb) -> cb.equal(
    				root.get(Order_.orderer).get(Orderer_.memberId).get(MemberId.id), ordererId
    			);
  }
  
  public static Specification<Order> between(Date from, Date to) {
    return (root, cb) -> cb.between(root.get(Order_.orderDate), from, to);
  }
}

//////////////////////////////

// 스펙 생성이 필요한 코드는 스펙 생성 기능을 제공하는 클래스를 이용해서 간결하게 스펙을 생성할 수 있다.
Specification<Order> betweenSpec = OrderSpecs.between(fromTime, toTIme);
```



- AND/OR 스펙 조합을 위한 구현

```java
// AND
package com.myshop.common.jpaspec;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;

public class AndSpecification<T> implements Specification<T> {
  private List<Specification<T>> specs;
 
  public AndSpecification(Sepcification<T> ... specs) {
    this.specs = Arrays.asList(specs);
  }
  
  @Override
  public Preficate toPredicate(Root<T> root, CriteriaBuilder cb) {
    Predicate[] predicates = specs.stream()
      				.map(spec -> spec.toPredicate(root, cb))
      				.toArray(size -> new Predicate[size]);
    return cb.and(predicates);
  }
}

//////////////////////////////////////////////

// OR
package com.myshop.common.jpaspec;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;

public class OrSpecification<T> implements Specification<T> {
  private List<Specification<T>> specs;
  
  public OrSpecification(Specification<T> ... specs) {
    this.specs = Arrays.asList(specs);
  }
  
  @Override
  public Predicate toPredicate(Root<T> root, CriteriaBuilder cb) {
    Predicate[] predicates = specs.stream()
      		.map(spec-> spec.toPredicate(root, cb))
      		.toArray(Predicate[]::new);
    return cb.or(predicates);
  }
}

//////////////////////////////////////////////////
Specification<Order> specs = Specs.and(
	OrderSpecs.orderer("madvirus"), OrderSpecs.between(fromTime, toTime)
)
 
```



- __스펙을 사용하는 JPA 리포지터리 구현__

```java
public interface OrderRepository {
  public List<Order> findAll(Specification<Order> spec);
  ...
}

//////////////////////////////////////////////////////////////

package com.myshop.infra;

import com.myshop.common.jpaspec.Specification;
import com.myshop.order.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

@Repository
public class JpaOrderRepository implements OrderRepository {
  @PersistenceContext
  private EntityManager entityManager;
  
  ...
    
  @Override
  public List<Order> findAll(Specification<Order> spec) {
    	CriteriaBuilder cb = entitymanager.getCritieriaBuilder();
    	CriteriaQuery<Order> criteriaQuery  = cb.createQuery(Order.class);
    	Root<Order> root = criteriaQuery.from(Order.class);
    	Predicate predicate = spec.toPredicate(root, cb);
    	criteriaQuery.where(predicate);
    	criteriaQuery.orderBy(
      	cb.desc(root.get(Order_.number).get(OrderNo_.number)));
      TypedQuery<Order> query = entityManager.createQuery(criteriaQuery);
    	return query.getResultList();
  }
}
```



<br>



***

### 정렬 구현

- CriteriBuilder 의 경우, asc() 와 desc() 메서드로 정렬할 대상을 지정

  

- JPQL 을 사용하는 경우, JPQL의 order by 절을 사용

```java
TypedQuery<Order> query = entityManager.createQuery(
		"select o from Order o " +
  	"where o.orderer.memberId.id = :ordererId " +
  	"order by o.number.number desc", Order.class
)
```



- 정렬 순서가 고정된 경우에는  위의 방법으로 할 수 있지만, 정렬 순서를 응용 서비스에서 결정 해야 하는 경우는? 

  - 정렬 순서를 리포지터리에 전달할 수 있어야 한다.

    

- 응용 서비스는 다른 타입을 이용해서 리포지터리에 정렬 순서를 전달하고, 다시 Criteria에 맞는 타입으로 변환하는 작업을 해야함 

```java
// 제일 쉬운 방법은 문자열을 사용하는 것이다.
List<Order> orders = orderRepository.findAll(somespec, "number.number desc"); //문자열을 잘못 입력하면 정렬이 제대로 동작하지 않을 위험이..

////////////////////////////////

package com.myshop.infra;

import com.myshop.infra.JpaQueryUtils;

@Repository
public class JpaOrderRepository implements OrderRepository {
  @PersistenceContext
  private EntityManger entityManager;
  ...
  
  @Override
  public List<Order> findAll(Specification<Order> spec, String ... orders) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Order> criteriaQuery = cb.createQuery(Order.class);
    Root<Order> root = criteriaQuery.from(Order.class);
    Predicate predicate = spec.toPredicate(root, cb);
    criteriaQuery.where(predicate);
    if (orders.length > 0) {
      criteriaQuery.orderBy(JpaQueryUtils.toJpaOrder(root, cb, orders));  // 문자열을 Order로 변경
    }
    TypedQuery<Order> query = entityManager.createQuery(criteiraQuery);
    
    return query.getResultList();    
  }
   
}

//////////////////////////////////////////

package com.myshop.infra;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.tuil.stream.Collectors.toList;

public class JpaQueryUtils {
  public static <T> List<Order> toJpaOrders(Root<T> root, CriteriaBuilder cb, String ... orders) {
    if (orders == null || orderes.length == 0) return Collections.emptyList(); 
    
    return Arrays.stream(orders)
      			.map(orderStr -> toJpaOrder(root, cb, orderStr))
      			.collect(toList());
  }
  
  private static <T> Order toJpaOrder(Root<T> root, CriteriaBuilder cb, String orderStr) {
    String[] orderClause = orderStr.split(" "); // [number.number, desc]
    boolean ascending = true;
    if (orderClause.length == 2 && orderClause[1].equalsIgnoreCase("desc")) {
      ascending = false;
    }
    
    String[] paths = orderClause[0].split("\\."); // [number, number]
    Path<Object> path = root.get(paths[0]); // number
    
    for(int i=1; i < paths.length; i++) {
      path = path.get(paths[i]);
    }
    
    return ascending ? cb.asc(path) : cb.desc(path);
    
  }
}
```



> JpaQueryUtils.toJpaOrders() 로부터 다음과 같은 다양한 결과를 예상할 수 있다.
>
> - "name desc" -> cb.desc(root.get( "name" ))
>
> - "customer.name asc" -> cb.asc(root.get( "customer" ).get( "name" ))



- JPQL 도 CriteriaBuilder 와 유사한 방법으로 메서드를 작성할 수 있다.

```java
package com.myshop.infra;

...
import static java.util.Collectors.joining;

public class JpaQueryUtils {
  ...
    
  public static String toJPQLOrderby(String alias, String ... orders) {
    if (orders == null || orders.length == 0) return "";
    String orderParts = Arrays.stream(orders)
      				.map(order -> alias + "." + order)
      				.collect(joining(", "));
    return "order by " + orderParts;
  }  
}


///////////////////////////////////////

TypedQuery<Order> query = entityManger.createQuery(
		"select o from Order o " +
  	"where o.orderer.memberId.id = :ordererId " +
  	JpaQueryUtils.toJPQLOrderby("o", "number.number desc"), Order.class
);
```



<br> 

***

### 페이징과 개수 구하기 구현

- JPA는 페이징 구현을 위한 두 메서드를 제공해 주고 있음
  - setFirstResult(int);
  - setMaxResults(int);

```java
@Override
public List<Order> findByORdererId(String ordererId, int startRow, int fetchSize) {
  TypedQuery<Order> query = entityManager.createQuery(
  		"select o from Order o " +
    	"where o.orderer.memberId.id = :ordererId " +
      "order by o.number.number desc", Order.class);
  
  query.setParameter("ordererId", ordererId);
  query.setFirstResult(startRow); // 읽어올 첫 번째 행 번호 지정, 보통 0번 부터 시작함  , mysql offset
  query.setMaxResults(fetchSize); // 읽어올 행 개수 지정 , mysql limit
  return query.getResultList();
}

/////////////////////////////

List<Order> orders = findByOrdererId("madvirus", 45, 15);
```



- 전체 개수 구하는 기능은 JPQL 을  이용해서 간단히 구현할 수 있다. 

```java
@Repository
public class JpaOrderRepository implements OrderRepository {
  ...
    @Override
    public Long countsAll() {
    	TypedQuery<Long> query = entityManager.createQuery(
      			"select count(o) from Order o", Long.class				
      );
    
    	return query.getSingleResult();
  }
}
```



- Specification 과 조합하여 특정 조건을 충족하는 애그리거트의 개수를 구할 수 있다.

```java
@Repository
public class JpaOrderRepository implements OrderRepository {

  ...
  @Override
  public Long counts(Specification<Order> spec) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
    Root<Order> root = criteriaQuery.from(Order.class);
    criteriaQuery.select(cb.count(root)).where(spec.toPredicate(root,cb));
    TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);
    return query.getSingleResult();
  }
}
```



> 앞에서 배운 것들에 대한 구현을 자동으로 해주는 모듈로  Spring Data JPA 를 얘기해 주고 있다. CriteriaBuilder 와 비슷하게  QueryDSL 로 대체할 수도 있다. 단 아직 비표준 오픈 소스이다.

<br>



***

### 조회 전용 기능 구현

- 리포지터리는 애그리거트의 저장소를 표현하는 것으로, 다음 용도로 사용하는 것은 적합하지 않음

  - 애그리거트를 조합해서 한 화면에 보여주는 데이터 제공

    - 지연 로딩, 즉시 로딩, 연관 매핑을 신경써야함

       

    - [ID 로 참조 할 때 장점](https://github.com/xeropise/read-book/blob/main/3.%20%EB%8F%84%EB%A9%94%EC%9D%B8%20%EC%A3%BC%EB%8F%84%20%EC%84%A4%EA%B3%84%20%EA%B5%AC%ED%98%84%EA%B3%BC%20%ED%95%B5%EC%8B%AC%20%EA%B0%9C%EB%85%90%20%EC%9D%B5%ED%9E%88%EA%B8%B0/3%EC%9E%A5%20(%EC%95%A0%EA%B7%B8%EB%A6%AC%EA%B2%8C%EC%9E%87).md#id%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%95%A0%EA%B7%B8%EB%A6%AC%EA%B2%8C%EC%9E%87-%EC%B0%B8%EC%A1%B0) 을 활용 할 수 없게 됨

      

  - 각종 통계 데이터 제공

    - JPQL이나 Criteria로 처리하기는 어려우므로 조회 전용 쿼리로 처리 해야함

      

- JPA와 하이버네이트의 다음 기능들을 활용하여 조회 전용 기능을 구현할 수 있음

  - __동적 인스턴스 생성__

    - JPA 는 쿼리 결과에서 임의의 객체를 동적으로 생성할 수 있는 기능을 제공

      

    - JPQL 을 그대로 사용하므로 객체 기준으로 쿼리를 작성하면서도 동시에 지연/즉시 로딩과 같은 점을 고려하지 않아도 된다.

  ```java
  @Repository
  public class JpaOrderViewDao implements OverViewDao {
    @Persistence
    private EntityManger em;
    
    @Override
    public List<OrderView> selectByOrderer(String ordererId) {
      String selectQuery = 
        			"select new com.myshop.order.application.dto.OrderView(o, m, p)" +  //  <== select 절에 new 키워드를 이용 했다.
        			"from Order o join o.orderLInes ol, Member m, Product p " +
        			"where o.orderer.memberid.id = :ordererId " +            // Order 애그리거트를 OrderView POJO 로 변환
        			"and o.orderer.memberId = m.id " +										// 표현 영역에 사용할 데이터 형식으로 변환
        			"and index(ol) = 0 " +
        			"and ol.productId = p.id " +
        			"order by o.number.number desc";
      
      TypedQuery<OrderView> query =
        			em.createQuery(selectQuery, OrderView.class);
      query.setParameter("ordererId", ordererId);
      return query.getResultList();
    }
  }
  
  //////////////////////////////////////////////////////////////
  
  public class OrderView {
    private String number;
    private long totalAmounts;
    ...
    private String productName;
    
    public class OrderView(Order order, Member member, Product product) {
      this.number = order.getNumber().getNumber();
      this.totalAmounts = order.getTotalAmounts().getValue();
      ...
      this.productName = product.getName();
    }
    
    ... // get 메서드
  }
  
  //////////////////////////////////////////////////////
  
  // 모델의 개별 프로퍼티를 생성자에 전달할 수도 있다.
  
  // JPQL
  select new com.myshop.order.application.dto.OrderView(
  	o.number.number, o.totalAmounts, o.orderDate, m.id.id, m.name, p.name)
  ...
    
  // 자바 코드
  public class OrderView {
    private String number;
    private int totalAmounts;
    ...
    private String productName;
    
    public OrderView(String number, int totalAmounts, Date orderDate, String memberId, String memberName, String productName) {
      this.number = number;
      this.totalAmounts = totalAmounts;
      ...
      
      this.productName = productName.
    }
  }
   
    
  ```

  

  - __하이버네이트 @Subselect 사용__

    - 쿼리 결과를 @Entity 로 매핑 할 수 있는 기능

      - @Subselect

        - 쿼리 실행결과를 매핑할 테이블처럼 사용

          

        - DBMS 의 View 테이블 같은 역할

          

        - @Subselect 를 사용해도 일반 @Entity와 같아 EntityManager#find(), JPQL Criteria 를 사용해서 조회할 수 있다는 것이 장점이다.

        ```java
        // @Subselect를 적용한 @Entity 는 일반 @Entity와 동일한 방법으로 조회할 수 있다.
        OrderSummary summary = entityManager.find(OrderSummary.class, orderNumber);
        
        TypedQuery<OrderSummary> query = em.createQuery("select os from OrderSummary " +
                                                       	"os where os.ordererId = :ordererId " +
                                                       	"order by os.orderDate desc", OrderSummary.class);
        query.setParameter("ordererId", ordererId);
        List<OrderSummary> result = query.getResultList();
        ```

        

        - @Subselect 의 값으로 지정한 쿼리를 from 절의 서브 쿼리로 사용하게 되는 점을 신경 써야한다.

        ```java
        select osm.nuber, osm.orderer_id, osm.orderer_name, osm.total_amounts, 
        		... 생략
        from (
              select o.order_number as number,
                o.orderer_id, o.orderer_name, o.total_amounts,
                ...생략
              from purchase_order o inner join order_line ol
                  on o.order_number = o.order_number
                  cross join product p
              where ol.line_idx = 0 and ol.product_id = p.product_id
              select o.order_number as number,
              o.orderer_id, o.orderer_name
            ) osm      
        where osm.number = ?      
        ```

        

        

      - @Immutable

        - @Subselect 를 이용한 @Entity 를 매핑 필드를 수정하면 하이버네이트가 더티체킹으로 update 쿼리 실행, 하지만 테이블이 없으므로 에러가 발생

          

        - 위 문제를 방지하기 위해 사용, 엔티티의 매핑 필드/프로퍼티가 변경되어도 DB에 반영하지 않고 무시함

          

      - @Synchronize

        - 하이버네이트가 엔티티를 로딩하기 전에 지정한 테이블과 관련된 변경이 발생하면 플러시를 먼저한다. 

          

        - 사용하지 않으면 다음과 같은 문제가 발생한다.([링크](https://github.com/xeropise/JPA/blob/master/%EC%98%81%EC%86%8D%EC%84%B1%20%EC%BB%A8%ED%85%8D%EC%8A%A4%ED%8A%B8%EC%99%80%20%EC%97%94%ED%8B%B0%ED%8B%B0%20%EA%B4%80%EB%A6%AC.md#%EB%93%B1%EB%A1%9D%EA%B3%BC-%EC%97%94%ED%8B%B0%ED%8B%B0-%EC%BA%90%EC%8B%9C))

      ```java
      // purchase_order 테이블에서 조회
      Order order = orderRepository.findById(orderNumber);
      order.changeShippingInfo(newInfo); // 상태 변경
      
      // 변경 내역이 DB에 반영되지 않았는데 purchase_order 테이블에서 조회
      List<OrderSummary> summaries = ordererSummaryRepository.findByOrdererId(userId);
      ```

  ```java
  import org.hibernate.annotations.Immutable;
  import org.hibernate.annotations.Subselect;
  import org.hibernate.annotations.Synchronize;
  
  import javax.persistence.*;
  import java.util.Date;
  
  @Entity
  @Immutable
  @Subselect("select o.order_number as number, o.orderer_id, o.orderer_name, o.total_amounts," + 
             "o.receiver_name, o.state, o.order_date, " +
             "p.product_id, p.name as porduct_name " +
             "from purchase_order o inner join order_line ol " +
             "		on o.order_number = ol.order_number " +
             "		cross join product p " +
             "where ol.line_idx = 0 and ol.product_id = p.product_id"
            )
  @Syncrhonize({"purchase_order", "order_line", "product"}) // 변경 내역이 발생하면 OrderSummary 를 로딩하는 시점에 변경 내역이 반영
  public class OrderSummary {
    
    @Id
    private String number;
    private String ordererId;
    private String ordererName;
    private int totalAmounts;
    private String receiverName;
    private String state;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "orderDate")
    private Date orderDate;
    private String productId;
    private String productName;
    
    protected OrderSummary() {
      
    }
    
    ...
  }
  ```

  

