package com.example.tour_management.service;

import com.example.tour_management.dto.promotion.PromotionRequest;
import com.example.tour_management.dto.promotion.PromotionResponse;
import com.example.tour_management.entity.Promotion;
import com.example.tour_management.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public List<PromotionResponse> getAll() {
        return promotionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PromotionResponse getById(Integer id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion"));
        return mapToResponse(p);
    }

    public List<PromotionResponse> search(
            String code,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean status
    ) {
        return promotionRepository.search(code, startDate, endDate, status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PromotionResponse create(PromotionRequest req) {

        if (promotionRepository.findByCode(req.getCode()).isPresent()) {
            throw new RuntimeException("Code đã tồn tại");
        }

        validateDate(req.getStartDate(), req.getEndDate());

        Promotion p = new Promotion();
        p.setCode(req.getCode());
        p.setDiscount(req.getDiscount());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());

        p.setActive(req.getActive() != null ? req.getActive() : true);

        return mapToResponse(promotionRepository.save(p));
    }

    public PromotionResponse update(Integer id, PromotionRequest req) {

        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion"));

        promotionRepository.findByCode(req.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Code đã tồn tại");
            }
        });

        validateDate(req.getStartDate(), req.getEndDate());

        p.setCode(req.getCode());
        p.setDiscount(req.getDiscount());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());

        if (req.getActive() != null) {
            p.setActive(req.getActive());
        }

        return mapToResponse(promotionRepository.save(p));
    }

    public void delete(Integer id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy promotion");
        }
        promotionRepository.deleteById(id);
    }

    private void validateDate(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new RuntimeException("End date phải sau start date");
        }
    }

    private PromotionResponse mapToResponse(Promotion p) {
        PromotionResponse res = new PromotionResponse();
        res.setId(p.getId());
        res.setCode(p.getCode());
        res.setDiscount(p.getDiscount());
        res.setStartDate(p.getStartDate());
        res.setEndDate(p.getEndDate());

        res.setIsActive(Boolean.TRUE.equals(p.getActive()));

        return res;
    }
}