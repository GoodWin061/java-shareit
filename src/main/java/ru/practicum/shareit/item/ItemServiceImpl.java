package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.ForbiddenException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemMapper itemMapper;

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Item createItem(NewItemDto newItemDto, Long userId) {
        UserDto userDto = userService.getUserById(userId);
        User user = UserMapper.fromDto(userDto);
        Item item = itemMapper.mapToItem(newItemDto, user);
        item.setId(nextId.getAndIncrement());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Long itemId, UpdateItemDto updateItemDto, Long userId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Item not found");
        }
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can update item");
        }
        Item updatedItem = itemMapper.updateItemFields(item, updateItemDto);
        items.put(itemId, updatedItem);
        return updatedItem;
    }

    @Override
    public Item getItem(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Item not found");
        }
        return item;
    }

    @Override
    public List<Item> getItemsByUser(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItems(String text) {
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(lowerText) ||
                                item.getDescription().toLowerCase().contains(lowerText)))
                .collect(Collectors.toList());
    }
}