package com.pixely.ai;

/**
 * 상품 이미지 + 모델 이미지 + 요청사항 → 광고 합성 이미지(bytes, PNG/JPEG)
 * 구현체: mock(기본), openai, fastapi — 환경변수 AI_PROVIDER 로 선택
 */
public interface ImageGenerationProvider {

    String name();

    byte[] generate(byte[] productImage, byte[] modelImage, String prompt) throws Exception;
}
