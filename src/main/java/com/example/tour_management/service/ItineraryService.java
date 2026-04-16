package com.example.tour_management.service;

import com.example.tour_management.dto.itinerary.ItineraryRequest;
import com.example.tour_management.dto.itinerary.ItineraryResponse;
import com.example.tour_management.entity.Itinerary;
import com.example.tour_management.entity.Tour;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.ItineraryRepository;
import com.example.tour_management.repository.TourRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class ItineraryService {

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private TourRepository tourRepository;

    public List<ItineraryResponse> getAll() {
        return itineraryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ItineraryResponse getById(Integer id) {
        Itinerary it = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch trình"));

        return mapToResponse(it);
    }

    public List<ItineraryResponse> getByTourId(Integer tourId) {
        return itineraryRepository.findByTourIdOrderByDayNumberAsc(tourId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateRequests(List<ItineraryRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Danh sách lịch trình không được rỗng");
        }

        Integer tourId = requests.get(0).getTourId();

        Map<Integer, Set<LocalTime>> timeMap = new HashMap<>();

        for (int i = 0; i < requests.size(); i++) {

            ItineraryRequest r = requests.get(i);

            if (r.getTourId() == null) {
                throw new BadRequestException("Dòng " + (i + 1) + ": tourId không được null");
            }

            if (!r.getTourId().equals(tourId)) {
                throw new BadRequestException(
                        "Dòng " + (i + 1) + ": tất cả lịch trình phải cùng 1 tour"
                );
            }

            if (r.getDayNumber() == null || r.getDayNumber() < 1) {
                throw new BadRequestException(
                        "Dòng " + (i + 1) + ": dayNumber phải >= 1"
                );
            }

            if (r.getTime() == null) {
                throw new BadRequestException(
                        "Dòng " + (i + 1) + ": time không được null"
                );
            }

            if (r.getActivity() == null || r.getActivity().trim().isEmpty()) {
                throw new BadRequestException(
                        "Dòng " + (i + 1) + ": hoạt động không được rỗng"
                );
            }

            timeMap.putIfAbsent(r.getDayNumber(), new HashSet<>());

            if (!timeMap.get(r.getDayNumber()).add(r.getTime())) {
                throw new BadRequestException(
                        "Trùng giờ ở ngày " + r.getDayNumber()
                );
            }
        }
    }

    @Transactional
    public List<ItineraryResponse> create(List<ItineraryRequest> requests) {

        validateRequests(requests);

        requests.sort(
                Comparator.comparing(ItineraryRequest::getDayNumber)
                        .thenComparing(ItineraryRequest::getTime)
        );

        Integer tourId = requests.stream()
                .findFirst()
                .map(ItineraryRequest::getTourId)
                .orElseThrow(() -> new BadRequestException("tourId không hợp lệ"));

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        List<Itinerary> entities = requests.stream().map(req -> {
            Itinerary i = new Itinerary();

            i.setTour(tour);
            i.setDayNumber(req.getDayNumber());
            i.setTime(req.getTime());
            i.setActivity(req.getActivity());
            i.setDescription(req.getDescription());

            return i;
        }).toList();

        return itineraryRepository.saveAll(entities)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public List<ItineraryResponse> update(Integer tourId, List<ItineraryRequest> requests) {

        validateRequests(requests);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        List<Itinerary> oldList = itineraryRepository.findByTourIdOrderByDayNumberAsc(tourId);

        Map<Integer, Itinerary> oldMap = new HashMap<>();
        for (Itinerary it : oldList) {
            oldMap.put(it.getId(), it);
        }

        List<Itinerary> toSave = new ArrayList<>();
        Set<Integer> requestIds = new HashSet<>();

        for (ItineraryRequest req : requests) {

            if (req.getId() != null && oldMap.containsKey(req.getId())) {

                Itinerary existing = oldMap.get(req.getId());

                existing.setDayNumber(req.getDayNumber());
                existing.setTime(req.getTime());
                existing.setActivity(req.getActivity());
                existing.setDescription(req.getDescription());

                toSave.add(existing);
                requestIds.add(req.getId());

            } else {
                Itinerary newIt = new Itinerary();

                newIt.setTour(tour);
                newIt.setDayNumber(req.getDayNumber());
                newIt.setTime(req.getTime());
                newIt.setActivity(req.getActivity());
                newIt.setDescription(req.getDescription());

                toSave.add(newIt);
            }
        }

        List<Itinerary> toDelete = oldList.stream()
                .filter(it -> !requestIds.contains(it.getId()))
                .toList();

        itineraryRepository.deleteAll(toDelete);

        toSave.sort(
                Comparator.comparing(Itinerary::getDayNumber)
                        .thenComparing(Itinerary::getTime)
        );

        return itineraryRepository.saveAll(toSave)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public void delete(Integer id) {
        itineraryRepository.deleteById(id);
    }

    private ItineraryResponse mapToResponse(Itinerary it) {
        ItineraryResponse res = new ItineraryResponse();

        res.setId(it.getId());
        res.setTourId(it.getTour().getId());
        res.setTourName(it.getTour().getTourName());
        res.setDayNumber(it.getDayNumber());
        res.setTime(it.getTime());
        res.setActivity(it.getActivity());
        res.setDescription(it.getDescription());

        return res;
    }

    public void deleteByTour(Integer tourId) {
        List<Itinerary> list = itineraryRepository.findByTourIdOrderByDayNumberAsc(tourId);

        if (list.isEmpty()) {
            throw new NotFoundException("Không có lịch trình để xóa");
        }

        itineraryRepository.deleteAll(list);
    }
}