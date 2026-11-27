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

## 프론트엔드
- 슬라이더는 Swiper 로 통일(tiny-slider 제거) — React 공식 컴포넌트가 있고 a11y/키보드 모듈 제공.
- GLightbox 는 사용하지 않음 — 결과 상세는 접근성 처리된 공용 Modal 로 대체해 의존성 축소.
- custom.js/navbar.js/counter.js/rellax → `Navbar`(모바일 메뉴), `useCountUp`, `useParallax`, App 의 `AOS.init` 으로 이식.
- Noto Sans KR 은 Google Fonts CDN 으로 로드 (번들 크기 절약), 실패 시 시스템 한글 폰트로 대체.
- 색상 리터럴은 `styles/tokens.css` 에만 존재 (mask 용 불투명색도 `--mask-solid` 토큰). Canvas 필터의 픽셀 연산 수치는 JS 상수로 유지.
- 필터는 Canvas 픽셀 연산으로 구현 (로맨틱 글로우는 ctx.filter 대신 축소→확대 블러로 Safari 호환).
- 다운로드/공유 시 현재 필터 버전을 `POST /variants` 로 저장 → 공유 페이지·히스토리에서 필터 버전 확인 가능.
- 공유: 터치 기기 + navigator.share 지원 시 네이티브 공유, 그 외 클립보드 복사, 클립보드 거부 시 링크 모달.
- 비로그인도 스튜디오/라이브러리 조회 가능, 생성·업로드 시 로그인 모달 (선택 상태는 zustand 에 유지).
- 태블릿 이하(≤1023px)는 CSS grid-template-areas 로 1.이미지 선택 → 2.요청사항 → 3.결과 세로 단계형 전환.
- ai-server 기본 엔진은 Pillow mock(백엔드 mock 과 같은 규칙), ComfyUI 는 API 포맷 워크플로우 JSON 의 {{PRODUCT_IMAGE}}/{{MODEL_IMAGE}}/{{PROMPT}} 치환 방식으로 연결 — 실제 ComfyUI 서버가 없어 미검증.
- OpenAI provider 는 키가 없어 실제 호출 대신 로컬 스텁 HTTP 서버 테스트로 요청 형식만 검증.
- 원 지시서의 '6. 완료 기준' 이 잘려 있어 본문 요구사항으로 체크리스트를 구성 (PROGRESS.md).

## 2차 작업 (_legacy 반영)
- `_legacy/` 확인 결과 기획서 PDF와 메인 페이지 원본 HTML(index.html)은 없음 → 메인 구성은 `css/style.css`의 템플릿 섹션(hero, feature, service, section-counter, testimonial, pricing, contact/subscribe, playvid, footer)과 지시서의 기획 흐름을 기준으로 점검. PDF 기반 화면 설계 비교는 하지 못함.
- `_legacy/`는 계속 `.gitignore`로 제외. 1차 작업 때 커밋된 `_legacy/README.md`는 인덱스에서 제거(`git rm --cached`)하고 로컬 파일은 그대로 둠.
- 상품 시드: `service-images/tab1` 14장을 `backend/src/main/resources/seed/products/product-NN.jpg`로 복사 (사용자 허용). 브랜드명이 들어간 실제 상품 컷이라 제목은 브랜드명 없이 일반 명칭으로 지정.
- 모델 시드: `service-images/tab2`는 실존 인물(연예인 가능성) 사진이라 초상권·퍼블리시티권 문제가 있음 → 어디에도 복사·커밋하지 않고, 코드로 그린 가상 모델 8장을 계속 사용. 같은 이유로 인물이 나오는 필터 데모 GIF(`F1~F6.gif`)도 사용하지 않음.
- `LEGACY_IMAGES_DIR` 기반 런타임 시드는 삭제 → tab2를 실수로 시드할 여지를 없앰.
- `video/`(약 300MB)는 커밋하지 않음. 메인 히어로에 `VITE_HERO_VIDEO` 환경변수 자리를 두고, 로컬에서 `frontend/public/videos/`(gitignore)에 복사해 쓰는 방법을 README에 적음.
- DB: `.env`의 접속 정보(pixely 계정, MYSQL_ROOT_PASSWORD의 root, H2용 sa)로 로컬 MySQL 3306 접속을 시도했으나 모두 `ERROR 1045 Access denied` → H2 유지. MySQL 전환 방법은 README 7장.
- Java: Gradle toolchain 17 + foojay resolver로 JDK 17을 자동 다운로드. Gradle을 실행할 JVM은 따로 필요해서, `gradlew.bat`에 JAVA_HOME/PATH가 없으면 `~/.jdks`, Adoptium, Oracle, Microsoft, Corretto, Zulu 설치 폴더에서 JDK를 찾는 fallback을 추가 (wrapper를 업그레이드하면 다시 넣어야 함).
- Gradle 8.14.3은 Java 24까지만 실행 가능 → fallback은 폴더 이름순으로 마지막 JDK를 고르는데, 이 PC에서는 temurin-24가 선택됨.
- IntelliJ 기본 템플릿 `src/Main.java`, `pixely.iml`은 Pixely와 무관해서 삭제. `.idea/`는 IDE 로컬 설정이라 그대로 두고 gitignore 유지.
- 원본의 "연예인 이미지" 탭 이름은 "모델 이미지"로 유지 (실존 인물 사용을 전제하지 않음).
- 원본은 슬롯이 차 있으면 alert로 선택을 막았지만, 클릭 한 번으로 교체하고 토스트로 알리는 편이 덜 번거로워 의도적으로 다르게 유지.
- 원본 필터 타일은 선택된 타일의 글자를 숨기지만, 무엇을 선택했는지 알 수 있도록 선택 타일 하단에 작은 라벨과 체크 표시를 둠.
- 헤더 알림 아이콘(`alram*.png`)은 알림 기능이 없어 가짜 UI가 되므로 넣지 않음.
- 템플릿의 testimonial(후기)은 실제 후기가 없어 꾸며낸 후기가 될 수 있어 넣지 않고 비포/애프터 슬라이더로 대체. pricing·contact도 정책 정보가 없어 미반영.
- 토스트 위치는 스튜디오의 상단 버튼(우측)과 필터·생성 버튼(하단)을 피해 상단 중앙으로.
- `robot.png`(605KB)는 360px로 줄여 약 145KB로 커밋.

## H2 콘솔
- H2 콘솔은 임의의 JDBC URL로 접속할 수 있어 외부에 노출되면 위험함 → `H2_CONSOLE_ENABLED`(기본 false)로만 켜고, Spring Security에서 `hasIpAddress('127.0.0.1') or hasIpAddress('::1')`로 로컬 요청만 허용, H2 자체 설정 `web-allow-others=false`도 함께 적용.
- 개발 편의를 위해 `.env`와 `.env.example`에는 `true`로 둠. 테스트 프로필은 명시적으로 false.
- 콘솔 경로는 Vite 프록시를 거치지 않고 백엔드 포트(8081)로 직접 접속 (콘솔 내부 프레임·세션 경로 문제를 피하기 위함).

## 폴더 구조 정리
- 사용자 요청에 따라 프로젝트 루트를 `IdeaProjects\pixely\pixely`에서 `IdeaProjects\pixely`로 올림. 경로는 모두 상대경로(`../.env`, `./data`, `./uploads`, Vite `envDir: '..'`)라 코드 수정 없이 동작.
- 상위 폴더의 원본 사본은 `_legacy`와 MD5가 모두 같아 사용자 확인 후 삭제. 원본은 `_legacy`(gitignore)에만 남김.
