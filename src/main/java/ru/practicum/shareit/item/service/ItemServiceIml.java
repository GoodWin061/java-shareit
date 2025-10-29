package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.error.exception.AccessDeniedException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceIml implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;


    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemMapper.toItem(itemDto, owner);

        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }


    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден с id: " + itemId));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Пользователь не является владельцем");
        }

        itemMapper.updateItemFromDto(itemDto, item);
        itemRepository.save(item);

        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemWithBookingsDto getItemByIdWithBookings(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));

        ItemWithBookingsDto itemWithBookings = itemMapper.toItemWithBookingsDto(item);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            bookingRepository
                    .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                            itemId, now, BookingStatus.APPROVED)
                    .ifPresent(booking -> itemWithBookings.setLastBooking(
                            new ItemWithBookingsDto.BookingInfoDto(booking.getId(), booking.getBooker().getId())));

            bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            itemId, now, BookingStatus.APPROVED)
                    .ifPresent(booking -> itemWithBookings.setNextBooking(
                            new ItemWithBookingsDto.BookingInfoDto(booking.getId(), booking.getBooker().getId())));
        }

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
        itemWithBookings.setComments(comments);

        return itemWithBookings;
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));
        return itemMapper.toItemDto(item);
    }

    @Override
    public Item getItemEntityById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));
    }

    @Override
    public List<ItemWithBookingsDto> getAllItemsByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream().map(item -> {
            ItemWithBookingsDto itemWithBookings = itemMapper.toItemWithBookingsDto(item);

            bookingRepository
                    .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                            item.getId(), now, BookingStatus.APPROVED)
                    .ifPresent(booking -> itemWithBookings.setLastBooking(
                            new ItemWithBookingsDto.BookingInfoDto(booking.getId(), booking.getBooker().getId())));

            bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            item.getId(), now, BookingStatus.APPROVED)
                    .ifPresent(booking -> itemWithBookings.setNextBooking(
                            new ItemWithBookingsDto.BookingInfoDto(booking.getId(), booking.getBooker().getId())));

            List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId())
                    .stream()
                    .map(commentMapper::toCommentDto)
                    .collect(Collectors.toList());
            itemWithBookings.setComments(comments);

            return itemWithBookings;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchAvailableItems(text)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> userBookings = bookingRepository.findByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId, userId, LocalDateTime.now(), BookingStatus.APPROVED);

        if (userBookings.isEmpty()) {
            throw new ValidationException("Пользователь не брал вещь в аренду или аренда еще не завершена");
        }

        Comment comment = commentMapper.toCommentFromCreate(commentDto, item, author);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(savedComment);
    }
}