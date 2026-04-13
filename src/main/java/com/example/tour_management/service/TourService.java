package com.example.tour_management.service;

import com.example.tour_management.dto.tour.TourRequest;
import com.example.tour_management.dto.tour.TourResponse;
import com.example.tour_management.entity.Category;
import com.example.tour_management.entity.Tour;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.CategoryRepository;
import com.example.tour_management.repository.TourRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TourService {

    private static final Logger log = LoggerFactory.getLogger(TourService.class);

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String IMAGE_BASE_URL = "http://localhost:8080/tours/";

    public List<TourResponse> getAll() {
        String key = "tours";

        Object cachedTours = redisTemplate.opsForValue().get(key);

        if (cachedTours != null) {
            log.info("Lấy danh sách tour từ Redis");
            return objectMapper.convertValue(
                    cachedTours,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, TourResponse.class)
            );
        }

        log.info("Lấy danh sách tour từ DB");

        List<TourResponse> tours = tourRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        redisTemplate.opsForValue().set(key, tours, 10, TimeUnit.MINUTES);

        return tours;
    }

    public TourResponse getById(Integer id) {
        String key = "tour:" + id;

        Object cachedTour = redisTemplate.opsForValue().get(key);

        if (cachedTour != null) {
            log.info("Lấy tour id={} từ Redis", id);
            return objectMapper.convertValue(cachedTour, TourResponse.class);
        }

        log.info("Lấy tour id={} từ DB", id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        TourResponse response = mapToResponse(tour);

        redisTemplate.opsForValue().set(key, response, 10, TimeUnit.MINUTES);

        return response;
    }

    public List<TourResponse> searchTour(String keyword, LocalDate startDate, LocalDate endDate) {
        log.info("Tìm kiếm tour với keyword={}", keyword);

        List<Tour> tours = tourRepository.searchTour(keyword, startDate, endDate);

        return tours.stream().map(this::mapToResponse).toList();
    }

    public TourResponse create(TourRequest request) {

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

        handleUploadImage(request, tour);

        redisTemplate.delete("tours");

        log.info("Tạo tour thành công: {}", tour.getTourName());

        return mapToResponse(tourRepository.save(tour));
    }

    public TourResponse update(Integer id, TourRequest request) {

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

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

        handleUpdateImage(request, tour);

        Tour savedTour = tourRepository.save(tour);

        TourResponse response = mapToResponse(savedTour);

        redisTemplate.opsForValue().set("tour:" + id, response, 10, TimeUnit.MINUTES);
        redisTemplate.delete("tours");

        log.info("Cập nhật tour id={} thành công", id);

        return response;
    }

    public void delete(Integer id) {
        if (!tourRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy tour");
        }

        tourRepository.deleteById(id);

        redisTemplate.delete("tours");
        redisTemplate.delete("tour:" + id);

        log.info("Xóa tour id={} thành công", id);
    }

    private void validateDate(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private void handleUploadImage(TourRequest request, Tour tour) {
        if (request.getImg() == null || request.getImg().isEmpty()) return;

        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + request.getImg().getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(request.getImg().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            tour.setImg("/uploads/" + fileName);

            log.info("Upload ảnh thành công: {}", filePath.toAbsolutePath());

        } catch (Exception e) {
            log.error("Lỗi upload ảnh", e);
            throw new BadRequestException("Không thể upload ảnh");
        }
    }

    private void handleUpdateImage(TourRequest request, Tour tour) {
        if (request.getImg() == null || request.getImg().isEmpty()) return;

        try {
            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = System.currentTimeMillis() + "_" + request.getImg().getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);

            Files.copy(request.getImg().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            if (tour.getImg() != null) {
                Path oldPath = Paths.get("uploads/" + tour.getImg().replace("/uploads/", ""));
                Files.deleteIfExists(oldPath);
            }

            tour.setImg("/uploads/" + filename);

            log.info("Cập nhật ảnh thành công");

        } catch (Exception e) {
            log.error("Lỗi cập nhật ảnh", e);
            throw new BadRequestException("Upload ảnh thất bại");
        }
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

        String img = tour.getImg();
        if (img != null && !img.isBlank()) {
            if (img.startsWith("http")) {
                res.setImg(img);
            } else if (img.startsWith("/tours/")) {
                res.setImg("http://localhost:8080" + img);
            } else {
                res.setImg(IMAGE_BASE_URL + img);
            }
        }

        if (tour.getCategory() != null) {
            res.setCategoryId(tour.getCategory().getId());
            res.setCategoryName(tour.getCategory().getCategoryName());
        }

        return res;
    }
}