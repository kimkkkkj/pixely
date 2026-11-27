package com.pixely.domain.generation;

import com.pixely.domain.generation.dto.GenerationDtos.*;
import com.pixely.domain.share.ShareService;
import com.pixely.domain.share.dto.ShareDtos.ShareLinkResponse;
import com.pixely.global.common.ApiResponse;
import com.pixely.global.common.PageResponse;
import com.pixely.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generations")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;
    private final ShareService shareService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<CreatedResponse> create(@Valid @RequestBody CreateRequest request,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(generationService.create(request, principal));
    }

    @GetMapping("/{id}")
    public ApiResponse<GenerationResponse> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(generationService.get(id, principal));
    }

    @GetMapping
    public ApiResponse<PageResponse<GenerationResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "12") int size,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(generationService.listMine(principal, page, size));
    }

    @PostMapping("/{id}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VariantResponse> saveVariant(@PathVariable Long id, @Valid @RequestBody VariantRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(generationService.saveVariant(id, request, principal));
    }

    @PostMapping("/{id}/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShareLinkResponse> share(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(shareService.create(generationService.requireOwned(id, principal)));
    }
}
