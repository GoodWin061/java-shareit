package ru.practicum.shareit.request.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repisitory.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceIml implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto createRequest(ItemRequestDto itemRequestDto, Long requestorId) {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }

        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = itemRequestMapper.toEntity(itemRequestDto, requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toDto(savedRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getAllRequestsByRequestor(Long requestorId) {
        userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestorId);
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemsByRequest = getItemsByRequestIds(requestIds);

        return requests.stream()
                .map(request -> itemRequestMapper.toDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (from < 0 || size <= 0) {
            throw new ValidationException("Параметры пагинации должны быть from >= 0 и size > 0");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pageable);
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemsByRequest = getItemsByRequestIds(requestIds);

        return requests.stream()
                .map(request -> itemRequestMapper.toDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<ItemDto> items = getItemsByRequestId(requestId);

        return itemRequestMapper.toDto(itemRequest, items);
    }

    private Map<Long, List<ItemDto>> getItemsByRequestIds(List<Long> requestIds) {
        if (requestIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Item> items = itemRepository.findByRequestIdIn(requestIds);
        return items.stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(itemMapper::toItemDto, Collectors.toList())
                ));
    }

    private List<ItemDto> getItemsByRequestId(Long requestId) {
        List<Item> items = itemRepository.findByRequestId(requestId);
        return items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}