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

네임스페이스 URI:

- `urn:lsexp2:test:namespace`

더미 NodeId:

- `ns=2;s=LS_EXP2/CurrentTemperature` (Double, 1초마다 20.0~30.0 랜덤 변경)
- `ns=2;s=LS_EXP2/Heartbeat` (Boolean, 1초마다 true/false 토글)
- `ns=2;s=LS_EXP2/ServerTime` (DateTime, 현재 UTC 시간)

## 4) HMI(LS eXP2-1000D) Client 설정 예시

1. 통신 드라이버/프로토콜: OPC UA Client
2. 서버 URL: `opc.tcp://192.168.89.2:8624/lsexp2-test`
3. Security Policy: `None`
4. Message Security Mode: `None`
5. User Authentication: `Anonymous`
6. 위 NodeId를 태그로 등록 후 값 모니터링
