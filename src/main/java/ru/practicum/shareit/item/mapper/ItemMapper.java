package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner() != null ? item.getOwner().getName() : null)
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(NewItemDto newItemDto, User user) {
        return Item.builder()
                .name(newItemDto.getName())
                .description(newItemDto.getDescription())
                .available(newItemDto.getAvailable())
                .owner(user)
                .build();
    }

    public static Item updateItemFields(Item item, UpdateItemDto updateDto) {
        if (updateDto.hasName()) {
            item.setName(updateDto.getName());
        }
        if (updateDto.hasDescription()) {
            item.setDescription(updateDto.getDescription());
        }
        if (updateDto.hasAvailable()) {
            item.setAvailable(updateDto.getAvailable());
        }
        return item;
    }
}
