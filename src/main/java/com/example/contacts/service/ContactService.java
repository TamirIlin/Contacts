package com.example.contacts.service;

import com.example.contacts.model.Contact;
import com.example.contacts.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {
    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional(readOnly = true)
    public List<Contact> findContacts(Long userId, String search, String type) {
        String normalizedSearch = search == null ? "" : search.trim();

        if (normalizedSearch.isEmpty() && type == null) {
            return contactRepository.findByUserIdOrderByNameAsc(userId);
        }
        if (normalizedSearch.isEmpty()) {
            return contactRepository.findByUserIdAndTypeOrderByNameAsc(userId, type);
        }
        if (type == null) {
            return contactRepository.findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(userId, normalizedSearch);
        }
        return contactRepository.findByUserIdAndNameContainingIgnoreCaseAndTypeOrderByNameAsc(
                userId, normalizedSearch, type);
    }

    @Transactional(readOnly = true)
    public Optional<Contact> findContact(Long id, Long userId) {
        return contactRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public Contact createContact(Long userId, Contact contact) {
        Contact entity = Contact.builder()
                .userId(userId)
                .name(contact.getName().trim())
                .phone(contact.getPhone().trim())
                .type(contact.getType())
                .build();

        return contactRepository.save(entity);
    }

    @Transactional
    public Contact updateContact(Long id, Long userId, Contact contact) {
        Contact entity = contactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Контакт не найден"));

        entity.setName(contact.getName().trim());
        entity.setPhone(contact.getPhone().trim());
        entity.setType(contact.getType());

        return contactRepository.save(entity);
    }

    @Transactional
    public void deleteContact(Long id, Long userId) {
        Contact contact = contactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Контакт не найден"));

        contactRepository.delete(contact);
    }
}
