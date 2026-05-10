package com.example.Backend.google;


import com.example.Backend.entity.Provider;
import com.example.Backend.entity.RoleType;
import com.example.Backend.entity.Student;
import com.example.Backend.entity.User;
import com.example.Backend.repository.StudentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.security.JwtService;
import com.example.Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    @Override
    /// spring securiy sẽ tự động gọi khi login gg thành công
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        /// lấy dữ liệu từ google (***OidcUser)
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); //oAuth2User chứa attributes

        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user sau khi xác thực Google"));

        userRepository.save(user);
        String token = jwtService.generateToken(user);

        response.sendRedirect(frontendUrl + "/oauth2-success?token=" + token);
    }
}
