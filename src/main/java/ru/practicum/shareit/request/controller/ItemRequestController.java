package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createRequest(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                        @RequestHeader(SHARER_USER_ID) Long requestorId) {
        return itemRequestService.createRequest(itemRequestDto, requestorId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllRequestsByRequestor(@RequestHeader(SHARER_USER_ID) Long requestorId) {
        return itemRequestService.getAllRequestsByRequestor(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(SHARER_USER_ID) Long userId,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable Long requestId,
                                         @RequestHeader(SHARER_USER_ID) Long userId) {
        return itemRequestService.getRequestById(requestId, userId);
    }
}
