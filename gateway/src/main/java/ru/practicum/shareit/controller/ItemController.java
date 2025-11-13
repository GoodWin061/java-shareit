package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody NewItemDto newItemDto,
                                             @RequestHeader(SHARER_USER_ID) Long ownerId) {
        ResponseEntity<Object> response = itemClient.createItem(newItemDto, ownerId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId,
                                             @RequestBody UpdateItemDto updateItemDto,
                                             @RequestHeader(SHARER_USER_ID) Long ownerId) {
        ResponseEntity<Object> response = itemClient.updateItem(itemId, updateItemDto, ownerId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId,
                                              @RequestHeader(SHARER_USER_ID) Long userId) {
        ResponseEntity<Object> response = itemClient.getItemById(itemId, userId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwner(@RequestHeader(SHARER_USER_ID) Long ownerId,
                                                     @RequestParam(defaultValue = "0") int from,
                                                     @RequestParam(defaultValue = "10") int size) {
        ResponseEntity<Object> response = itemClient.getAllItemsByOwner(ownerId, from, size);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        ResponseEntity<Object> response = itemClient.searchItems(text, from, size);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @RequestHeader(SHARER_USER_ID) Long userId,
                                             @Valid @RequestBody CommentDto commentDto,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        ResponseEntity<Object> response = itemClient.addComment(itemId, userId, commentDto, from, size);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}