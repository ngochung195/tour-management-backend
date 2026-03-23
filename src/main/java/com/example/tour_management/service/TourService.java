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

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Async("taskExecutor")
    public CompletableFuture<List<TourResponse>> searchTourPublicAsync(
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId){
        List<Tour> tours = tourRepository.searchPublic(keyword, startDate, endDate, categoryId);

        List<TourResponse> response = mapToResponseList(tours);

        return CompletableFuture.completedFuture(response);
    }

    public TourResponse create(TourRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Tour tour = new Tour();
        tour.setTourName(request.getTourName());
        tour.setPrice(request.getPrice());
        tour.setQuantity(request.getQuantity());
        tour.setDescription(request.getDescription());
        tour.setImg(request.getImg());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setCategory(category);

        redisTemplate.delete("tours");

        return mapToResponse(tourRepository.save(tour));
    }

    public TourResponse update(Integer id, TourRequest request) {

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        tour.setTourName(request.getTourName());
        tour.setPrice(request.getPrice());
        tour.setQuantity(request.getQuantity());
        tour.setDescription(request.getDescription());
        tour.setImg(request.getImg());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setCategory(category);

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

    private List<TourResponse> mapToResponseList(List<Tour> tours){
        return tours.stream().map(this::mapToResponse).toList();
    }
}
