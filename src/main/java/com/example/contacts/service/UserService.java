package com.example.contacts.service;

import com.example.contacts.model.User;
import com.example.contacts.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class UserService {
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(User user) {
        String login = user.getLogin().trim();
        if (userRepository.existsByLogin(login)) {
            throw new DuplicateLoginException("Пользователь с таким логином уже существует");
        }
        if (user.getPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new PasswordTooLongException("Пароль слишком длинный");
        }

        User entity = User.builder()
                .login(login)
                .password(passwordEncoder.encode(user.getPassword()))
                .build();

        return userRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String login, String password) {
        if (login == null || password == null) {
            return Optional.empty();
        }

        return userRepository.findByLogin(login.trim())
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    public static class DuplicateLoginException extends RuntimeException {
        public DuplicateLoginException(String message) {
            super(message);
        }
    }

    public static class PasswordTooLongException extends RuntimeException {
        public PasswordTooLongException(String message) {
            super(message);
        }
    }
}
