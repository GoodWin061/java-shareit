package item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.practicum.shareit.item.service.ItemServiceIml;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceIml itemService;

    private User user;
    private Item item;
    private ItemDto itemDto;
    private ItemWithBookingsDto itemWithBookingsDto;
    private Comment comment;
    private CommentDto commentDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        itemWithBookingsDto = new ItemWithBookingsDto();
        itemWithBookingsDto.setId(1L);
        itemWithBookingsDto.setName("Test Item");
        itemWithBookingsDto.setComments(Collections.emptyList());

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Test Comment");

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Test Comment");

        booking = new Booking();
        booking.setId(1L);
        booking.setBooker(user);
    }

    @Test
    void createItem_ShouldReturnItemDto_WhenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemMapper.toItem(itemDto, user)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertEquals(itemDto, result);
        verify(userRepository).findById(1L);
        verify(itemMapper).toItem(itemDto, user);
        verify(itemRepository).save(item);
        verify(itemMapper).toItemDto(item);
    }

    @Test
    void createItem_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(itemDto, 1L));
        verify(userRepository).findById(1L);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateItem_ShouldReturnUpdatedItemDto_WhenOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(1L, itemDto, 1L);

        assertEquals(itemDto, result);
        verify(itemRepository).findById(1L);
        verify(itemMapper).updateItemFromDto(itemDto, item);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldThrowNotFoundException_WhenItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, itemDto, 1L));
        verify(itemRepository).findById(1L);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItem_ShouldThrowAccessDeniedException_WhenNotOwner() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        item.setOwner(anotherUser);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class, () -> itemService.updateItem(1L, itemDto, 1L));
        verify(itemRepository).findById(1L);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void getItemByIdWithBookings_ShouldReturnItemWithBookings_WhenOwner() {
        // Моки для бронирований: предположим, есть last и next
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemWithBookingsDto(item)).thenReturn(itemWithBookingsDto);
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);
        // Моки для бронирований — вызовется дважды (для last и next)
        when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(Optional.of(booking));

        ItemWithBookingsDto result = itemService.getItemByIdWithBookings(1L, 1L);

        assertEquals(itemWithBookingsDto, result);
        verify(bookingRepository, times(1)).findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(anyLong(), any(LocalDateTime.class), any()); // last
        verify(bookingRepository, times(1)).findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(anyLong(), any(LocalDateTime.class), any()); // next
    }

    @Test
    void getItemByIdWithBookings_ShouldThrowNotFoundException_WhenItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemByIdWithBookings(1L, 1L));
    }

    @Test
    void getItemById_ShouldReturnItemDto() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.getItemById(1L);

        assertEquals(itemDto, result);
    }

    @Test
    void getItemEntityById_ShouldReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item result = itemService.getItemEntityById(1L);

        assertEquals(item, result);
    }

    @Test
    void getAllItemsByOwner_ShouldReturnList_WhenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findByOwnerIdOrderById(1L)).thenReturn(List.of(item));
        when(itemMapper.toItemWithBookingsDto(item)).thenReturn(itemWithBookingsDto);
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(Collections.emptyList());
        // Моки для бронирований — аналогично выше
        when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(Optional.of(booking));

        List<ItemWithBookingsDto> result = itemService.getAllItemsByOwner(1L);

        assertEquals(1, result.size());
        assertEquals(itemWithBookingsDto, result.get(0));
        verify(bookingRepository, times(1)).findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(anyLong(), any(LocalDateTime.class), any());
        verify(bookingRepository, times(1)).findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(anyLong(), any(LocalDateTime.class), any());
    }

    @Test
    void getAllItemsByOwner_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getAllItemsByOwner(1L));
    }

    @Test
    void searchItems_ShouldReturnList_WhenTextNotBlank() {
        when(itemRepository.searchAvailableItems("test")).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("test");

        assertEquals(1, result.size());
        assertEquals(itemDto, result.get(0));
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextBlank() {
        List<ItemDto> result = itemService.searchItems("");

        assertTrue(result.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextNull() {
        List<ItemDto> result = itemService.searchItems(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_ShouldReturnCommentDto_WhenValid() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // Исправление: все аргументы — матчеры
        when(bookingRepository.findByItemIdAndBookerIdAndEndBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(booking));
        when(commentMapper.toCommentFromCreate(commentDto, item, user)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        assertEquals(commentDto, result);
        verify(bookingRepository).findByItemIdAndBookerIdAndEndBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED));
    }

    @Test
    void addComment_ShouldThrowNotFoundException_WhenItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(1L, 1L, commentDto));
    }

    @Test
    void addComment_ShouldThrowValidationException_WhenNoBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // Исправление: все аргументы — матчеры
        when(bookingRepository.findByItemIdAndBookerIdAndEndBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.addComment(1L, 1L, commentDto));
        verify(bookingRepository).findByItemIdAndBookerIdAndEndBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED));
    }
}