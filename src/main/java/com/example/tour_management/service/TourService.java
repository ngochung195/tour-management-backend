package com.example.tour_management.service;

import com.example.tour_management.config.RedisConfig;
import com.example.tour_management.dto.tour.TourRequest;
import com.example.tour_management.dto.tour.TourResponse;
import com.example.tour_management.entity.Category;
import com.example.tour_management.entity.Tour;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.CategoryRepository;
import com.example.tour_management.repository.TourRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<TourResponse> getAll() {
        String key = "tours";

        Object cachedTours = redisTemplate.opsForValue().get(key);

        if (cachedTours != null){
            System.out.println("Get data from Redis");
            return objectMapper.convertValue(
                    cachedTours,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TourResponse.class)
            );
        }

        System.out.println("Query DB");

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

        if (cachedTour != null){
            System.out.println("Get data from Redis");
            return objectMapper.convertValue(cachedTour, TourResponse.class);
        }

        System.out.println("Query DB");

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found"));

        TourResponse response = mapToResponse(tour);

        redisTemplate.opsForValue().set(key, response, 10, TimeUnit.MINUTES);

        return response;
    }

    public List<TourResponse> searchTour(String keyword, LocalDate startDate, LocalDate endDate){

        List<Tour> tours = tourRepository.searchTour(keyword, startDate, endDate);

        return tours.stream().map(this::mapToResponse).toList();
    }


    public TourResponse create(TourRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (request.getStartDate() != null && request.getEndDate() != null){
            if (request.getEndDate().isBefore(request.getStartDate())){
                throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
            }
        }

        Tour tour = new Tour();
        tour.setTourName(request.getTourName());
        tour.setPrice(request.getPrice());
        tour.setQuantity(request.getQuantity());
        tour.setDescription(request.getDescription());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setCategory(category);

        if (request.getImg() != null && !request.getImg().isEmpty()){
            try{
                String uploadDir = System.getProperty("user.dir") + "/uploads";
                Path uploadPath = Paths.get(uploadDir);;
                if (!Files.exists(uploadPath)){
                    Files.createDirectories(uploadPath);
                }

                String fileName = System.currentTimeMillis() + "_" + request.getImg().getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(request.getImg().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                tour.setImg("/uploads/" + fileName);

                System.out.println("SAVE TO: " + filePath.toAbsolutePath());

            } catch (Exception e) {
                throw new RuntimeException("Could not store file: " + e.getMessage());
            }
        }

        redisTemplate.delete("tours");

        return mapToResponse(tourRepository.save(tour));
    }

    public TourResponse update(Integer id, TourRequest request) {

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (request.getStartDate() != null && request.getEndDate() != null){
            if (request.getEndDate().isBefore(request.getStartDate())){
                throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
            }
        }

        tour.setTourName(request.getTourName());
        tour.setPrice(request.getPrice());
        tour.setQuantity(request.getQuantity());
        tour.setDescription(request.getDescription());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setCategory(category);

        if (request.getImg() != null && !request.getImg().isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);

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

            } catch (Exception e) {
                throw new RuntimeException("Upload file failed: " + e.getMessage());
            }
        }

        Tour savedTour = tourRepository.save(tour);

        TourResponse response = mapToResponse(savedTour);

        redisTemplate.opsForValue().set("tour:" + id, response, 10, TimeUnit.MINUTES);

        redisTemplate.delete("tours");

        return mapToResponse(tourRepository.save(tour));
    }

    public void delete(Integer id) {
        if (!tourRepository.existsById(id)) {
            throw new NotFoundException("Tour not found");
        }
        tourRepository.deleteById(id);

        redisTemplate.delete("tours");
        redisTemplate.delete("tour:" + id);
    }

    private static final String IMAGE_BASE_URL = "http://localhost:8080/tours/";

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
