package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;


public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemWithBookingsDto getItemByIdWithBookings(Long itemId, Long userId);

    ItemDto getItemById(Long itemId);

    Item getItemEntityById(Long itemId);

    List<ItemWithBookingsDto> getAllItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);

}