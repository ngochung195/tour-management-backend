package com.example.tour_management.service;

import com.example.tour_management.dto.contact.ContactRequest;
import com.example.tour_management.entity.Contact;
import com.example.tour_management.enums.ContactStatus;
import com.example.tour_management.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    // CUSTOMER
    public void sendContact(ContactRequest req) {

        Contact contact = new Contact();
        contact.setUserName(req.getUserName());
        contact.setEmail(req.getEmail());
        contact.setSubject(req.getSubject());
        contact.setMessage(req.getMessage());
        contact.setStatus(ContactStatus.NEW);

        contactRepository.save(contact);
    }

    // ADMIN
    public List<Contact> getAll() {
        return contactRepository.findAll();
    }

    public void updateStatus(Integer id, ContactStatus status) {
        Contact c = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));

        c.setStatus(status);
        contactRepository.save(c);
    }
}