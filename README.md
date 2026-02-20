# LS eXP2-1000D HMI 연동용 OPC UA Test Server (Java 17 / Gradle Kotlin DSL)

이 프로젝트는 **Temurin OpenJDK 17** 환경을 기준으로 사용하는 OPC UA 연결 테스트 서버입니다.
LS eXP2-1000D HMI에서 작업일보 List 바인딩을 테스트할 수 있도록 구성되어 있습니다.

## 1) 실행 환경

- Java: Temurin OpenJDK 17
- Build: Gradle (`build.gradle.kts`)
- OPC UA SDK: Eclipse Milo 0.6.14 (`sdk-server`, `stack-server`)

## 2) 서버 실행

```bash
./gradlew run
```

고정 엔드포인트:

- `opc.tcp://192.168.89.2:8624/` (discovery 호환용)
- `opc.tcp://192.168.89.2:8624/lsexp2-test`

서버 보안 설정:

- Security Policy: `None`
- Message Security Mode: `None`
- User Authentication: `Anonymous`

## 3) 작업일보 List 태그 구성

요청 태그(클라이언트/HMI에서 쓰기):

- `ns=<index>;s=LS_EXP2/workReportRequest` (Int16, Write 1~3)

리스트 표시 태그(총 5행, 각 행마다 5컬럼 String):

- `ns=<index>;s=LS_EXP2/workReport/row1/productcode`
- `ns=<index>;s=LS_EXP2/workReport/row1/productname`
- `ns=<index>;s=LS_EXP2/workReport/row1/customer`
- `ns=<index>;s=LS_EXP2/workReport/row1/process`
- `ns=<index>;s=LS_EXP2/workReport/row1/workdeadline`
- 동일 패턴으로 `row2` ~ `row5`

추가 테스트 태그:

- `ns=<index>;s=LS_EXP2/Heartbeat` (Boolean)
- `ns=<index>;s=LS_EXP2/temp` (Int16)

## 4) 데이터 동작 방식

서버에는 더미 작업일보 15건이 내림차순(P015 → P001)으로 고정 저장되어 있습니다.

- `workReportRequest = 1` 전송 시: P015 ~ P011 (5건)
- `workReportRequest = 2` 전송 시: P010 ~ P006 (5건)
- `workReportRequest = 3` 전송 시: P005 ~ P001 (5건)

요청 값이 1 미만이면 1로, 3 초과면 3으로 자동 보정됩니다.

## 5) HMI 설정 예시

1. 통신 드라이버/프로토콜: OPC UA Client
2. 서버 URL: `opc.tcp://192.168.89.2:8624/` (먼저 시도)
   - 연결 실패 시: `opc.tcp://192.168.89.2:8624/lsexp2-test`
3. Security Policy: `None`
4. Message Security Mode: `None`
5. User Authentication: `Anonymous`
6. `workReportRequest` 태그를 쓰기 가능한 숫자 입력기(1~3)로 바인딩
7. row1~row5의 5개 컬럼 태그를 화면 리스트 컴포넌트에 바인딩

> 참고: 네임스페이스 인덱스는 실행 시점에 따라 달라질 수 있으니 서버 콘솔에 출력되는 `ns=<index>`를 사용하세요.
