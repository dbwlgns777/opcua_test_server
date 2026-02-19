# LS eXP2-1000D HMI 연동용 OPC UA Test Server (Java 17 / Gradle Kotlin DSL)

이 프로젝트는 **Temurin OpenJDK 17** 환경을 기준으로 사용하는 OPC UA 연결 테스트 서버입니다.
Milo API 버전 충돌 가능성을 줄이기 위해 서버는 단순하게 구성하고, HMI/UA Client에서 바로 확인 가능한 더미 태그를 제공합니다.

## 1) 실행 환경

- Java: Temurin OpenJDK 17
- Build: Gradle (`build.gradle.kts`)
- OPC UA SDK: Eclipse Milo 0.6.14 (`sdk-server`, `stack-server`)

## 2) 서버 실행

```bash
./gradlew run
```

고정 엔드포인트:

- `opc.tcp://192.168.89.2:8624/lsexp2-test`

서버 보안 설정:

- Security Policy: `None`
- Message Security Mode: `None`
- User Authentication: `Anonymous`

## 3) OPC UA Client에서 확인 가능한 더미 데이터

더미 NodeId:

- `ns=<서버 콘솔에 출력된 값>;s=LS_EXP2/Heartbeat` (Boolean, 1초마다 true/false 토글)
- `ns=<서버 콘솔에 출력된 값>;s=LS_EXP2/temp` (UInt16, 1초마다 200~319 범위 랜덤 변경)

## 4) HMI(LS eXP2-1000D) Client 설정 예시

1. 통신 드라이버/프로토콜: OPC UA Client
2. 서버 URL: `opc.tcp://192.168.89.2:8624/lsexp2-test`
3. Security Policy: `None`
4. Message Security Mode: `None`
5. User Authentication: `Anonymous`
6. 위 NodeId를 태그로 등록 후 값 모니터링

> 참고: 네임스페이스 인덱스는 실행 시점에 따라 달라질 수 있으니 서버 콘솔에 출력되는 `ns=<index>;s=...` 값을 사용하세요.


## 5) 연결 오류(BadCommunicationError) 체크리스트

- 서버는 `0.0.0.0:8624`로 바인딩되고, 클라이언트에는 `192.168.89.2:8624`를 안내합니다.
- HMI/UaExpert 설정이 다음과 같은지 확인하세요.
  - Security Policy: `None`
  - Message Security Mode: `None`
  - 인증: `Anonymous`
- PC/장비 방화벽에서 `8624/TCP` 인바운드 허용이 필요합니다.
- 서버와 HMI가 같은 네트워크 대역이며 `192.168.89.2`로 Ping이 되는지 확인하세요.

- UAExpert에 휴지통 아이콘 노드가 보이면, 이전 세션의 stale NodeId일 수 있습니다. Disconnect 후 Reconnect/Address Space 새로고침 후 다시 Browse 하세요.
