package com.example.contacts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {
    public static final List<String> TYPES = List.of("Друг", "Коллега", "Родственник", "Другое");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Введите имя")
    @Size(max = 100, message = "Имя не должно быть длиннее 100 символов")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Введите телефон")
    @Size(max = 30, message = "Телефон не должен быть длиннее 30 символов")
    @Column(nullable = false, length = 30)
    private String phone;

    @NotNull(message = "Выберите тип контакта")
    @Pattern(regexp = "Друг|Коллега|Родственник|Другое", message = "Выберите тип из списка")
    @Column(nullable = false, length = 20)
    private String type;
}
