# LS eXP2-1000D HMI 연동용 OPC UA Test Server (Java 17 / Gradle Kotlin DSL)

이 프로젝트는 **Temurin OpenJDK 17** 환경의 OPC UA 테스트 서버입니다.
LS eXP2-1000D HMI에서 작업일보 List + Detail, 작업 상태 태그 바인딩 테스트용입니다.

## 1) 실행 환경

- Java: Temurin OpenJDK 17
- Build: Gradle (`build.gradle.kts`)
- OPC UA SDK: Eclipse Milo 0.6.14 (`sdk-server`, `stack-server`)

## 2) 서버 실행

```bash
./gradlew run
```

고정 엔드포인트:

- `opc.tcp://192.168.89.2:8624/` (discovery)
- `opc.tcp://192.168.89.2:8624/lsexp2-test` (service)

보안:

- Security Policy: `None`
- Message Security Mode: `None`
- User Authentication: `Anonymous`

## 3) 작업일보 List + Detail 태그

### 3-1) 페이지/선택 입력 태그 (HMI -> Server)

- `ns=<index>;s=LS_EXP2/workReportCurrentPage` (Int16, Write 1~3)
- `ns=<index>;s=LS_EXP2/workReportTotalPage` (Int16, ReadOnly, 값=3)
- `ns=<index>;s=LS_EXP2/workReportSelectedRow` (Int16, Write 1~5)

### 3-2) 리스트 태그 (row1~row5)

- `.../productcode` (String)
- `.../productname` (String)
- `.../customer` (String)
- `.../process` (String)
- `.../workdeadline` (String)
- `.../targetQuantity` (Int16)  ← 목표생산량 추가

예시:
- `ns=<index>;s=LS_EXP2/workReport/row1/productcode`
- `ns=<index>;s=LS_EXP2/workReport/row1/targetQuantity`

### 3-3) Detail 태그 (선택 row 기준)

- `ns=<index>;s=LS_EXP2/workReport/detail/productcodeDetail` (String)
- `ns=<index>;s=LS_EXP2/workReport/detail/productnameDetail` (String)
- `ns=<index>;s=LS_EXP2/workReport/detail/customerDetail` (String)
- `ns=<index>;s=LS_EXP2/workReport/detail/processDetail` (String)
- `ns=<index>;s=LS_EXP2/workReport/detail/workdeadlineDetail` (String)
- `ns=<index>;s=LS_EXP2/workReport/detail/targetQuantityDetail` (Int16)

## 4) 작업 상태/카운터 입력 태그 (HMI -> Server)

아래 태그들은 HMI에서 서버로 쓰는 형태(Read/Write)로 구성되어 있습니다.

- `ns=<index>;s=LS_EXP2/workStart` (Boolean)
- `ns=<index>;s=LS_EXP2/workPause` (Boolean)
- `ns=<index>;s=LS_EXP2/productionCounter` (Int16)
- `ns=<index>;s=LS_EXP2/defectCount` (Int16)
- `ns=<index>;s=LS_EXP2/workEnd` (Boolean)

서버는 값 변경 시 콘솔에 `[CLIENT->SERVER] ...` 로그를 출력합니다.

## 5) 데이터 동작

- 더미 데이터 총 15건(P015 → P001, 내림차순)
- `currentPage=1` -> P015~P011
- `currentPage=2` -> P010~P006
- `currentPage=3` -> P005~P001
- `selectedRow`(1~5)로 Detail 태그가 갱신됨
- 범위 보정:
  - currentPage: 1~3
  - selectedRow: 1~5

## 6) HMI 바인딩 예시

1. `workReportCurrentPage` 입력기(1~3) 바인딩
2. `workReportSelectedRow` 입력기(1~5) 또는 선택 인덱스 바인딩
3. row1~row5 리스트 컬럼 6개(목표생산량 포함) 바인딩
4. Detail 태그 6개 바인딩
5. 작업 상태/카운터 입력 태그 5개 바인딩

> 네임스페이스 인덱스는 실행 시 달라질 수 있으니 콘솔 출력 기준으로 사용하세요.
