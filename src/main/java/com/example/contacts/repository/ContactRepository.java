package com.example.contacts.repository;

import com.example.contacts.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUserIdOrderByNameAsc(Long userId);

    List<Contact> findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(Long userId, String name);

    List<Contact> findByUserIdAndTypeOrderByNameAsc(Long userId, String type);

    List<Contact> findByUserIdAndNameContainingIgnoreCaseAndTypeOrderByNameAsc(Long userId, String name, String type);

    Optional<Contact> findByIdAndUserId(Long id, Long userId);
}
