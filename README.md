# UDP Socket Programming

## 개발환경

Window10 / IntelliJ IDEA

## 실행환경

IntelliJ IDEA 2022.2

SDK openjdk-15 Amazon Corretto version 15.0.2

## 개요

- UDP Multicast를 이용한 P2P 방식 오픈 채팅 프로그램 구현

## 과제 명세

### 1. 채팅 프로그램 실행

- 프로그램 실행 인자로 port number를 입력 받는다
- ‘#’로 시작하는 문장은 명령어로 처리(==’#’로 시작하는 메시지를 보낼 수 없다)

### 2. 채팅연결 #JOIN (채팅방이름) (사용자이름)

- peer 채팅방 참여/개설 가능
- 네트워크 내 중복되지 않는 multicast address(225.0.0.0~225.225.225.225)
    - 채팅방이름 — “SHA-256”—> 225.x.y.z로 변환 (java.security.MessageDigest)
    - x,y,z는 해시 값의 마지막 3 byte
    - collision 무시
- 참여: 채팅방 이름을 변환한 multicast address를 이용하여 채팅방 참여 가능

### 3. 채팅

- 사용자 이름이 메시지 시작 부분에 포함되어 전달된다(송신자 표시)
- 채팅방 내에 있는 모든 peer에게 메시지 전달(multicast)
- 메시지를 chunk 단위 (512byte)로 나누어서 전송
- 메시지 송수신 동시 동작(Thread)

### 4. 채팅방 나가기 #EXIT

- 현재 채팅방에서 떠난다

## 프로그램 구조

![Untitled](https://user-images.githubusercontent.com/114637188/234228780-b275a4cd-094d-428a-a5d0-875535f9185d.png)

## 코드 설명

<aside>
💡 자세한 내용은 코드 주석을 참고해주세요.

</aside>

package assignment1/

**UDPTest**

- main 함수, String[] args로 port number를 받는다.
- thread를 생성한 후, isAlive method를 통해 thread가 동작 중인지 확인한다.
- 동작 중이지 않다면 multicast address, chatroom name, user name을 저장할 변수를 초기화한다.
- 사용자로부터 #JOIN chatroom user 또는 #QUIT을 입력 받는다. case-sensitive이며, 이 외의 input은 전부 invalid로 처리하고 다시 입력 받는다.
    - #QUIT을 받으면 System.exit(0)를 통해 즉시 프로그램을 종료한다.
    - valid한 #JOIN을 받으면, message digest의 SHA-256 algorithm으로 chatroom name을 multicast address로 변환한다.
        - multicast address, port number, user name을 인자로 UDPClient, UDPServer, UDPReceiver thread를 시작한다.
        - 각각의 thread가 진행되면서 메시지 송수신이 동시 동작한다.
        - while문으로 loop back하여 thread 동작이 끝나면 다시 #JOIN/#QUIT을 입력 받는다.

**UDPClient**

- msg를 입력 받고 local server에 msg를 송신하는 역할
- Client socket을 random port number로 생성한다. local server에 송신하기 위해 IPAddress를 local host 주소로 설정한다.
- buffer에 msg를 입력 받는다(String)
    - msg가 #로 시작하는 경우 명령어라고 판단한다.
        - msg가 #EXIT이면 채팅방을 떠난다는 msg를 송신하고, thread를 종료한다.
        - 그 외의 #로 시작하는 msg는 invalid로 처리하고 다시 입력을 받는다.
    - #로 시작하지 않는 경우 일반적인 msg이다.
        - msg를 “[username]”을 포함해 512 byte chunk 단위로 분할하기 위해 String arraylist와 substring method를 이용한다.
        - chunk를 차례대로 getBytes()로 변환하여 clientsocket을 통해 local host server에게 전송한다.

**UDPServer**

- UDPClient로부터 msg를 수신 받아 multicast group에 msg를 송신하는 역할.
- Server socket을 args에서 입력받는 port number로 생성한다.
- while문 내에서 반복적으로 receive method를 통해 msg를 기다린다.
- msg packet을 받으면 getData method를 통해 msg String을 생성하고, getBytes method로 다시 byte stream을 생성한다
- datagrampacket을 생성할 때 IP주소는 chatroom name을 변환한 225.x.y.z, port번호는 임의의 숫자 k로 설정하여 송신한다.
    - 이때 msg가 “userName EXIT.”라면, serverSocket을 닫아서 thread를 새로 시작(#JOIN again)했을 때 socket 충돌이 발생하지 않도록 한다.
- while문으로 loop back하여 msg receive를 기다린다.

**UDPReceiver**

- UDPServer로부터 msg를 수신 받아 화면에 출력하는 역할
- DatagramSocket의 하위인 MulticastSocket으로 clientSocket을 생성한다. 이때 port번호는 UDPServer가 송신하고자 했던 port번호 k로 한다.
- chatroom name을 변환한 IP주소와, port번호 k, 네트워크 인터페이스 bge0를 이용하여 joinGroup method로 multicast server에 접속한다.
- 접속했다면 즉시 “username ENTER.” msg를 생성하여 multicast server에 보낸다.
- while문 내에서 clientSocket이 위의 msg를 바로 receive한다. while문을 돌면 다시 UDPServer로부터 msg를 기다린다.
- UDPServer와 같은 방식으로 msg를 String으로 변환한다.
    - msg가 “userName EXIT.”라면, leaveGroup method를 통하여 multicast server와의 접속을 끊고, while문을 탈출한다.
    - 일반적인 msg의 경우 화면에 출력한다.
- while문으로 loop back하여 msg receive를 기다린다.

## 실행

### 컴파일방법

- intellij : 빌드
- cmd : \src> javac assignment1/UDPTest.java -encoding utf-8

### 실행방법

- intellij : run configuration 프로그램 인수(port number) 설정 후 실행
- cmd : \src> java assignment1.UDPTest (port number 입력)

1. #JOIN 채팅방이름 채팅에참여할이름 을 순서대로 입력
2. 원하는 메시지 입력
3. 채팅방을 떠나고 싶다면 #EXIT 입력
4. 채팅방을 나간 후 프로그램을 종료하고 싶다면 #QUIT 입력

### 실행 결과

- cmd
![Untitled 1](https://user-images.githubusercontent.com/114637188/234228542-bca1300b-566c-4300-a8e1-65a43815ede3.png)

![Untitled 2](https://user-images.githubusercontent.com/114637188/234228600-1c4b0a62-9ead-4ab9-99ee-fbc4ccb5fcd3.png)

- IntelliJ + cmd

![Untitled 3](https://user-images.githubusercontent.com/114637188/234228671-f3ea4e3c-3d1b-4538-b267-bd054fbf1516.png)

## Reference

[https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/net/MulticastSocket.html](https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/net/MulticastSocket.html)

[https://velog.io/@1984/MulticastSocketJava#multicastsender](https://velog.io/@1984/MulticastSocketJava#multicastsender)
