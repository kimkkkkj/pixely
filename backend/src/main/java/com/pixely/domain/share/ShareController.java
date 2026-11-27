package com.pixely.domain.share;

import com.pixely.domain.share.dto.ShareDtos.SharedGenerationResponse;
import com.pixely.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 비로그인 공개 조회 */
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @GetMapping("/{token}")
    public ApiResponse<SharedGenerationResponse> get(@PathVariable String token) {
        return ApiResponse.ok(shareService.getPublic(token));
    }
}
