"""
Pixely ai-server (선택 모듈)

백엔드의 AI_PROVIDER=fastapi 일 때 호출되는 이미지 생성 서버.
- POST /generate  (multipart: product_image, model_image, prompt, system_prompt) -> image/png
- GET  /health

엔진은 환경변수 AI_ENGINE 으로 선택:
- mock (기본)  : Pillow 로 모델 이미지 위에 상품을 배치하고 요청사항 키워드로 톤 보정
- comfyui      : ComfyUI 서버(COMFYUI_URL)에 워크플로우(COMFYUI_WORKFLOW)를 제출 (기획서의 ComfyUI 구조 확장 지점)
"""
from __future__ import annotations

import io
import logging
import os
from pathlib import Path

from dotenv import load_dotenv
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.responses import Response

from compose import compose_mock

# 루트 .env 공유
load_dotenv(Path(__file__).resolve().parent.parent / ".env")

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s - %(message)s")
log = logging.getLogger("pixely.ai")

ENGINE = os.getenv("AI_ENGINE", "mock").strip().lower()
MAX_BYTES = 15 * 1024 * 1024

app = FastAPI(title="Pixely ai-server", version="0.1.0")


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "engine": ENGINE}


async def _read(upload: UploadFile, name: str) -> bytes:
    data = await upload.read()
    if not data:
        raise HTTPException(status_code=400, detail=f"{name} 이(가) 비어 있습니다.")
    if len(data) > MAX_BYTES:
        raise HTTPException(status_code=413, detail=f"{name} 이(가) 너무 큽니다.")
    return data


@app.post("/generate", responses={200: {"content": {"image/png": {}}}})
async def generate(
    product_image: UploadFile = File(...),
    model_image: UploadFile = File(...),
    prompt: str = Form(""),
    system_prompt: str = Form(""),
) -> Response:
    product = await _read(product_image, "product_image")
    model = await _read(model_image, "model_image")
    log.info("generate engine=%s prompt=%r", ENGINE, prompt[:80])
    try:
        if ENGINE == "comfyui":
            from comfyui import run_workflow  # 지연 로딩: mock 사용 시 httpx 의존 없음

            png = await run_workflow(product, model, prompt, system_prompt)
        else:
            png = compose_mock(product, model, prompt)
    except HTTPException:
        raise
    except Exception as e:  # noqa: BLE001 - 사용자에게 보여줄 메시지로 변환
        log.exception("generation failed")
        raise HTTPException(status_code=500, detail=f"이미지 생성 실패: {e}") from e
    return Response(content=png, media_type="image/png")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("main:app", host="0.0.0.0", port=int(os.getenv("AI_SERVER_PORT", "8000")), reload=False)
