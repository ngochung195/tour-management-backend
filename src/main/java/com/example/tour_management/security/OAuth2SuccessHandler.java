package com.example.tour_management.security;

import com.example.tour_management.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtUtil jwtUtil,
                                UserDetailsService userDetailsService,
                                UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        // KHÔNG CÓ EMAIL
        if (email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write("""
            {
              "status": 401,
              "error": "Unauthorized",
              "message": "Tài khoản Google không có email"
            }
            """);
            return;
        }

        // ĐÃ CÓ USER → LOGIN
        if (userRepository.existsByEmail(email)) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            String fullName = oAuth2User.getAttribute("name");
            String token = jwtUtil.generateToken(userDetails, fullName);

            response.sendRedirect(
                    "http://localhost:4200/login-success?token=" + token
            );
            return;
        }

        // CHƯA CÓ USER → REGISTER
        String name = oAuth2User.getAttribute("name");

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedName = URLEncoder.encode(name != null ? name : "", StandardCharsets.UTF_8);

        response.sendRedirect(
                "http://localhost:4200/register?email=" + encodedEmail + "&name=" + encodedName
        );
    }
}