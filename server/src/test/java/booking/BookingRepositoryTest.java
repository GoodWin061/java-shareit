package booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = ShareItServer.class)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @Sql("/test-data/bookings-users-items.sql")
    void findByBookerId_returnsBookings() {
        List<Booking> bookings = bookingRepository.findByBookerId(2L, null);

        assertEquals(2, bookings.size());
    }

    @Test
    @Sql("/test-data/bookings-users-items.sql")
    void findByItemOwnerId_returnsBookings() {
        List<Booking> bookings = bookingRepository.findByItemOwnerId(1L, null);

        assertEquals(2, bookings.size());
    }

    @Test
    @Sql("/test-data/bookings-users-items.sql")
    void findByBookerIdAndStatus_returnsWaiting() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndStatus(2L, BookingStatus.WAITING, null);

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
    }

    @Test
    @Sql("/test-data/bookings-users-items.sql")
    void findByItemOwnerIdAndEndBefore_returnsPast() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndEndBefore(1L, LocalDateTime.now(), null);

        assertEquals(1, bookings.size());  // ID=2 is in the past
    }
}