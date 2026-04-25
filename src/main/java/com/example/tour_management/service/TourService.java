package com.example.tour_management.service;

import com.example.tour_management.dto.tour.TourRequest;
import com.example.tour_management.dto.tour.TourResponse;
import com.example.tour_management.entity.*;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TourService {

    private static final Logger log = LoggerFactory.getLogger(TourService.class);

    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final VehicleRepository vehicleRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public TourService(TourRepository tourRepository,
                       CategoryRepository categoryRepository,
                       HotelRepository hotelRepository,
                       VehicleRepository vehicleRepository,
                       UserRepository userRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       ObjectMapper objectMapper) {
        this.tourRepository = tourRepository;
        this.categoryRepository = categoryRepository;
        this.hotelRepository = hotelRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
            return null;
        }

        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public List<TourResponse> getAll() {

        User user = getCurrentUser();

        log.info("=== getAll() called ===");
        log.info("Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        log.info("User: {}", user == null ? "NULL" : user.getEmail());
        if (user != null) {
            log.info("Role từ DB: '{}'", user.getRole().getRoleName());
        }

        if (user == null) {
            return getAllPublic();
        }

        String role = user.getRole().getRoleName();

        if ("ROLE_ADMIN".equals(role)) {
            return tourRepository.findAll()
                    .stream().map(this::mapToResponse).toList();
        }

        if ("ROLE_MANAGER".equals(role)) {
            return tourRepository.findByManagerId(user.getId())
                    .stream().map(this::mapToResponse).toList();
        }

        return getAllPublic();
    }

    public List<TourResponse> getAllPublic() {
        return tourRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TourResponse getById(Integer id) {
        String key = "tour:" + id;

        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return objectMapper.convertValue(cached, TourResponse.class);
        }

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        TourResponse res = mapToResponse(tour);

        redisTemplate.opsForValue().set(key, res, 10, TimeUnit.MINUTES);
        return res;
    }

    public TourResponse create(TourRequest request) {

        User currentUser = getCurrentUser();

        if (currentUser == null) {
            throw new BadRequestException("Chưa đăng nhập");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));

        validateDate(request.getStartDate(), request.getEndDate());

        Tour tour = new Tour();
        tour.setTourName(request.getTourName());
        tour.setPrice(request.getPrice());
        tour.setQuantity(request.getQuantity());
        tour.setDescription(request.getDescription());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setCategory(category);

        if ("ROLE_MANAGER".equals(currentUser.getRole().getRoleName())) {
            tour.setManagerId(currentUser.getId());
        } else if ("ROLE_ADMIN".equals(currentUser.getRole().getRoleName())) {
            tour.setManagerId(request.getManagerId());
        } else {
            throw new BadRequestException("Không có quyền tạo tour");
        }

        handleUploadImage(request, tour);

        redisTemplate.delete("tours");

        return mapToResponse(tourRepository.save(tour));
    }

    public TourResponse update(Integer id, TourRequest request) {

        User currentUser = getCurrentUser();

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        if ("ROLE_MANAGER".equals(currentUser.getRole().getRoleName())) {
            if (!tour.getManagerId().equals(currentUser.getId())) {
                throw new BadRequestException("Bạn không có quyền sửa tour này");
            }
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));

        validateDate(request.getStartDate(), request.getEndDate());

        tour.setTourName(request.getTourName());
        tour.setPrice(request.getPrice());
        tour.setQuantity(request.getQuantity());
        tour.setDescription(request.getDescription());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setCategory(category);

        if ("ROLE_ADMIN".equals(currentUser.getRole().getRoleName())) {
            tour.setManagerId(request.getManagerId());
        }

        handleUpdateImage(request, tour);

        Tour saved = tourRepository.save(tour);

        redisTemplate.opsForValue().set("tour:" + id, mapToResponse(saved), 10, TimeUnit.MINUTES);
        redisTemplate.delete("tours");

        return mapToResponse(saved);
    }

    public void delete(Integer id) {

        User currentUser = getCurrentUser();

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        if ("ROLE_MANAGER".equals(currentUser.getRole().getRoleName())) {
            if (!tour.getManagerId().equals(currentUser.getId())) {
                throw new BadRequestException("Bạn không có quyền xóa tour này");
            }
        }

        tourRepository.deleteById(id);

        redisTemplate.delete("tours");
        redisTemplate.delete("tour:" + id);
    }

    public List<TourResponse> searchTour(String keyword,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         Integer categoryId) {

        User currentUser = getCurrentUser();

        Integer managerId = null;
        if (currentUser != null && "ROLE_MANAGER".equals(currentUser.getRole().getRoleName())) {
            managerId = currentUser.getId();
        }

        return tourRepository.searchTour(keyword, startDate, endDate, categoryId, managerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateDate(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private void handleUploadImage(TourRequest request, Tour tour) {
        if (request.getImg() == null || request.getImg().isEmpty()) return;

        try {
            Path path = Paths.get("uploads");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            String fileName = System.currentTimeMillis() + "_" + request.getImg().getOriginalFilename();
            Path filePath = path.resolve(fileName);

            Files.copy(request.getImg().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            tour.setImg("/uploads/" + fileName);

        } catch (Exception e) {
            throw new BadRequestException("Upload ảnh thất bại");
        }
    }

    private void handleUpdateImage(TourRequest request, Tour tour) {
        handleUploadImage(request, tour);
    }

    private TourResponse mapToResponse(Tour tour) {

        TourResponse res = new TourResponse();

        res.setId(tour.getId());
        res.setTourName(tour.getTourName());
        res.setQuantity(tour.getQuantity());
        res.setDescription(tour.getDescription());
        res.setStartDate(tour.getStartDate());
        res.setEndDate(tour.getEndDate());
        res.setPrice(tour.getPrice());

        if (tour.getImg() != null && !tour.getImg().isBlank()) {
            res.setImg("http://localhost:8080" + tour.getImg());
        }

        if (tour.getCategory() != null) {
            res.setCategoryId(tour.getCategory().getId());
            res.setCategoryName(tour.getCategory().getCategoryName());
        }

        if (tour.getTourDetails() != null && !tour.getTourDetails().isEmpty()) {
            TourDetail d = tour.getTourDetails().get(0);

            if (d.getHotel() != null) {
                res.setHotelName(d.getHotel().getHotelName());
            }

            if (d.getVehicle() != null) {
                res.setVehicleName(d.getVehicle().getVehicleName());
            }
        }

        return res;
    }
}