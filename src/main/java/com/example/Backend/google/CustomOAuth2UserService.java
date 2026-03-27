package com.example.Backend.google;


import com.example.Backend.entity.Provider;
import com.example.Backend.entity.RoleType;
import com.example.Backend.entity.Student;
import com.example.Backend.entity.User;
import com.example.Backend.repository.StudentRepository;
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
    private final StudentRepository studentRepository;

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

        // handle user data
        User user = userRepository
                .findByProviderAndProviderId(Provider.GOOGLE, sub)
                .orElse(null);

        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                // merge account LOCAL -> GOOGLE
                user.setProvider(Provider.GOOGLE);
                user.setProviderId(sub);
                user.setEmailVerified(true);
            } else {
                // tạo mới user
                user = User.builder()
                        .email(email)
                        .fullName(name)
                        .role(RoleType.STUDENT)
                        .provider(Provider.GOOGLE)
                        .providerId(sub)
                        .password(null)
                        .emailVerified(true)
                        .isActive(true)
                        .build();

                user = userRepository.save(user);
            }
        }

        Student student = studentRepository.findByUser(user).orElse(null);

        if (student == null) {
            student = new Student();
            student.setUser(user);
            user.setStudent(student);
        }

        // update dữ liệu
        student.setAvatarUrl(picture);
        user.setFullName(name);

        // save
        studentRepository.save(student);
        userRepository.save(user);
        return oAuth2User;
    }
}
