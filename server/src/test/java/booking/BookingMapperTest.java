package booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.mapper.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private final BookingMapper mapper = new BookingMapperImpl();

    @Test
    void toBookingResponseDto_mapsCorrectly() {
        User booker = new User(2L, "Booker", "booker@example.com");
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(1L, "Item", "Description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), item, booker, BookingStatus.WAITING);

        BookingResponseDto dto = mapper.toBookingResponseDto(booking);

        assertEquals(1L, dto.getId());
        assertNotNull(dto.getStart());
        assertNotNull(dto.getEnd());
        assertEquals("WAITING", dto.getStatus());
        assertEquals(1L, dto.getItem().getId());
        assertEquals("Item", dto.getItem().getName());
        assertEquals(2L, dto.getBooker().getId());
    }

    @Test
    void toBooking_mapsCorrectly() {
        User booker = new User(2L, "Booker", "booker@example.com");
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(1L, "Item", "Description", true, owner, null);
        BookingDto dto = new BookingDto(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L, 2L, "WAITING");

        Booking booking = mapper.toBooking(dto, item, booker);

        assertEquals(1L, booking.getId());
        assertNotNull(booking.getStart());
        assertNotNull(booking.getEnd());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
    }

    @Test
    void toBookingDto_mapsCorrectly() {
        User booker = new User(2L, "Booker", "booker@example.com");
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(1L, "Item", "Description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), item, booker, BookingStatus.APPROVED);

        BookingDto dto = mapper.toBookingDto(booking);

        assertEquals(1L, dto.getId());
        assertNotNull(dto.getStart());
        assertNotNull(dto.getEnd());
        assertEquals("APPROVED", dto.getStatus());
        assertEquals(1L, dto.getItemId());
        assertEquals(2L, dto.getBookerId());
    }
}