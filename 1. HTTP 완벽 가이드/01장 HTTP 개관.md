### 1장 HTTP 개관



---

### 1.1 HTTP란?

### ![fetching_a_page](https://github.com/xeropise/read-book/assets/50399804/876af139-b135-4d18-a1a7-e69c77b794d8)


- 전 세계의 웹 브라우저, 서버 , 웹 애플리케이션은 모두 HTTP (HyperText Transfer Protocol) 을 통해 서로 대화한다.

  - HyperText?

    - 하이퍼 링크를 나타낼 수 있는 텍스트

    - 하이퍼 링크를 통해 사용자가 한 문서에서 다른 문서를 이동할 수 있는 글 (Text)

    - HTML(HyperText Markup Language) 는 하이퍼 텍스트를 위한 마크업 언어

      - 마크업 언어는 태그와 부호 등을 이용하여 문서나 데이터의 구조를 나타내는 언어

        

- 하이퍼 텍스트를 전송하는 통신 규약

  

- HTML 을 주고 받는다. 

  

- 신뢰성 있는 데이터 전송 프로토콜 (TCP) 사용하여 전송 중 손상되거나 꼬이지 않음을 보장한다.



---

###  1.2 웹 클라이언트와 서버

- 웹 컨텐츠는 서버에 존재, HTTP 프로토콜로 의사소통하기 때문에 보통 HTTP 서버라고 한다.

  

- 클라이언트(사용자)는 서버에서 HTTP 요청을 보내고, 서버는 요청된 데이터를 HTTP 응답으로 돌려준다.

  

- 일상 생활에서는 브라우저를 통해 요청을 보내고 있다.



---

### 1.3 리소스

- 웹 서버는 웹 리소를 관리하고 제공한다.

  - 정적 파일

    - 텍스트,. HTML, 워드 파일, JPEG 등의 이미지, 동영상 등등..

      

- 리소스는 반드시 정적 파일이어야 할 필요는 없다. 요청에 따라 컨텐츠를 생상하는 프로그램이 될 수도 있다.

  - 사용자가 누구인지, 어떤 정보를 요청했는지, 몇 시인지에 따라 다른 컨텐츠를 생성할 수 있다.
  - 카메라에서 라이브 영상을 보여주거나, 주식 거래, 부동산 데이터베이스 검색 등등..

  

- 어떤 종류의 컨텐츠 소스도 리소스가 될 수 있다.



#### 1.3.1 미디어 타입

- 웹에는 수천 가지의 데이터 타입을 다루기 떄문에 웹에서 전송되는 객체 각각에 신중하게 

  MIME(Multipurpose Internet Mail Extensions) 타입이라는 데이터 포맷 라벨을 붙인다.

  - 원래는 전자메일 시스템 사이에서 메시지가 오갈 떄 겪는 문제점을 해결하기 위한 것이었으나HTTP 에서도 채택되었다.



- 웹 서버는 모든 HTTP 객체 데이터에 MIME 타입을 붙인다.

  

- MIME 타입은 사선(/)으로 구분하여 주 타입(Primary) 과 부 타입(Specific subtype) 으로 이루어진 문자열 라벨이다. 예시로는 아래와 같은데 수백 가지가 있다.

  - HTML로 작성된 텍스트문서 => text/html
  - plain ASCII 텍스트 문서 => text/plain
  - JPEG 이미지 => image/jpeg
  - GIF 이미지 => image/gif



#### 1.3.2 URI (Uniform resource Identifier)

- 서버의 각 리소스 이름은 통합 자원 식별자 혹은 URI로 불린다.

  

- 정보 리소스를 고유하게 식별하고 위치를 지정할 수 있다.

```
http://www.joes-hardware.com/specials/saw-blade.gif
```



- URI에는 2가지가 있는데, URL, URN 이라는 것이 있다.

  

#### 1.3.3 통합 자원 지시자, URL(Uniform resource locator)

- 통합 자원 식별자의 가장 흔한 형태
- 특정 서버의 한 리소스에 대한 구체적인 위치를 서술한다.





#### 1.3.4 유니폼 리소스 이름, URN(Uniform resource name)

- 컨텐츠를 이루는 한 리소스에 대해, 그 리소스의 위치에 영햐을 받지 않는 유일무이한 이름
- 아직 널리 채택되지 않았다.



---

### 1.4 트랜잭션

- HTTP 트랜잭션은 요청 명령과 응답 결과로 구성되어 있다.
- 이 상호작용은 HTTP 메시지라고 불리는 정형화된 데이터 덩어리를 이용해 이루어진다.



#### 1.4.1 메서드

- HTTP 메서드라고 불리는 여러가지 종류의 요청 명령을 지원하며 모든 요청 메시지에는 한 개의 메서드를 갖는다.

  

- 메서드는 서버에게 어떤 동작이 취해져야 하는지 말해준다.



| HTTP 메서드 | 설명                                                         |
| ----------- | ------------------------------------------------------------ |
| GET         | 서버에서 클라이언트로 지정한 리소스를 보내라.                |
| PUT         | 클라이언트에서 서버로 보낸 데이터를 지정한 이름의 리소스로 지정하라. |
| DELETE      | 지정한 리소스를 서버에서 삭제하라.                           |
| POST        | 클라이언트 데이터를 서버 게이트웨이 애플리케이션으로 보내라. |
| HEAD        | 지정한 리소스에 대한 응답에서 HTTP 헤더만 보내라.            |

> 이외에도 여러 method가 있다. https://developer.mozilla.org/ko/docs/Web/HTTP/Methods



