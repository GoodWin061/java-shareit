package ru.practicum.shareit.item.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "available", source = "dto.available")
    @Mapping(target = "owner", source = "owner")
    Item toItem(ItemDto dto, User owner);

    @Mapping(target = "id", ignore = true)
    Item toEntity(ItemDto itemDto);

    ItemDto toItemDto(Item item);

    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "requestId", source = "requestId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ItemWithBookingsDto toItemWithBookingsDto(Item item);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "requestId", ignore = true)
    void updateItemFromDto(ItemDto dto, @MappingTarget Item item);
}
