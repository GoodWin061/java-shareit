package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody NewUserDto userDto) {
        ResponseEntity<Object> response = userClient.createUser(userDto);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserDto request) {
        request.setId(userId);
        ResponseEntity<Object> response = userClient.updateUser(userId, request);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        ResponseEntity<Object> response = userClient.getUserById(userId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        ResponseEntity<Object> response = userClient.getAllUsers();
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        ResponseEntity<Object> response = userClient.deleteUser(userId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(response.getStatusCode()).build();
        }
        return ResponseEntity.noContent().build();
    }
}
