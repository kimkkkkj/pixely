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
- [ ] git push — 실패: `remote: Repository not found` (원격 저장소 미생성 또는 접근 권한 없음). 저장소 생성/로그인 후 `git push -u origin main` 수동 실행 필요

---

# 2차 작업: _legacy 원본 반영

## 0) `_legacy` 구성 확인
| 경로 | 내용 | 처리 |
|---|---|---|
| `service.html` | 스튜디오 시안 (1158줄, 인라인 CSS/JS) | 레이아웃·문구·상호작용 비교 후 반영 |
| `css/` | bootstrap*, aos, glightbox, tiny-slider, `style.css`(템플릿: hero, feature, service, section-counter, testimonial, pricing, contact/subscribe, playvid, footer, loader) | 메인 섹션 구성 점검 기준 |
| `js/` | aos, bootstrap.bundle, counter, custom(AOS·Rellax·preloader·countdown·tiny-slider·GLightbox), navbar(모바일 메뉴), rellax, tiny-slider, glightbox | 이미 React 훅/컴포넌트로 이식됨 |
| `images/` | `main_logo.png`(흰 글자), `main_logo_b.png`(검은 글자), `pa.png`(로고 마크), `main-ai-visual-2.png`(AI 웨이브) | 로고 2종·웨이브 비주얼 → `frontend/src/assets/brand` |
| `pavi/` | 파비콘·앱 아이콘 세트, manifest | favicon.ico, 16/32/96, apple 180, android 192 → `frontend/public` |
| `service-images/` | logo/logo2, alram/alram2, profile, search, robot, `F1~F6.gif`, `tab1`(상품 14), `tab2`(인물 14) | profile → 기본 아바타, robot → CTA 마스코트(360px로 축소), tab1 → 백엔드 상품 시드. **tab2·F*.gif(인물) 미사용** |
| `video/` | back1·back2·bg1·bg2·bg3.mp4 (총 약 300MB) | 미커밋. `VITE_HERO_VIDEO` 자리만 마련 |
| 기획서 PDF | **없음** | 비교 불가 (DECISIONS.md) |
| 메인 원본 `index.html` | **없음** | `style.css` 템플릿 클래스 구조로 대체 점검 |

