package com.example.contacts.repository;

import com.example.contacts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
    boolean existsByLogin(String login);
    Optional<User> findByLogin(String login);
}
