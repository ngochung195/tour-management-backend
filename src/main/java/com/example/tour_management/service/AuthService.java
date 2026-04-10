package com.example.tour_management.service;

import com.example.tour_management.dto.auth.RegisterRequest;
import com.example.tour_management.dto.auth.RegisterResponse;
import com.example.tour_management.entity.User;
import com.example.tour_management.dto.auth.LoginRequest;
import com.example.tour_management.dto.auth.LoginResponse;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.repository.RoleRepository;
import com.example.tour_management.repository.UserRepository;
import com.example.tour_management.security.JwtUtil;
import com.example.tour_management.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public LoginResponse login(LoginRequest request){
        //Kiểm tra user tồn tại
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Sai email hoặc mật khẩu"));

        //Xác thực email + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        //Load userDetail để tạo JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String fullName = user.getUserName();

        String token = jwtUtil.generateToken(userDetails, fullName);

        return new LoginResponse(token);
    }

    public RegisterResponse register(RegisterRequest request) {

        // Check email trùng
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // Validate password
        if (request.getPassword().length() < 6) {
            throw new BadRequestException("Mật khẩu phải nhiều hơn hoặc bằng 6 ký tự");
        }

        // Validate phone (VN)
        if (request.getPhone() != null &&
                !request.getPhone().matches("^0\\d{9}$")) {
            throw new BadRequestException("Số điện thoại không hợp lệ");
        }

        // Tạo user
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());

        // Role mặc định
        user.setRole(roleRepository.findByRoleName ("ROLE_CUSTOMER").get());

        userRepository.save(user);

        return new RegisterResponse("Đăng ký thành công");
    }
}

