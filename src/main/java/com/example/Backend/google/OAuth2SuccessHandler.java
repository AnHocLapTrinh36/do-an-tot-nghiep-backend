package com.example.Backend.google;


import com.example.Backend.entity.Provider;
import com.example.Backend.entity.RoleType;
import com.example.Backend.entity.User;
import com.example.Backend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.io.IOException;

@Component
@RequiredArgsConstructor
/// xử lý khi người dùng đăng nhập thành công bằng Google
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService; //inject -> create jwt
    //  private final UserService userService;
    @Override
    /// spring securiy sẽ tự động gọi khi login gg thành công
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        /// lấy dữ liệu từ google (***OidcUser)
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); //oAuth2User chứa attributes

        String email = oAuth2User.getAttribute("email");
        User tempUser = User.builder()
                .email(email)
                .role(RoleType.STUDENT)   // Google mặc định STUDENT
                .provider(Provider.GOOGLE)
                .build();
        // generate JWT
        String token = jwtService.generateToken(tempUser);

        response.sendRedirect("http://localhost:5173/oauth2-success?token=" + token);
    }
}