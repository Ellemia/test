# 오늘 뭐 먹지? (Mood → Food)

기분 기반 음식/레시피 추천 서비스. Spring Boot(백엔드) + React/TS/Vite/Tailwind(프론트엔드) + PostgreSQL.

## 구조

```
backend/   Spring Boot 3.3 (Java 21) API 서버
frontend/  React 19 + TypeScript + Vite + Tailwind v4 SPA
```

## 핵심 설계 원칙

- **추천 로직과 레시피 데이터 분리**: 메뉴별 if/else 하드코딩 없이, 레시피마다 매겨진
  24개 점수(맛 8 + 기분 8 + 식감 8)를 가중치 공식으로 채점하는 규칙 기반 엔진
  (`RecommendationEngine`, `ALGORITHM_VERSION = "v1"`). AI/LLM은 핵심 추천 알고리즘에
  관여하지 않는다.
- **출처 추적(provenance-first)**: 모든 레시피는 `source_type`/`source_name`/`source_url`/
  `license`/`attribution_required`/`retrieved_at`을 필수로 가진다. 무단 스크래핑 없음.
- **원시 데이터 스테이징**: 외부 소스에서 가져온 레시피는 먼저 `recipe_imports`에 원본
  JSON으로 저장된 뒤, 정규화 → 중복 탐지 → 재료 표준화 → 규칙 기반 태깅 → 관리자 검수를
  거쳐야 `PUBLISHED` 상태가 되고 추천 대상이 된다.
- **비회원 익명 세션**: 강제 회원가입 없이 클라이언트가 생성한 UUID를 `X-Session-Id`
  헤더로 보내 세션을 식별한다.

## 데이터 수집 현황

TheMealDB, 식품안전나라 조리식품 레시피(COOKRCP01), 농림축산식품 공공데이터,
USDA FoodData Central의 라이선스 조건을 조사했으나(§Phase 1), 현재 개발 샌드박스는
일반 외부 도메인으로의 아웃바운드 네트워크 접근이 차단되어 있어 실시간 수집을
실행할 수 없었다. 대신 `SeedDataRunner`가 자체 작성한 한식 가정식 레시피 8종을
`source_type=OWN_RECIPE`로 수집 파이프라인(`RecipeCollectorService.ingestOwn`)을 통해
동일하게 스테이징 → 정규화 → 태깅 → 발행 과정을 거쳐 등록한다. 실제 네트워크 접근이
가능한 환경에서는 `collector/adapter/TheMealDbAdapter`처럼 소스별 어댑터를 추가해
`POST /api/admin/import`로 실데이터 수집을 재개할 수 있다.

## 로컬 실행

### 백엔드

```bash
cd backend
# PostgreSQL 16, DB moodfood / user moodfood 필요 (application.yml 참고)
mvn spring-boot:run
```

첫 실행 시 `moodfood.seed.enabled=true`이면 자체 작성 레시피 8종이 자동 발행된다.
API는 기본 `http://localhost:8090`.

### 프론트엔드

```bash
cd frontend
npm install
npm run dev
```

기본 `http://localhost:5174`. `VITE_API_BASE_URL` 환경변수로 백엔드 주소를 바꿀 수 있다
(기본값 `http://localhost:8090`).

## 화면 흐름

1. **홈** — 기분/원하는 맛(필수) + 상황/조리 의향(선택) 선택
2. **추천 결과** — 가중치 채점 결과 1개 추천 + 이유 문장, 좋아요/별로예요/만들어볼래요
   피드백, "다른 메뉴 보여주세요"(리롤, 최근 노출 이력 기반 novelty 페널티 적용)
3. **레시피 상세** — 재료/조리 순서/영양정보/출처·라이선스, 즐겨찾기 토글
