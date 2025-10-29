package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    Collection<UserDto> getUsers();

    UserDto addNewUser(NewUserDto request);

    UserDto updateUser(UpdateUserDto request);

    UserDto getUserById(Long id);

    void deleteUser(Long id);
}
