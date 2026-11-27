# PROGRESS

- 프로젝트 루트: `C:\Users\nino3\IdeaProjects\pixely\pixely`

## 사전 준비
- [x] 0-1 작업 범위 고정 (루트 확인)
- [x] 0-2 `_legacy` 정리 — 루트에 원본 파일 없음. `_legacy/` 파일 목록: `README.md`(안내문)만 존재
- [x] 0-3 환경 점검 — Java: PATH 없음(~/.jdks/temurin-24.0.2 사용), Node v24.19.0 / npm 11.17.0, Docker 미설치, Python 3.10.11
  - 포트: 3306 사용중(MySQL80), 8080 사용중, 5173 사용중, 8000 비어있음
- [x] 0-4 DB 결정 — H2 파일 DB(MySQL 모드) 기본, MySQL 전환 가이드 제공
- [x] 0-5 `.env.example` / `.env` 생성
- [x] 0-6 Git init (main), origin = https://github.com/kimkkkkj/pixely.git

## 1) _legacy 분석 → 기능/화면 목록
원본 파일이 없어 지시서 명세에서 도출:
- 스튜디오: 좌측 사이드바(상품/모델 탭, 검색, 2열 그리드, 드래그 스크롤) / 우측 선택 슬롯 2개(클릭 시 해제 확인, 빈 슬롯 클릭 시 탭 이동) / 요청사항 textarea + 생성 버튼 / 가운데 결과 미리보기 + 5색 웨이브 로딩 / 생성 후 필터 6종 / 상단 공유·다운로드
- 기존 시안 문제점(해결 대상): generateImage 중복, openTab 전역 event, 드래그 스크롤 첫 그리드만, GIF 교체식 가짜 필터, 공유 미구현, Swiper 미사용, 실제 AI 출력 아님
- 메인: 히어로, 문제 정의, 3단계 사용법, 타겟 플랫폼, 카운터, 비포/애프터 슬라이더, FAQ, 푸터
- [x] 완료

## 2) 루트 구조
- [x] docker-compose.yml, db/init.sql, .env.example/.env, .gitignore, .gitattributes

## 3) 백엔드
- [x] 스캐폴딩 (Spring Boot 3.5.16, Gradle 8.14.3, Java 17 release)
- [x] 엔티티/레포지토리 (User, LibraryImage, Generation, GenerationVariant, ShareLink)
- [x] 인증 (signup/login/me, JWT, BCrypt)
- [x] 라이브러리 API (검색/페이지/업로드 리사이즈/삭제) + 관리자 샘플 API
- [x] 생성 API (@Async + 60초 타임아웃 + 분당 5회 제한, mock provider)
- [x] 공유/히스토리/관리자 생성내역
- [x] 시드 (관리자 계정, 플레이스홀더 상품 8 + 가상 모델 8, 중단 작업 FAILED 처리)

## 4) 백엔드 빌드/테스트
- [x] `.\gradlew build` 성공 — ApiIntegrationTest 11개 + MockImageGenerationProviderTest 3개 통과 (H2 test 프로필)

## 5) 프론트엔드
- [x] 스캐폴딩 (React 18 + Vite 6 + React Router 6 + Axios + Zustand + Bootstrap 5 + AOS + Swiper)
- [x] 디자인 토큰(tokens.css) / 공통 컴포넌트 (Button, Modal, Toast, ConfirmHost, Card, Tabs, Skeleton, Empty/Error, WaveLoader, Icon)
- [x] 메인 페이지 (히어로, 문제 정의, 3단계, 플랫폼 뱃지, 카운터, 비포/애프터 Swiper, FAQ, CTA, 푸터)
- [x] 스튜디오 (라이브러리 탭/검색/무한스크롤/드래그 스크롤/업로드, 슬롯, 요청사항+칩, 생성+polling, Canvas 필터 6종, 다운로드, 공유)
- [x] 히스토리 / 공유 / 로그인·회원가입 / 관리자

## 6) 프론트 빌드
- [x] `npm run lint` 경고 0, `npm run build` 에러 0

## 7) 실제 흐름 점검 (puppeteer-core + 로컬 Chrome, headless)
- [x] 회원가입 → 스튜디오 → 상품/모델 선택 → 예시 칩 → 생성(mock, polling) → 로맨틱 필터 → 다운로드(`pixely-N-romantic.png`) → 공유 링크(공개 페이지에 원본/로맨틱 버전 표시) → 히스토리 → 상세 → 다시 편집 → 390/768/1024 반응형 → 비로그인 생성 시 로그인 모달 → 관리자 로그인 → 샘플 업로드(8→9)
- [x] 콘솔 에러 / 5xx 응답 0건
- 발견·수정: 결과 이미지가 미리보기 영역을 넘치던 문제(absolute + object-fit), 토스트가 필터 스트립을 가리던 문제(우측 상단으로 이동), mock 합성 그림자가 투명 상품에서 사각형으로 보이던 문제(알파 실루엣 블러)

## 8) openai / fastapi provider + ai-server
- [x] OpenAiImageGenerationProvider — `/v1/images/edits`, `image[]` 2장 + 영어 광고 프롬프트(PromptBuilder) — 스텁 서버 테스트로 요청 형식/에러 처리 검증 (실제 키 없음)
- [x] FastApiImageGenerationProvider — ai-server `/generate` 위임, 실제 연동 확인 (AI_PROVIDER=fastapi 로 생성 DONE, 결과 이미지 확인)
- [x] ai-server (FastAPI + Pillow mock, ComfyUI 워크플로우 연결 모듈)
- 발견·수정: JDK HttpClient 의 h2c 업그레이드를 uvicorn 이 거부(422) → HTTP/1.1 고정

## 9) README
- [x] 소개, 아키텍처(Mermaid), ERD(Mermaid), API 목록, PowerShell 실행법, 환경변수, provider 전환, MySQL 전환, 기존 시안 대비 개선점

## 완료 기준 체크 (지시서 6번이 잘려 있어 본문 요구사항 기준으로 정리)
- [x] 0-4 규칙에 따른 DB 결정 + DECISIONS.md 기록 (H2 기본, MySQL 전환 가이드, compose 3307)
- [x] 비밀값 전부 .env, .env 는 git 제외
- [x] 백엔드 `gradlew build` 성공, 테스트 18개 통과
- [x] 프론트 `npm run build` 에러 0, `npm run lint` 경고 0
- [x] 키 없이(mock) 회원가입→생성→필터→다운로드→공유→히스토리→관리자 업로드 전체 흐름 브라우저 점검 통과
- [x] 기존 시안 문제 7가지 전부 해결 (README 9장)
- [x] 3개 provider 구현, AI_PROVIDER 로 전환
- [x] 반응형 1440/1024/768/390 확인, alert/confirm 미사용
- [x] git push (아래 결과 참고)
