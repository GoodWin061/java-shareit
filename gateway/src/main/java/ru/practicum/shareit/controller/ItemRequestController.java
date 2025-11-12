package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                                @RequestHeader(SHARER_USER_ID) Long requestorId) {
        ResponseEntity<Object> response = itemRequestClient.createRequest(itemRequestDto, requestorId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader(SHARER_USER_ID) Long requestorId) {
        ResponseEntity<Object> response = itemRequestClient.getUserRequests(requestorId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(SHARER_USER_ID) Long userId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        ResponseEntity<Object> response = itemRequestClient.getAllRequests(userId, from, size);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId,
                                                 @RequestHeader(SHARER_USER_ID) Long userId) {
        ResponseEntity<Object> response = itemRequestClient.getRequestById(requestId, userId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }
}
