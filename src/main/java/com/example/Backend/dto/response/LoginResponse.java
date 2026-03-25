package com.example.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) //trường nào null bỏ qua
public class LoginResponse {
    @JsonProperty("access_token")
    private String accessToken;
    private String email;
    @JsonProperty("full_name")
    private String fullName;
    // @JsonProperty("avatar_url")
    //private String avatarUrl;
    private String role;
}