## 1) 원본 대비 비교
| 영역 | 원본 시안 | 1차 구현 | 2차 조치 |
|---|---|---|---|
| 로고 | `logo2.png` 이미지 (보라→파랑 마크 + PIXELY) | 직접 그린 SVG 4칸 마크 | **원본 로고 이미지로 교체** (다크/라이트 2종), 원본 파비콘·manifest 적용 |
| 헤더 | 로고 / 알림 아이콘 / 프로필 사진 + 이름 | 로고 / 메뉴 / 닉네임 텍스트 | **프로필 아바타 + 닉네임** 추가. 알림 아이콘은 알림 기능이 없어 미반영 |
| 주 버튼 색 | `#574CE3 → #2E8CF0 → #574CE3`, hover 시 배경 이동 | 파랑 단색 계열 그라데이션 | **원본 그라데이션 + hover 이동**을 primary 토큰으로 지정 |
| 탭 | 상품 이미지 / 연예인 이미지 | 상품 이미지 / 모델 이미지 | 구조는 같음. "연예인"은 초상권 문제로 **"모델" 유지** |
| 검색창 | 검정 pill, 우측 돋보기, "찾으시는 목록이 있으신가요?" | 좌측 아이콘 사각 입력 | **원본 형태·문구로 변경** (+ 디바운스 검색은 유지) |
| 그리드 | 2열, 드래그 스크롤(첫 그리드만 동작) | 2열, 모든 그리드 드래그 + 무한 스크롤 | 유지 (태블릿에서는 자동 열 수) |
| 우측 상단 | 공유하기(#3c6593) / 다운로드(#0377FF) 반반 버튼 | 미리보기 헤더 우측의 작은 버튼 | **원본 위치·색으로 이동** (데스크톱 우측 패널 상단, 태블릿 이하는 결과 헤더) |
| 슬롯 문구 | "상품 이미지를 / 넣어주세요", "연예인 이미지를 / 넣어주세요" | "+ 상품 선택" + 라벨 | **원본 문구** ("모델 이미지를 넣어주세요") |
| 슬롯 해제 | confirm "이미지를 삭제하시겠어요? Y/N" | 커스텀 모달 "선택을 해제할까요?" | 모달 제목을 **원본 문구**로 |
| 슬롯 중복 선택 | alert "한 장의 사진만 선택할 수 있습니다." 로 차단 | 조용히 교체 | 교체하고 **토스트로 안내** (차단보다 편해서 의도적으로 다르게 둠) |
| 요청사항 | "여기에 요청사항을 적어보세요!", 포커스 시 움직이는 그라데이션 테두리 | 일반 textarea | **원본 placeholder + 움직이는 그라데이션 테두리** |
| 생성 버튼 | 60px, 보라-파랑 그라데이션 | 52px primary | **60px(`xl`) + 원본 그라데이션** |
| 미리보기 빈 상태 | "이미지 생성하기 / 버튼을 눌러주세요." | 아이콘 + 긴 안내문 | **원본 문구** |
| 로딩 | 8px 볼 5개(초록→청록→하늘→파랑→보라), ±20px 웨이브 1.5s, -45° 움직이는 그라데이션 텍스트 | 14px 볼, 브랜드 4색 + 흰색 | **원본 크기·색·타이밍**으로 |
| 필터 바 | "필터적용", 142px 정사각 타일, 텍스트 오버레이, 비선택 어둡게, 스크롤바 숨김, 생성 전 비활성 "필터 없음" | 84px 썸네일 + 아래 라벨 | **원본 구조로** (유동 88~142px, 오버레이 라벨, 비선택 50% 밝기, 생성 전 "필터 없음") |
| 필터 동작 | GIF를 바꿔 끼우는 가짜 필터 | Canvas 픽셀 필터 | 유지 (원본 결함 해결) |
| 공유 | 버튼만 있고 동작 없음 | 공유 링크/클립보드/navigator.share | 유지 |
| 메인 히어로 | 템플릿 hero + 배경 영상(video/) | 그라데이션 orb | **AI 웨이브 비주얼 배경 + `VITE_HERO_VIDEO` 영상 자리 + 소개 영상 모달(playvid)** |
| feature/service 카드 | 아이콘 카드, hover 시 lift·그라데이션 | 정적 카드 | **hover lift + 상단 그라데이션 바** |
| counter | `.section-counter` | `useCountUp` | 유지 |
| testimonial (tiny-slider) | 후기 슬라이더 | 비포/애프터 Swiper | 가짜 후기를 만들지 않으려고 **비포/애프터로 대체 유지** |
| pricing / contact·subscribe | 템플릿 섹션 | 없음 | **미반영** (요금·문의 정책 정보 없음) |
| preloader | `.loader` 페이드아웃 | Suspense 로딩 화면 | 유지 |
| 마스코트 | robot.png (시안에서 미사용) | 없음 | **CTA 배너에 사용** |

## 2) DB
- [x] `.env` 정보로 로컬 MySQL 3306 접속 시도 → pixely / root / sa 모두 `ERROR 1045 Access denied` → **H2 유지**, README 7장에 원인과 전환 방법

## 3) 정리
- [x] `src/Main.java`, `pixely.iml` 삭제, `.gitignore`의 `/src/` 규칙 삭제 및 깨진 주석 수정
- [x] Gradle toolchain 17(foojay 자동 다운로드) + `gradlew.bat` JDK 자동 탐색 → **JAVA_HOME과 PATH에 java가 없는 상태에서 `gradlew build` 성공** 확인
- [x] 상품 시드 14장(classpath), 가상 모델 8장

## 4) 검증
- [x] `gradlew clean build` — 테스트 18/18 통과 (H2 test 프로필)
- [x] `npm run lint` 경고 0, `npm run build` 성공
- [x] 브라우저 흐름(puppeteer-core + Chrome headless): 회원가입 → 상품/모델 선택 → 생성(mock) → 로맨틱 필터 → 다운로드(`pixely-2-romantic.png`) → 공유 링크 공개 페이지(원본/로맨틱) → 히스토리 → 다시 편집 → 로그인 모달 → 관리자 샘플 업로드(15→16), 콘솔 에러·5xx 0건
- [x] 1440 / 1024 / 768 / 390 화면 확인
- 발견·수정: 우측 상단 토스트가 새 공유/다운로드 버튼을 가림 → 토스트를 상단 중앙으로 이동. 태블릿에서 라이브러리 타일·슬롯이 과도하게 커짐 → 자동 열 수 그리드, 슬롯 최대 200px
