package com.example.Backend.security;

import com.example.Backend.google.CustomOAuth2UserService;
import com.example.Backend.google.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOriginPatterns(List.of("*"));
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    configuration.setAllowedHeaders(List.of("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setExposedHeaders(List.of("Authorization"));
                    configuration.setMaxAge(3600L);
                    return configuration;
                }))

                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/cvs/{id}/preview",
                                "/api/students/{id}",
                                "/api/projects/**",
                                "/api/cvs/public/**",
                                "/api/companies",
                                "/api/companies/{id}",
                                "/api/auth/register",
                                "/api/auth/user/**",
                                "/api/auth/login",
                                "/api/auth/verify",
                                "/api/auth/verify/**",
                                "/oauth2/**",
                                "/login/oauth2/code/**",
                                "/error",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()
                        //.requestMatchers("/api/student/{mssv}").hasAnyRole("ADMIN", "TEACHER")
                        .anyRequest().authenticated()
                )

                // Thêm JWT Filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 Login Google
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(user -> user
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                // Trả về JSON khi chưa đăng nhập
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json;charset=UTF-8");
                            String json = """
                                    {"error": "Unauthorized", 
                                     "message": "Vui lòng đăng nhập để truy cập tài nguyên này"}
                                    """;
                            response.getWriter().write(json);
                        })
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}