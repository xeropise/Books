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

  

- Specification 구현 클래스를 개별적으로 ㅁ나들지 않고, 별도 클래스에 스펙 생성 기능을 모아도 된다.

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

