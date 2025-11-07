package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "requestorId", source = "requestor.id")
    ItemRequestDto toDto(ItemRequest itemRequest);

    default ItemRequestDto toDto(ItemRequest itemRequest, List<ItemDto> items) {
        ItemRequestDto dto = toDto(itemRequest);
        dto.setItems(items != null ? items : List.of());
        return dto;
    }

    @Mapping(target = "id", expression = "java(itemRequestDto.getId())")
    @Mapping(target = "description", expression = "java(itemRequestDto.getDescription())")
    @Mapping(target = "requestor", source = "requestor")
    @Mapping(target = "created", expression = "java(itemRequestDto.getCreated() != null ? itemRequestDto.getCreated() : java.time.LocalDateTime.now())")
    ItemRequest toEntity(ItemRequestDto itemRequestDto, User requestor);
}


