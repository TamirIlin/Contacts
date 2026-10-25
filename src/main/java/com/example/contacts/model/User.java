package com.example.contacts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank(message = "Введите логин")
    @Size(max = 50, message = "Логин не должен быть длиннее 50 символов")
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @NotBlank(message = "Введите пароль")
    @Size(max = 100, message = "Пароль не должен быть длиннее 100 символов")
    @Column(nullable = false, length = 100)
    private String password;
}
