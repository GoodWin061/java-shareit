package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item createItem(NewItemDto newItemDto, Long userId);

    Item updateItem(Long itemId, UpdateItemDto updateItemDto, Long userId);

    Item getItem(Long itemId);

    List<Item> getItemsByUser(Long userId);

    List<Item> searchItems(String text);
}
