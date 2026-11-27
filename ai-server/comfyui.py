"""
ComfyUI 연동 (확장 지점)

ComfyUI 기본 HTTP API 사용:
  1) POST /upload/image         — 상품/모델 이미지를 input 폴더로 업로드
  2) POST /prompt               — 워크플로우(API 포맷 JSON) 제출
  3) GET  /history/{prompt_id}  — 완료될 때까지 polling
  4) GET  /view?filename=...    — 결과 이미지 다운로드

환경변수
  COMFYUI_URL       : 예) http://127.0.0.1:8188
  COMFYUI_WORKFLOW  : ComfyUI 에서 "Save (API Format)" 으로 내보낸 JSON 경로.
                      JSON 안의 문자열 "{{PRODUCT_IMAGE}}", "{{MODEL_IMAGE}}", "{{PROMPT}}" 를 치환한다.
"""
from __future__ import annotations

import asyncio
import json
import os
import uuid
from pathlib import Path

import httpx

TIMEOUT = float(os.getenv("AI_TIMEOUT_SECONDS", "60"))


def _fill(node, mapping: dict[str, str]):
    if isinstance(node, dict):
        return {k: _fill(v, mapping) for k, v in node.items()}
    if isinstance(node, list):
        return [_fill(v, mapping) for v in node]
    if isinstance(node, str):
        for key, value in mapping.items():
            node = node.replace(key, value)
    return node


async def run_workflow(product: bytes, model: bytes, prompt: str, system_prompt: str) -> bytes:
    base = os.getenv("COMFYUI_URL")
    workflow_path = os.getenv("COMFYUI_WORKFLOW")
    if not base or not workflow_path:
        raise RuntimeError("AI_ENGINE=comfyui 이지만 COMFYUI_URL / COMFYUI_WORKFLOW 가 설정되지 않았습니다.")
    workflow = json.loads(Path(workflow_path).read_text(encoding="utf-8"))

    async with httpx.AsyncClient(base_url=base, timeout=TIMEOUT) as client:
        names = {}
        for key, data in (("product", product), ("model", model)):
            filename = f"pixely_{key}_{uuid.uuid4().hex}.png"
            r = await client.post("/upload/image", files={"image": (filename, data, "image/png")}, data={"overwrite": "true"})
            r.raise_for_status()
            names[key] = r.json()["name"]

        graph = _fill(workflow, {
            "{{PRODUCT_IMAGE}}": names["product"],
            "{{MODEL_IMAGE}}": names["model"],
            "{{PROMPT}}": (system_prompt or prompt).replace('"', "'"),
        })
        r = await client.post("/prompt", json={"prompt": graph, "client_id": "pixely"})
        r.raise_for_status()
        prompt_id = r.json()["prompt_id"]

        deadline = asyncio.get_event_loop().time() + TIMEOUT
        while asyncio.get_event_loop().time() < deadline:
            await asyncio.sleep(1.0)
            h = (await client.get(f"/history/{prompt_id}")).json()
            if prompt_id not in h:
                continue
            for output in h[prompt_id].get("outputs", {}).values():
                for image in output.get("images", []):
                    v = await client.get("/view", params={
                        "filename": image["filename"], "subfolder": image.get("subfolder", ""), "type": image.get("type", "output"),
                    })
                    v.raise_for_status()
                    return v.content
            raise RuntimeError("ComfyUI 워크플로우에 이미지 출력이 없습니다.")
        raise TimeoutError("ComfyUI 응답 시간이 초과되었습니다.")
