package com.example.tour_management.controller;

import com.example.tour_management.dto.contact.ContactRequest;
import com.example.tour_management.entity.Contact;
import com.example.tour_management.enums.ContactStatus;
import com.example.tour_management.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<?> send(@Valid @RequestBody ContactRequest req) {
        contactService.sendContact(req);
        return ResponseEntity.ok("Gửi liên hệ thành công");
    }

    @GetMapping
    public List<Contact> getAll() {
        return contactService.getAll();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestParam ContactStatus status) {

        contactService.updateStatus(id, status);
        return ResponseEntity.ok("Cập nhật thành công");
    }
}