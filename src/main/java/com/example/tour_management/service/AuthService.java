package com.example.tour_management.service;

import com.example.tour_management.dto.auth.*;
import com.example.tour_management.entity.User;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.repository.RoleRepository;
import com.example.tour_management.repository.UserRepository;
import com.example.tour_management.security.JwtUtil;
import com.example.tour_management.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsServiceImpl userDetailsService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public LoginResponse login(LoginRequest request){

        log.info("Yêu cầu đăng nhập với email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Email không tồn tại: {}", request.getEmail());
                    return new BadCredentialsException("Sai email hoặc mật khẩu");
                });

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            log.warn("Sai mật khẩu cho email: {}", request.getEmail());
            throw new BadCredentialsException("Sai email hoặc mật khẩu");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getEmail());

        String fullName = user.getUserName();

        String token = jwtUtil.generateToken(userDetails, fullName);

        log.info("Đăng nhập thành công: {}", request.getEmail());

        return new LoginResponse(token);
    }

    public RegisterResponse register(RegisterRequest request) {

        log.info("Yêu cầu đăng ký với email: {}", request.getEmail());

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email không được để trống");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Email đã tồn tại: {}", request.getEmail());
            throw new BadRequestException("Email đã được sử dụng");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("Mật khẩu phải >= 6 ký tự");
        }

        if (request.getPhone() != null &&
                !request.getPhone().matches("^0\\d{9}$")) {
            throw new BadRequestException("Số điện thoại không hợp lệ");
        }

        var role = roleRepository.findByRoleName("ROLE_CUSTOMER")
                .orElseThrow(() -> {
                    log.error("Không tìm thấy ROLE_CUSTOMER trong DB");
                    return new RuntimeException("Role không tồn tại");
                });

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setRole(role);

        userRepository.save(user);

        log.info("Đăng ký thành công: {}", request.getEmail());

        return new RegisterResponse("Đăng ký thành công");
    }
}