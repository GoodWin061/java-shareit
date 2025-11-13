package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
    private Long id;
    private String name;
    private String email;
}