#### 1.4.2 상태 코드

- HTTP 메시지는 상태 코드와 함께 반환 된다.



| 상태 코드의 종류 | 의미               |
| ---------------- | ------------------ |
| 1xx              | 정보성             |
| 2xx              | 성공               |
| 3xx              | 리다이렉션         |
| 4xx              | 클라이언트 측 오류 |
| 5xx              | 서버 측 오류       |



#### 1.4.3 웹페이지는 여러 객체로 이루어질 수 있다.

- 웹페이지는 첨부된 리소스들에 대해 각각 별개의 HTTP 트랜잭션을 필요로 한다.



---

### 1.5 메시지

![httpmsgstructure2](https://github.com/xeropise/read-book/assets/50399804/b96c93c3-5a84-4627-ae11-4fc24993074c)

- 시작 줄 (Start line)
  - 메시지의 첫 줄은 시작줄로, 요청이라면 무엇을 해야 하는지 응답이라면 무슨일이 있어났는지 나타난다.
  - 요청과 응답의 형태가 다름에 주의하자.
    - 요청은 HTTP 메서드 및 버전 
    - 응답은 HTTP 버전 및 상태 코드가 오고 있다.



- 헤더 (HTTP Headers)
  - 시작줄 다음에는 0개 이상의 헤더 필드가 이어진다. 각 헤더 필드는 쉬운 구문 분석을 위해 쌍점(:) 으로 구분되어 있는 하나의 이름과 하나의 값으로 구성 된다.
  -  헤더필드를 추가하려면 그저 한 줄을 더하기만 하면 되며, 헤더는 빈 줄로 끝난다.(empty line)



- 본문 (Body)
  - 어떤 종류의 데이터든 들어갈 수 있는 메시지 본문이 필요에 따라 올 수 있다.
  - 요청의 마지막 부분에 들어가나 모든 요청에 본문이 들어가지는 않는다.
  - GET, HEAD, DELETE, OPTIONS 처럼 리소스를 가져오는 요청은 보통 본문이 필요가 없다 



---

### 1.6 TCP 커넥션



#### 1.6.1 TCP/IP

- HTTP는 애플리케이션 계층의 프로토콜이다. 네트워크 통신의 핵심적인 세부사항에 대해서 신경 쓰지 않는다. 대중적이고 신뢰성 있는 인터넷 전송 프로토콜인 TCP/IP 에게 맡긴다.

  

- TCP는 다음과 같은 특징이 있다.

  - 오류 없는 데이터 전송

  - 순서에 맞는 전달

  - 조각나지 안는 데이터 스트림

  
- TCP/IP는 TCP와 IP가 층을 이루는 패킷 교환 네트워크 프로토콜의 집합이다.

<img width="309" alt="Screen Shot 2022-02-10 at 2 12 25 PM" src="https://github.com/xeropise/read-book/assets/50399804/56b3022d-efc9-4a31-bf92-cdd4c92abdb8">


#### 1.6.2 접속, IP 주소 그리고 포트 번호

- HTTP 클라이언트가 서버에 메시지를 전송할 수 있게 되기 전에 인터넷 프로토콜(Internet Protocol, IP) 주소와 포트번호를 사용해 클라이언트와 서버 사이에 TCP/IP 커넥션을 맺어야 한다.

  

- 과정에 대해서는 후술



---

### 1.7 프로토콜 버전



- 오늘 날 쓰이고 있는 HTTP 프로토콜은 여러 버전으로 진화해 왔다. 모든 브라우저에서 같은 버전을 지원하지 않으므로 (보통은 2.0, 구글은 3.0을 지원하고 있음)



- 각 버전 별 특징을 알아 두면 좋다. 상세한 스펙에 대해서는 후술 

  

[참조]: https://developer.mozilla.org/ko/docs/Web/HTTP/Basics_of_HTTP/Evolution_of_HTTP



---

### 1.8 웹의 구성요소

- 프록시 서버, 캐시, 게이트웨이, 터널, 에이전트 등 인터넷과 상호작용 할 수 있는 웹 애플리케이션은 많다.



#### 1.8.1 프록시 서버

- 클라이언트와 서버 사이에 위치하여, 클라이언트의 모든 HTTP 요청을 받아 서버에 전달
- 프록시는 보통 보안을 위해 사용하나 다양한 용도로 사용하고 있다. 관련해서는 후술



#### 1.8.2 캐시

- 클라이언트는 멀리 떨어진 웹 서버 보다 근처의 캐시에서 훨씬 더 빨리 문서를 다운 받을 수 있다.
- 성능 향상을 위해 자주 찾는 문서의 사본을 저장해둔다. 
- 관련해서는 후술



#### 1.8.3 게이트웨이

- 다른 서버들의 중개자로 동작하는 특별한 서버
- 게이트웨이는 주로 HTTP 트래픽을 다른 프로토콜로 변환하기 위해 사용되었다. 관련해서는 후술



#### 1.8.4 터널

- 두 커넥션 사이에서 Raw 데이터를 열어보지 않고, 그대로 전달할 수 있는 HTTP 애플리케이션 이다.
- 주로 비 HTTP 데이터를 하나 이상의 HTTP 연결을 통해 그대로 전송해주기 위해 사용된다. 
- 솔직히 잘 모르겠다.. 관련해서는 후술



#### 1.8.5 에이전트 (봇)

- 사용자를 위해 HTTP 요청을 만들어주는 클라이언트 프로그램
- 사람의 통제 없이 스스로 웹을 돌아다니면 HTTP 트랜잭션을 일으키고 컨텐츠를 받아온다.