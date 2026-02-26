# LS eXP2-1000D HMI 연동용 OPC UA Test Server (Java 17 / Gradle Kotlin DSL)

이 프로젝트는 **Temurin OpenJDK 17** 환경을 기준으로 사용하는 OPC UA 연결 테스트 서버입니다.
LS eXP2-1000D HMI에서 작업일보 List + 상세 페이지 바인딩을 테스트할 수 있도록 구성되어 있습니다.

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

## 3) 작업일보 List + Detail 태그 구성

### 3-1) 클라이언트/HMI에서 서버로 보내는 입력 태그

- `ns=<index>;s=LS_EXP2/workReportCurrentPage` (Int16, Write 1~3)
- `ns=<index>;s=LS_EXP2/workReportTotalPage` (Int16, ReadOnly, 값=3)
- `ns=<index>;s=LS_EXP2/workReportSelectedRow` (Int16, Write 1~5)

### 3-2) 리스트 표시 태그 (5행 x 5컬럼)

- `ns=<index>;s=LS_EXP2/workReport/row1/productcode`
- `ns=<index>;s=LS_EXP2/workReport/row1/productname`
- `ns=<index>;s=LS_EXP2/workReport/row1/customer`
- `ns=<index>;s=LS_EXP2/workReport/row1/process`
- `ns=<index>;s=LS_EXP2/workReport/row1/workdeadline`
- 동일 패턴으로 `row2` ~ `row5`

### 3-3) 상세 페이지 표시 태그 (선택 row 기준)

- `ns=<index>;s=LS_EXP2/workReport/detail/productcodeDetail`
- `ns=<index>;s=LS_EXP2/workReport/detail/productnameDetail`
- `ns=<index>;s=LS_EXP2/workReport/detail/customerDetail`
- `ns=<index>;s=LS_EXP2/workReport/detail/processDetail`
- `ns=<index>;s=LS_EXP2/workReport/detail/workdeadlineDetail`

### 3-4) 추가 테스트 태그

- `ns=<index>;s=LS_EXP2/Heartbeat` (Boolean)
- `ns=<index>;s=LS_EXP2/temp` (Int16)

## 4) 데이터 동작 방식

서버에는 더미 작업일보 15건이 내림차순(P015 → P001)으로 고정 저장되어 있습니다.

- `workReportCurrentPage = 1`: P015 ~ P011 (리스트 5건)
- `workReportCurrentPage = 2`: P010 ~ P006 (리스트 5건)
- `workReportCurrentPage = 3`: P005 ~ P001 (리스트 5건)

그리고 `workReportSelectedRow` (1~5)로 선택한 행의 데이터가 detail 태그 5개에 반영됩니다.

예시)
- `currentPage=2`, `selectedRow=3` -> 전체 15건 기준 8번째 항목(P008)의 컬럼 값이 detail 태그로 출력

요청 값 보정:
- currentPage: 1 미만 -> 1, 3 초과 -> 3
- totalPage 태그는 항상 3으로 제공(페이지 기준점)
- selectedRow: 1 미만 -> 1, 5 초과 -> 5

## 5) HMI 설정 예시

1. 통신 드라이버/프로토콜: OPC UA Client
2. 서버 URL: `opc.tcp://192.168.89.2:8624/` (먼저 시도)
   - 연결 실패 시: `opc.tcp://192.168.89.2:8624/lsexp2-test`
3. Security Policy: `None`
4. Message Security Mode: `None`
5. User Authentication: `Anonymous`
6. `workReportCurrentPage` 태그를 숫자 입력기(1~3)로 바인딩
7. `workReportSelectedRow` 태그를 숫자 입력기(1~5) 또는 선택 인덱스로 바인딩
8. row1~row5의 5개 컬럼 태그를 리스트 컴포넌트에 바인딩
9. detail 5개 태그를 상세 화면 컴포넌트에 바인딩

> 참고: 네임스페이스 인덱스는 실행 시점에 따라 달라질 수 있으니 서버 콘솔에 출력되는 `ns=<index>`를 사용하세요.
