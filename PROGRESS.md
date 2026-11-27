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
