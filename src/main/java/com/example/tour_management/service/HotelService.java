package com.example.tour_management.service;

import com.example.tour_management.dto.hotel.HotelRequest;
import com.example.tour_management.dto.hotel.HotelResponse;
import com.example.tour_management.entity.Hotel;
import com.example.tour_management.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Service
public class HotelService {

    private static final Logger log = LoggerFactory.getLogger(HotelService.class);

    @Autowired
    private HotelRepository hotelRepository;

    public List<HotelResponse> getAll() {
        log.info("Đang lấy danh sách tất cả khách sạn");

        List<HotelResponse> list = hotelRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Lấy danh sách khách sạn thành công, số lượng = {}", list.size());

        return list;
    }

    public HotelResponse getById(Integer id) {
        log.info("Đang lấy thông tin khách sạn với id={}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy khách sạn với id={}", id);
                    return new RuntimeException("Không tìm thấy khách sạn");
                });

        log.info("Lấy khách sạn thành công id={}", id);

        return mapToResponse(hotel);
    }

    public HotelResponse create(HotelRequest request) {
        log.info("Đang tạo khách sạn mới, tên={}", request.getHotelName());

        Hotel hotel = new Hotel();
        hotel.setHotelName(request.getHotelName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());

        Hotel saved = hotelRepository.save(hotel);

        log.info("Tạo khách sạn thành công, id={}", saved.getId());

        return mapToResponse(saved);
    }

    public HotelResponse update(Integer id, HotelRequest request) {
        log.info("Đang cập nhật khách sạn id={}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy khách sạn để cập nhật id={}", id);
                    return new RuntimeException("Không tìm thấy khách sạn");
                });

        hotel.setHotelName(request.getHotelName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());

        Hotel updated = hotelRepository.save(hotel);

        log.info("Cập nhật khách sạn thành công id={}", id);

        return mapToResponse(updated);
    }

    public void delete(Integer id) {
        log.info("Đang xoá khách sạn id={}", id);

        if (!hotelRepository.existsById(id)) {
            log.error("Không tìm thấy khách sạn để xoá id={}", id);
            throw new RuntimeException("Không tìm thấy khách sạn");
        }

        hotelRepository.deleteById(id);

        log.info("Xoá khách sạn thành công id={}", id);
    }

    private HotelResponse mapToResponse(Hotel hotel) {
        HotelResponse res = new HotelResponse();
        res.setId(hotel.getId());
        res.setHotelName(hotel.getHotelName());
        res.setDescription(hotel.getDescription());
        res.setAddress(hotel.getAddress());
        return res;
    }
}