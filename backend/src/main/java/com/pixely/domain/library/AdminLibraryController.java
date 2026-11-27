package com.pixely.domain.library;

import com.pixely.domain.library.dto.LibraryImageResponse;
import com.pixely.global.common.ApiResponse;
import com.pixely.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 공용 샘플 관리 (ROLE_ADMIN 전용 — SecurityConfig 에서 /api/admin/** 제한) */
@RestController
@RequestMapping("/api/admin/library")
@RequiredArgsConstructor
public class AdminLibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public ApiResponse<PageResponse<LibraryImageResponse>> list(
            @RequestParam(required = false) ImageType type,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ApiResponse.ok(libraryService.searchPublic(type, q, page, size));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LibraryImageResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam ImageType type,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tags) {
        return ApiResponse.ok(libraryService.upload(file, type, title, tags, null));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        libraryService.deletePublic(id);
        return ApiResponse.ok(null);
    }
}
