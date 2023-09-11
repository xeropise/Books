### 2장 URL과 리소스

---

### URL(Uniform Resource Locator) 이란?

![](https://velog.velcdn.com/images/xeropise1/post/9e795738-7ee8-4bdd-b734-e14fc8bd6824/image.png)


- 웹에서 주어진 고유 리소스의 주소

- 웹에 게시된 리소스를 검색하기 위해 브라우저에서 사용하는 메커니즘

- URL은 다음과 같은 구조로 되어 있다.

    - 스킴(Scheme)
        - 브라우저가 리소스를 요청하는 데 사용해야 하는 프로토콜
          <br>
    - 도메인 네임:포트(DomainName : Port)
        - 권한(authority)이 뒤따르며 문자 패턴 ://에 의해 스키마와 구분 된다.
        - 요청하는 웹 서버의 위치
          <br>
    - 경로(Path)
        - 웹 서버에 있는 리소스의 경로
          <br>
    - 매개변수(Parameter)
        -  웹 서버에 제공되는 추가 매개변수입니다. 이러한 매개변수는 & 기호로 구분된 키/값 쌍 목록이다.
           <br>
    - 앵커(Anchor)
        - 리소스 내부에서 일종의 "책갈피" 역할을 하며, 브라우저에 해당 "책갈피" 지점의 콘텐츠를 표시하도록 지시

---

### URI, URL, URN?

![](https://velog.velcdn.com/images/xeropise1/post/694adc7f-b0b3-4655-a649-ab05501ed362/image.png)

- URI(Uniform Resource Identifier)란?

    - 통합 자원 식별자의 줄임말이다. 인터넷의 자원을 식별할 수 있는 문자열
    - URI의 하위 개념으로 URL과 URN이 있다.

- URL(Uniform Resource Locator)이란?

    - URL은 웹 상에서 리소스(웹 페이지, 이미지, 동영상 등의 파일) 위치한 정보를 나타낸다.


- URN(Uniform Resource Name)이란?

    - URN은 URI의 표준 포맷 중 하나로, 이름으로 리소스를 특정하는 URI이다.
    - 잘 사용하지 않는다.        

---

### URL 문자 집합

- 아스키 코드를 사용해서만 전송할 수 있다.

- 아스키 코드가 아닌 것을 전송하려면 인코딩하여 전송해야 하는데, % 기호로 시작해 아스키코드로 표현되는 2개의 16진수 숫자로 이루어진
  이스케이프 문자로 바꾸면 된다.

```text
http://www.joes-hardware.co/more%20tools.html
```

### 문자 제한

- URL 내에서 특별한 의미로 예약된 문자 들이 있으며 몇몇 문자들은 아스키코드로 출력 가능한 문자 집합에 포함되어 있지 않아
반드시 인코딩해야 하는 문자들이 있다.
