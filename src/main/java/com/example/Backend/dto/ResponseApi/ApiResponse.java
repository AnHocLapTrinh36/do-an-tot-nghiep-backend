package com.example.Backend.dto.ResponseApi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) //  Trường nào null sẽ không hiện ở JSON
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;
}
