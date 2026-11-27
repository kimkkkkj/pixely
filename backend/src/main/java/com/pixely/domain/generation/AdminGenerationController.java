package com.pixely.domain.generation;

import com.pixely.domain.generation.dto.GenerationDtos.GenerationResponse;
import com.pixely.global.common.ApiResponse;
import com.pixely.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/generations")
@RequiredArgsConstructor
public class AdminGenerationController {

    private final GenerationService generationService;

    @GetMapping
    public ApiResponse<PageResponse<GenerationResponse>> list(@RequestParam(required = false) GenerationStatus status,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(generationService.listAll(status, page, size));
    }
}
