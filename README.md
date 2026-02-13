# LS eXP2-1000D HMI 연동용 OPC UA Test Server (Java 17 / Gradle Kotlin DSL)

이 프로젝트는 **Temurin OpenJDK 17** 환경을 기준으로 사용할 수 있는 간단한 OPC UA Test Server 예제입니다.
서버는 LS eXP2-1000D HMI에서 OPC UA Client로 접속 테스트할 수 있도록 `SecurityPolicy=None` + `Anonymous` 토큰 정책으로 구성되어 있습니다.

## 1) 실행 환경

- Java: Temurin OpenJDK 17
- Build: Gradle (`build.gradle.kts`)
- OPC UA SDK: Eclipse Milo 0.6.14 (`sdk-server`, `stack-server`)

## 2) 서버 실행

```bash
gradle run
```

기본 엔드포인트:

- `opc.tcp://192.168.89.2:8624/lsexp2-test`

## 3) OPC UA 주소 공간 (테스트 태그)

네임스페이스 URI:

- `urn:lsexp2:test:namespace`

테스트 태그(NodeId):

- `ns=2;s=LS_EXP2/CurrentTemperature` (Double, 1초마다 변경)
- `ns=2;s=LS_EXP2/Heartbeat` (Boolean, 1초마다 토글)
- `ns=2;s=LS_EXP2/ServerTime` (DateTime, 현재 UTC 시간)

## 4) HMI(LS eXP2-1000D) Client 설정 예시

1. 통신 드라이버/프로토콜: OPC UA Client
2. 서버 URL: `opc.tcp://192.168.89.2:8624/lsexp2-test`
3. Security Policy: `None`
4. Message Security Mode: `None`
5. User Authentication: `Anonymous`
6. Namespace URI 매핑 후 위 NodeId를 태그로 등록

## 5) 참고

- 현재 샘플은 빠른 연동 테스트용 구성(보안 최소화)입니다.
- 실제 운영 환경에서는 인증서 기반 보안(`Basic256Sha256` 등)과 사용자 인증 정책을 강화해서 사용하세요.
