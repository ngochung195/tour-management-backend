package com.example.tour_management.service;

import com.example.tour_management.entity.User;
import com.example.tour_management.dto.auth.LoginRequest;
import com.example.tour_management.dto.auth.LoginResponse;
import com.example.tour_management.dto.user.UserResponse;
import com.example.tour_management.repository.UserRepository;
import com.example.tour_management.security.JwtUtil;
import com.example.tour_management.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    public LoginResponse login(LoginRequest request){
        //Kiểm tra user tồn tại
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Xác thực email + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        //Load userDetail để tạo JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String token = jwtUtil.generateToken(userDetails);

        String role = user.getRole().getRoleName();

        return new LoginResponse(token, role);
    }
}

