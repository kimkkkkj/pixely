package com.pixely.ai;

/** 한국어 요청사항을 광고 사진 생성용 영어 지시문으로 감싼다 */
public final class PromptBuilder {

    private PromptBuilder() {}

    public static String advertisingPrompt(String userRequest) {
        String request = userRequest == null || userRequest.isBlank() ? "(none)" : userRequest.trim();
        return """
                Create a photorealistic, high-end e-commerce advertising photo for an online marketplace listing.
                Image 1 is the PRODUCT. Image 2 is the MODEL (a person).
                Show the model naturally holding, applying or using the product, as in a professional commercial shoot.
                Keep the product exactly as it appears in image 1: same shape, colors, label text, logo and packaging.
                Keep the model's face, identity, hairstyle and skin tone consistent with image 2.
                Use soft professional studio lighting, a clean background that suits the product, sharp focus on the
                product, natural hand pose, and a balanced composition with space for marketing copy.
                Do not add any extra text, watermarks or additional logos.
                The seller's additional request is written in Korean below. Follow it faithfully unless it conflicts
                with the rules above:
                \"\"\"%s\"\"\"
                """.formatted(request);
    }
}
