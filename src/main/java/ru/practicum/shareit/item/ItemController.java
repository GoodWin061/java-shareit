package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody NewItemDto newItemDto,
                              @RequestHeader(SHARER_USER_ID) Long userId) {
        return itemMapper.mapToItemDto(itemService.createItem(newItemDto, userId));
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestBody UpdateItemDto updateItemDto,
                              @RequestHeader(SHARER_USER_ID) Long userId) {
        return itemMapper.mapToItemDto(itemService.updateItem(itemId, updateItemDto, userId));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        return itemMapper.mapToItemDto(itemService.getItem(itemId));
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(SHARER_USER_ID) Long userId) {
        return itemService.getItemsByUser(userId).stream()
                .map(itemMapper::mapToItemDto)
                .toList();
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        return itemService.searchItems(text).stream()
                .map(itemMapper::mapToItemDto)
                .toList();
    }
}