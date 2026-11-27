package com.pixely.domain.library;

import com.pixely.domain.library.dto.LibraryImageResponse;
import com.pixely.global.common.ApiResponse;
import com.pixely.global.common.PageResponse;
import com.pixely.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public ApiResponse<PageResponse<LibraryImageResponse>> list(
            @RequestParam ImageType type,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long viewerId = principal == null ? null : principal.id();
        return ApiResponse.ok(libraryService.search(type, q, page, size, viewerId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LibraryImageResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam ImageType type,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tags,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(libraryService.upload(file, type, title, tags, principal.id()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        libraryService.deleteOwn(id, principal);
        return ApiResponse.ok(null);
    }
}
