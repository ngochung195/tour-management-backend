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
    private final HotelRepository hotelRepository;
    private final VehicleRepository vehicleRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String IMAGE_BASE_URL = "http://localhost:8080/tours/";

    public TourService(TourRepository tourRepository,
                       CategoryRepository categoryRepository,
                       HotelRepository hotelRepository,
                       VehicleRepository vehicleRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       ObjectMapper objectMapper) {
        this.tourRepository = tourRepository;
        this.categoryRepository = categoryRepository;
        this.hotelRepository = hotelRepository;
        this.vehicleRepository = vehicleRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<TourResponse> getAll() {
        String key = "tours";

        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return objectMapper.convertValue(
                    cached,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, TourResponse.class)
            );
        }

        List<TourResponse> list = tourRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        redisTemplate.opsForValue().set(key, list, 10, TimeUnit.MINUTES);
        return list;
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

    public List<TourResponse> searchTour(String keyword,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         Integer categoryId) {

        List<Tour> tours = tourRepository.searchTour(keyword, startDate, endDate, categoryId);

        return tours.stream()
                .map(this::mapToResponse)
                .toList();
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

        Tour saved = tourRepository.save(tour);

        redisTemplate.opsForValue().set("tour:" + id, mapToResponse(saved), 10, TimeUnit.MINUTES);
        redisTemplate.delete("tours");

        return mapToResponse(saved);
    }

    public void delete(Integer id) {
        if (!tourRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy tour");
        }

        tourRepository.deleteById(id);

        redisTemplate.delete("tours");
        redisTemplate.delete("tour:" + id);
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
            }
            else if (img.startsWith("/uploads/")) {
                res.setImg("http://localhost:8080" + img);
            }
            else if (img.startsWith("/tours/")) {
                res.setImg("http://localhost:8080" + img);
            }
            else {
                res.setImg("http://localhost:8080/tours/" + img);
            }
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