package com.example.Backend.dto.apiResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private int statusCode;

    // Thành công - không có data
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .statusCode(200)
                .build();
    }

    // Thành công - có data
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .statusCode(200)
                .build();
    }

    // Thành công với status code khác (ví dụ: 201 Created)
    public static <T> ApiResponse<T> success(String message, T data, int statusCode) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .build();
    }

    // Lỗi - chỉ message
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .message(message)
                .statusCode(statusCode)
                .build();
    }

    // Lỗi - có chi tiết lỗi trong data
    public static <T> ApiResponse<T> error(String message, T errorData, int statusCode) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(errorData)      // lỗi nằm trong data
                .statusCode(statusCode)
                .build();
    }
}
