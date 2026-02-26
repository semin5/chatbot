## AI 챗봇 API 서버 (GPT 연동)

- 웹, 모바일, 데스크톱 등 다양한 클라이언트에서 사용할 수 있는 범용 AI 챗봇 REST API 서버입니다. GPT API를 연동하여 대화를 처리하고, 대화 이력을 저장/관리하며, API Key 기반 인증과 스트리밍 응답을 지원합니다. Swagger를 통한 완성도 높은 API 문서를 제공합니다
---
## 기술 스택
- Spring Boot 3.2.12, Java 21
- Spring Security
- Gradle
- PostgreSQL, Redis
- Spring WebFlux
- Spring Data JPA
- Docker
- Railway
- Swagger (OpenAPI 3)
---
## 주요 기능

- Intelligent Conversation: OpenAI GPT API 기반의 문맥 인지 대화
- SSE Streaming: `text/event-stream` 기반 실시간 토큰 전송
- Rate Limiting: Redis 기반 실시간 트래픽 제어 (DoS 방지 및 비용 최적화)
- Security: API Key 기반 인증 및 필터 기반 로깅 시스템 (MDC 추적)
- Robust Persistence: PostgreSQL 기반 대화 이력 및 컨텍스트 관리 (기본 최근 10개 메시지 유지, 설정으로 조정 가능)
---
## 배포 아키텍처
- Railway: Spring Boot API 서버 배포
- PostgreSQL: 대화/메시지 영속화
- Redis: Rate Limiting 및 캐시/임시 데이터
---
##  실시간 서비스 확인
*   헬스 체크: [https://chatbot-production-337e.up.railway.app/health](https://chatbot-production-337e.up.railway.app/health)
    * `{"status": "up", ...}` 이면 정상
*   Swagger: [https://chatbot-production-337e.up.railway.app/swagger-ui/index.html#/](https://chatbot-production-337e.up.railway.app/swagger-ui/index.html#/)
---
## 엔드포인트
- `POST /api/chat/completions`: 채팅 메시지 전송
- `POST /api/chat/completions/stream`: 스트리밍 메시지 전송
- `GET /api/conversations`: 전체 채팅 조회
- `GET /api/conversations/{id}/messages`: 특정 채팅 및 메시지 조회
- `DELETE /api/convertsations/{id}`: 특정 채팅 삭제
- `GET /health`: 헬스 체크
- `GET /swagger-ui.html`: swagger 문서
---
## 인증 방식(API Key)
모든 API 호출 시 헤더에 서비스 등록된 X-API-Key를 포함해야 하며 Swagger에서 테스트할 때도 동일하게 헤더를 넣어야 합니다.
- X-API-Key: <YOUR_API_KEY>
---
## 환경 변수
- OPENAI
- POSTGRE_URL
- POSTGRE_USERNAME
- POSTGRE_PASSWORD
- REDIS_HOST
- REDIS_PORT
- REDIS_PASSWORD

## docker 개발환경 구축
```bash
# 1. 소스코드 복제 및 .env.docker 준비
git clone https://github.com/semin5/chatbot.git

# 실행
./scripts/run.sh
# 또는 Windows
./scripts/run.bat

# 종료
./scripts/run.sh
# 또는 Windows
./scripts/run.bat
```