package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        return userService.getUsers();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody NewUserDto userRequest) {
        return userService.addNewUser(userRequest);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable @Positive(message = "ID must be positive") Long userId,
                          @Valid @RequestBody UpdateUserDto request) {
        request.setId(userId);
        return userService.updateUser(request);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable @Positive(message = "ID must be positive") Long userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable @Positive(message = "ID must be positive") Long userId) {
        userService.deleteUser(userId);
    }
}
