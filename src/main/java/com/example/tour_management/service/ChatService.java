package com.example.tour_management.service;

import com.example.tour_management.entity.Itinerary;
import com.example.tour_management.entity.Tour;
import com.example.tour_management.repository.ItineraryRepository;
import com.example.tour_management.repository.TourRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ChatService {

    private final TourRepository tourRepository;
    private final GeminiService geminiService;
    private final ItineraryRepository itineraryRepository;

    public ChatService(TourRepository tourRepository, GeminiService geminiService,
                       ItineraryRepository itineraryRepository) {
        this.tourRepository = tourRepository;
        this.geminiService = geminiService;
        this.itineraryRepository = itineraryRepository;
    }

    public String chat(String message) {
        List<Tour> tours = tourRepository.findAll();

        String toursData = tours.stream()
                .map(t -> {
                    String basic = "- " + t.getTourName()
                            + " | Giá: " + t.getPrice() + " VND"
                            + " | Còn chỗ: " + t.getQuantity()
                            + " | Ngày đi: " + t.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " | Ngày về: " + t.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    List<Itinerary> itineraries = itineraryRepository.findByTourIdOrderByDayNumberAsc(t.getId());

                    if (itineraries.isEmpty()) return basic;

                    String schedule = itineraries.stream()
                            .map(i -> "    Ngày " + i.getDayNumber()
                                    + " " + (i.getTime() != null ? i.getTime().toString() : "")
                                    + ": " + i.getActivity()
                                    + (i.getDescription() != null ? " - " + i.getDescription() : ""))
                            .collect(java.util.stream.Collectors.joining("\n"));

                    return basic + "\n  Lịch trình:\n" + schedule;
                })
                .collect(java.util.stream.Collectors.joining("\n\n"));

        String prompt = """
                    Bạn là AI tư vấn du lịch TravelGo. Chỉ tư vấn dựa trên danh sách tour bên dưới.
                    Nếu khách hỏi ngoài vấn đề data và không thể trả lời thì nói liên hệ với người quản trị ở trang "Liên hệ"
                    Trả lời bằng tiếng Việt, ngắn gọn, thân thiện. KHÔNG dùng markdown (không dùng **, ##, -, *).
                    Bạn chỉ trả lời câu hỏi của khách dựa vào nội dung được cung cấp, nếu không thể trả lời hoặc câu hỏi của
                    khách ngoài nội dung được cung cấp hãy từ chối trả lời một cách khéo léo và lịch sự.
                    
                    QUY TẮC QUAN TRỌNG:
                    - Khi khách hỏi về danh sách tour, giới thiệu tour, giá, ngày đi: CHỈ dùng thông tin cơ bản, KHÔNG đề cập lịch trình.
                    - Khi khách hỏi về lịch trình, hoạt động, chương trình của một tour cụ thể: mới trả lời lịch trình chi tiết.
                    
                    Danh sách tour hiện có:
                    %s
                    
                    Câu hỏi của khách: %s
                    """.formatted(toursData, message);

        return geminiService.call(prompt);
    }
}