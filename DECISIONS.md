# DECISIONS

판단이 필요했던 부분과 선택 이유 (한 줄씩).

## 사전 준비
- `_legacy/` 에 원본(service.html, CSS/JS, 기획서 PDF, service-images)이 없었음 → 작업 지시서 0번 개요의 명세만으로 구현. 루트 바깥(상위 폴더)은 규칙상 탐색하지 않음.
- 루트의 IntelliJ 기본 템플릿(`src/Main.java`, `pixely.iml`, `.idea/`)은 사용자 파일이라 삭제하지 않고 `.gitignore`로 커밋에서만 제외.
- `java` 가 PATH 에 없음, `~/.jdks/temurin-24.0.2` 존재 → 이 JDK 로 Gradle 을 실행하고 `options.release = 17` 로 Java 17 바이트코드를 생성 (toolchain 17 은 JDK 17 자동 다운로드가 필요해서 사용하지 않음).
- start.spring.io 가 Boot 3.x 를 더 이상 제공하지 않음 → Gradle wrapper 만 받아오고 `build.gradle` 은 Boot 3.5.16 으로 직접 작성, wrapper 는 Boot 3.5 와 검증된 Gradle 8.14.3 으로 고정.
- Docker 미설치 + 로컬 MySQL80 이 3306 에서 실행 중이지만 root 비밀번호를 알 수 없어 DB/계정 생성 불가 → 개발 기본 DB 는 H2 파일 DB(MySQL 모드), `db/init.sql` + README 에 MySQL 전환 방법 기록.
- docker-compose MySQL 은 로컬 MySQL 과의 충돌을 피하려고 호스트 포트 3307(`MYSQL_PORT`)로 매핑.
- 8080, 5173 이 사용 중 → 백엔드 8081, 프론트 5174. 8000 은 비어 있어 ai-server 는 8000 유지.
- 모든 설정은 루트 `.env` 하나에서 읽음: Spring 은 `spring.config.import=optional:file:../.env[.properties]`, Vite 는 `envDir: '..'`, ai-server 는 python-dotenv.
- DB 접속은 `DB_URL/DB_USERNAME/DB_PASSWORD` 3개로 추상화 → URL 만 바꾸면 H2 ↔ MySQL 전환 (드라이버/방언 자동 감지).
- 로컬 git 사용자 정보가 없어 저장소 로컬 설정으로 user.name=kimkkkkj(원격 소유자), user.email=계정 이메일 지정.
