package booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceIml;
import ru.practicum.shareit.error.exception.AccessDeniedException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceIml bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        booker = new User(2L, "Booker", "booker@example.com");
        owner = new User(1L, "Owner", "owner@example.com");
        item = new Item(1L, "Item", "Desc", true, owner, null);
        booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING);
        bookingDto = new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 2L, "WAITING");
    }

    // Тесты для createBooking
    @Test
    void createBooking_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingMapper.toBooking(any(), any(), any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.createBooking(bookingDto, 2L);

        assertNotNull(result);
        verify(bookingRepository).save(any());
        verify(bookingMapper).toBooking(bookingDto, item, booker);
        verify(bookingMapper).toBookingResponseDto(booking);
    }

    @Test
    void createBooking_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, 2L));
        verify(userRepository).findById(2L);
        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void createBooking_itemNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, 2L));
        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
    }

    @Test
    void createBooking_itemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(jakarta.validation.ValidationException.class, () -> bookingService.createBooking(bookingDto, 2L));
        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ownerCannotBookOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, 1L));
        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_invalidDates_startNull() {
        bookingDto.setStart(null);

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(bookingDto, 2L));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createBooking_invalidDates_endNull() {
        bookingDto.setEnd(null);

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(bookingDto, 2L));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createBooking_invalidDates_startAfterEnd() {
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(bookingDto, 2L));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createBooking_invalidDates_endBeforeNow() {
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().minusHours(1));

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(bookingDto, 2L));
        verify(userRepository, never()).findById(anyLong());
    }

    // Тесты для approveBooking
    @Test
    void approveBooking_success_approved() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.approveBooking(1L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(booking);
        verify(bookingMapper).toBookingResponseDto(booking);
    }

    @Test
    void approveBooking_success_rejected() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.approveBooking(1L, 1L, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void approveBooking_bookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(1L, 1L, true));
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_notOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.approveBooking(1L, 2L, true));
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_alreadyApproved() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(jakarta.validation.ValidationException.class, () -> bookingService.approveBooking(1L, 1L, true));
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    // Тесты для getBookingById
    @Test
    void getBookingById_success_booker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.getBookingById(1L, 2L);

        assertNotNull(result);
        verify(bookingRepository).findById(1L);
        verify(bookingMapper).toBookingResponseDto(booking);
    }

    @Test
    void getBookingById_success_owner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.getBookingById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getBookingById_bookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 2L));
        verify(bookingRepository).findById(1L);
    }

    @Test
    void getBookingById_accessDenied() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 3L));
        verify(bookingRepository).findById(1L);
    }

    // Тесты для getAllBookingsByBooker
    @Test
    void getAllBookingsByBooker_all() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerId(2L, Sort.by(Sort.Direction.DESC, "start"))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByBooker(2L, "ALL");

        assertEquals(1, result.size());
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerId(2L, Sort.by(Sort.Direction.DESC, "start"));
        verify(bookingMapper).toBookingResponseDto(booking);
    }

    @Test
    void getAllBookingsByBooker_current() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByBooker(2L, "CURRENT");

        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStartBeforeAndEndAfter(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllBookingsByBooker_past() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndEndBefore(eq(2L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByBooker(2L, "PAST");

        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndEndBefore(eq(2L), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllBookingsByBooker_future() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartAfter(eq(2L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByBooker(2L, "FUTURE");

        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStartAfter(eq(2L), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllBookingsByBooker_waiting() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatus(2L, BookingStatus.WAITING, Sort.by(Sort.Direction.DESC, "start"))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByBooker(2L, "WAITING");

        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStatus(2L, BookingStatus.WAITING, Sort.by(Sort.Direction.DESC, "start"));
    }

    @Test
    void getAllBookingsByBooker_rejected() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatus(2L, BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByBooker(2L, "REJECTED");

        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStatus(2L, BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"));
    }

    @Test
    void getAllBookingsByBooker_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getAllBookingsByBooker(2L, "ALL"));
        verify(userRepository).findById(2L);
        verify(bookingRepository, never()).findByBookerId(anyLong(), any());
    }

    @Test
    void getAllBookingsByBooker_invalidState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThrows(jakarta.validation.ValidationException.class, () -> bookingService.getAllBookingsByBooker(2L, "INVALID"));
        verify(userRepository).findById(2L);
        verify(bookingRepository, never()).findByBookerId(anyLong(), any());
    }

    // Тесты для getAllBookingsByOwner
    @Test
    void getAllBookingsByOwner_all() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerId(1L, Sort.by(Sort.Direction.DESC, "start"))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByOwner(1L, "ALL");

        assertEquals(1, result.size());
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerId(1L, Sort.by(Sort.Direction.DESC, "start"));
        verify(bookingMapper).toBookingResponseDto(booking);
    }

    @Test
    void getAllBookingsByOwner_current() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByOwner(1L, "CURRENT");

        assertEquals(1, result.size());
        verify(bookingRepository).findByItemOwnerIdAndStartBeforeAndEndAfter(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllBookingsByOwner_past() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndEndBefore(eq(1L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByOwner(1L, "PAST");

        assertEquals(1, result.size());
        verify(bookingRepository).findByItemOwnerIdAndEndBefore(eq(1L), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllBookingsByOwner_future() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartAfter(eq(1L), any(LocalDateTime.class), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByOwner(1L, "FUTURE");

        assertEquals(1, result.size());
        verify(bookingRepository).findByItemOwnerIdAndStartAfter(eq(1L), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllBookingsByOwner_waiting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatus(1L, BookingStatus.WAITING, Sort.by(Sort.Direction.DESC, "start"))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByOwner(1L, "WAITING");

        assertEquals(1, result.size());
        verify(bookingRepository).findByItemOwnerIdAndStatus(1L, BookingStatus.WAITING, Sort.by(Sort.Direction.DESC, "start"));
    }

    @Test
    void getAllBookingsByOwner_rejected() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatus(1L, BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getAllBookingsByOwner(1L, "REJECTED");

        assertEquals(1, result.size());
        verify(bookingRepository).findByItemOwnerIdAndStatus(1L, BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"));
    }

    @Test
    void getAllBookingsByOwner_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getAllBookingsByOwner(1L, "ALL"));
        verify(userRepository).findById(1L);
        verify(bookingRepository, never()).findByItemOwnerId(anyLong(), any());
    }

    @Test
    void getAllBookingsByOwner_invalidState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(jakarta.validation.ValidationException.class, () -> bookingService.getAllBookingsByOwner(1L, "INVALID"));
        verify(userRepository).findById(1L);
        verify(bookingRepository, never()).findByItemOwnerId(anyLong(), any());
    }
}