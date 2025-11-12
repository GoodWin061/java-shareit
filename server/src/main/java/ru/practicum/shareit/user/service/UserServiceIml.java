package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceIml implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Collection<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto addNewUser(NewUserDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Пользователь с email " + request.getEmail() + " уже существует");
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(UpdateUserDto updateUserDto) {
        log.info("Обновление пользователя с ID: {}", updateUserDto.getId());

        User existingUser = userRepository.findById(updateUserDto.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + updateUserDto.getId() + " не найден"));

        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(updateUserDto.getEmail())) {
                throw new ConflictException("Пользователь с таким email уже существует");
            }
        }

        try {
            userMapper.updateUserFromDto(updateUserDto, existingUser);
            User updatedUser = userRepository.save(existingUser);
            log.info("Пользователь с ID {} обновлён", updatedUser.getId());
            return userMapper.toDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка обновления пользователя: {}", e.getMessage());
            throw new ConflictException("Ошибка обновления: возможно, email уже используется");
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id " + id + " не найден"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь с id " + id + " не найден");
        }
        userRepository.deleteById(id);
    }
}


