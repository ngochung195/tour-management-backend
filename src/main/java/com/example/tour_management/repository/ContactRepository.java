package com.example.tour_management.repository;

import com.example.tour_management.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    @Query(value = """
                SELECT COUNT(*)
                FROM contacts
                WHERE status = 'NEW'
            """, nativeQuery = true)
    long countNewContacts();

    @Query("SELECT c FROM Contact c WHERE c.status = 'NEW'")
    List<Contact> findRecentNewContacts(Pageable pageable);
}