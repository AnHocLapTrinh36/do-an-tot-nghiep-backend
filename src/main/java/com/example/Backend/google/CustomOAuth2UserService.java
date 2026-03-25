package com.example.Backend.google;


import com.example.Backend.entity.Provider;
import com.example.Backend.entity.RoleType;
import com.example.Backend.entity.User;
import com.example.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String sub = oAuth2User.getAttribute("sub");

        if (email == null || sub == null) {
            throw new RuntimeException("Không lấy được email hoặc sub từ Google");
        }

        //  Tìm theo Google provider + sub (ưu tiên)
        Optional<User> existingByGoogle = userRepository.findByProviderAndProviderId(
                Provider.GOOGLE, sub);

        User user;

        if (existingByGoogle.isPresent()) {
            // Đã có tài khoản Google không cần update gì thêm
            user = existingByGoogle.get();
            log.info("Found existing Google user: {}", email);
        } else {
            // Không tìm thấy theo Google → check theo email để merge
            Optional<User> existingByEmail = userRepository.findByEmail(email);

            if (existingByEmail.isPresent()) {
                user = existingByEmail.get();
                // Merge: link Google vào tài khoản cũ
                user.setProvider(Provider.GOOGLE);
                user.setProviderId(sub);
                user.setEmailVerified(true);  // Google đã verify

                // Optional: nếu muốn update name/avatar
                // user.setFullName(name);
                // user.setAvatarUrl(picture);

                userRepository.save(user);
                log.info("Merged Google to existing LOCAL user: {}", email);
            } else {
                // Tạo mới hoàn toàn
                user = User.builder()
                        .email(email)
                        .fullName(name)
                        .avatarUrl(picture)
                        .role(RoleType.STUDENT)
                        .provider(Provider.GOOGLE)
                        .providerId(sub)
                        .password(null)
                        .emailVerified(true)
                        .isActive(true)
                        .build();
                user = userRepository.save(user);
                log.info("Created new Google user: {}", email);
            }
        }

        return oAuth2User;
    }
}
