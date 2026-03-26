package com.example.Backend.dto.apiResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String message;
    private T data;           // Thành công: dữ liệu | Lỗi: thông tin lỗi
    private int code;

    // Thành công - không có data
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .code(200)
                .build();
    }

    // Thành công - có data
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .code(200)
                .build();
    }

    // Thành công với status code khác (ví dụ: 201 Created)
    public static <T> ApiResponse<T> success(String message, T data, int code) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .code(code)
                .build();
    }

    // Lỗi - chỉ message
    public static <T> ApiResponse<T> error(String message, int code) {
        return ApiResponse.<T>builder()
                .message(message)
                .code(code)
                .build();
    }

    // Lỗi - có chi tiết lỗi trong data
    public static <T> ApiResponse<T> error(String message, T errorData, int code) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(errorData)      // lỗi nằm trong data
                .code(code)
                .build();
    }
}
