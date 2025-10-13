package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public Collection<UserDto> getUsers() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto addNewUser(NewUserDto request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Пользователь с email: " + request.getEmail() + " существует");
        }
        User user = UserMapper.toUser(request);
        User savedUser = repository.create(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(UpdateUserDto request) {
        Long id = request.getId();
        User user = findByIdUser(id);

        if (repository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ValidationException("Пользователь с email: " + request.getEmail() + " существует");
        }

        UserMapper.updateUserFromDto(user, request);
        repository.create(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = findByIdUser(id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        findByIdUser(id);
        repository.delete(id);
    }

    private User findByIdUser(Long id) {
        Optional<User> optUser = repository.findByUserId(id);
        if (optUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id: " + id + " в базе отсутствует");
        }
        return optUser.get();
    }
}
