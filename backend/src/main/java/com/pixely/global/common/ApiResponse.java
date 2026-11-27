package com.pixely.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** 모든 API 의 공통 응답 포맷: { success, data, code, message, errors } */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, String code, String message, List<FieldError> errors) {

    public record FieldError(String field, String message) {}

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, null);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, code, message, null);
    }

    public static ApiResponse<Void> error(String code, String message, List<FieldError> errors) {
        return new ApiResponse<>(false, null, code, message, errors);
    }
}
