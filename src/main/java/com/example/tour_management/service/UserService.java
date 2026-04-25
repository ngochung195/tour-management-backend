package com.example.tour_management.service;

import com.example.tour_management.dto.user.UserRequest;
import com.example.tour_management.dto.user.UserResponse;
import com.example.tour_management.entity.Role;
import com.example.tour_management.entity.User;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.RoleRepository;
import com.example.tour_management.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse getCurrentUser() {
        String email = getCurrentUserEmail();

        log.info("Lấy thông tin user hiện tại: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        return mapToResponse(user);
    }

    public UserResponse updateCurrentUser(UserRequest request) {
        String email = getCurrentUserEmail();

        log.info("Cập nhật thông tin user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        updateUserInfo(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToResponse(userRepository.save(user));
    }

    public List<UserResponse> getAll() {
        log.info("Lấy danh sách người dùng");

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Integer id) {
        log.info("Lấy user id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        return mapToResponse(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user"));
    }

    public User getUserFromAuthentication(Authentication authentication) {
        return findByEmail(authentication.getName());
    }

    public List<UserResponse> searchUsers(String name, String role) {
        log.info("Tìm kiếm user: name={}, role={}", name, role);

        List<User> users = userRepository.searchUsers(name, role);

        return users.stream().map(this::mapToResponse).toList();
    }

    public UserResponse create(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }

        String roleName = request.getRoleName();

        if (roleName == null || roleName.isBlank()) {
            roleName = "ROLE_CUSTOMER";
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò"));

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(role);

        log.info("Tạo user mới: {}", request.getEmail());

        return mapToResponse(userRepository.save(user));
    }

    public UserResponse update(Integer id, UserRequest request) {

        log.info("Cập nhật user id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        String oldRole = user.getRole().getRoleName();

        updateUserInfo(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò"));

        user.setRole(role);

        userRepository.save(user);

        String currentEmail = getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        boolean roleChanged = !oldRole.equals(role.getRoleName());
        boolean needRelogin = currentUser.getId().equals(user.getId()) && roleChanged;

        UserResponse response = mapToResponse(user);
        response.setNeedRelogin(needRelogin);

        log.info("Cập nhật user thành công, cần đăng nhập lại: {}", needRelogin);

        return response;
    }

    public void delete(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy người dùng");
        }

        userRepository.deleteById(id);

        log.info("Xóa user id={} thành công", id);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private void updateUserInfo(User user, UserRequest request) {
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
    }

    private UserResponse mapToResponse(User user) {
        UserResponse res = new UserResponse();

        res.setId(user.getId());
        res.setUserName(user.getUserName());
        res.setEmail(user.getEmail());
        res.setDateOfBirth(user.getDateOfBirth());
        res.setPhone(user.getPhone());
        res.setAddress(user.getAddress());
        res.setRoleName(user.getRole().getRoleName());

        return res;
    }
}