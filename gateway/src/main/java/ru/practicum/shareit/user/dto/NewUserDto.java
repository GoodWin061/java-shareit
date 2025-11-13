package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewUserDto {
    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @Email(message = "Неверный формат электронной почты")
    @NotBlank(message = "Email не должен быть пустым")
    private String email;
}