# LS eXP2-1000D HMI 연동용 OPC UA Test Server (Java 17 / Gradle Kotlin DSL)

이 프로젝트는 **Temurin OpenJDK 17** 환경을 기준으로 사용하는 OPC UA 연결 테스트 서버입니다.
이전 코드에서 Milo API 버전 차이로 컴파일 에러가 반복되어, 우선 **접속 확인에 집중한 최소 서버 구성**으로 단순화했습니다.

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

## 3) HMI(LS eXP2-1000D) Client 설정 예시

1. 통신 드라이버/프로토콜: OPC UA Client
2. 서버 URL: `opc.tcp://192.168.89.2:8624/lsexp2-test`
3. Security Policy: `None`
4. Message Security Mode: `None`
5. User Authentication: `Anonymous`

## 4) 참고

- 본 버전은 **"우선 접속이 되는지"**를 검증하기 위한 최소 서버입니다.
- 연결 확인 후, 필요하면 다음 단계에서 커스텀 네임스페이스/태그(`CurrentTemperature`, `Heartbeat` 등)를 버전에 맞춰 추가하는 것이 안전합니다.
