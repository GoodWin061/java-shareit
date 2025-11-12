package booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.client.BookingClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createBooking_success() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 2L, "WAITING");
        BookingResponseDto responseDto = new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Item Name"), new BookingResponseDto.Booker(2L), "WAITING");

        when(bookingClient.createBooking(any(BookingDto.class), eq(2L))).thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/bookings")
                        .header(userIdHeader, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void createBooking_invalidRequest() throws Exception {
        BookingDto invalidDto = new BookingDto();

        when(bookingClient.createBooking(any(BookingDto.class), eq(2L)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный запрос на бронирование"));

        mockMvc.perform(post("/bookings")
                        .header(userIdHeader, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_clientError() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 2L, "WAITING");

        when(bookingClient.createBooking(any(BookingDto.class), eq(2L))).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error"));

        mockMvc.perform(post("/bookings")
                        .header(userIdHeader, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_success() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Item Name"), new BookingResponseDto.Booker(2L), "APPROVED");

        when(bookingClient.approveBooking(eq(1L), eq(true), eq(1L))).thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void approveBooking_notFound() throws Exception {
        when(bookingClient.approveBooking(eq(1L), eq(true), eq(1L))).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Бронирование не найдено"));

        mockMvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingById_success() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Item Name"), new BookingResponseDto.Booker(2L), "WAITING");

        when(bookingClient.getBookingById(eq(1L), eq(2L))).thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/bookings/1")
                        .header(userIdHeader, 2L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void getBookingById_accessDenied() throws Exception {
        when(bookingClient.getBookingById(eq(1L), eq(3L))).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Доступ запрещен"));

        mockMvc.perform(get("/bookings/1")
                        .header(userIdHeader, 3L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBookingsByBooker_success() throws Exception {
        List<BookingResponseDto> responseList = List.of(new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Item Name"), new BookingResponseDto.Booker(2L), "WAITING"));

        when(bookingClient.getAllBookingsByBooker(eq(2L), eq("ALL"))).thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseList)));
    }

    @Test
    void getAllBookingsByBooker_invalidState() throws Exception {
        when(bookingClient.getAllBookingsByBooker(eq(2L), eq("INVALID"))).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Недопустимое состояние"));

        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 2L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllBookingsByOwner_success() throws Exception {
        List<BookingResponseDto> responseList = List.of(new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Item Name"), new BookingResponseDto.Booker(2L), "WAITING"));

        when(bookingClient.getAllBookingsByOwner(eq(1L), eq("ALL"))).thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseList)));
    }

    @Test
    void getAllBookingsByOwner_future() throws Exception {
        List<BookingResponseDto> responseList = List.of(new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Item Name"), new BookingResponseDto.Booker(2L), "WAITING"));

        when(bookingClient.getAllBookingsByOwner(eq(1L), eq("FUTURE"))).thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseList)));
    }

    @Test
    void getAllBookingsByOwner_waiting() throws Exception {
        List<BookingResponseDto> responseList = List.of(new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Item Name"), new BookingResponseDto.Booker(2L), "WAITING"));

        when(bookingClient.getAllBookingsByOwner(eq(1L), eq("WAITING"))).thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseList)));
    }

    @Test
    void getAllBookingsByOwner_rejected() throws Exception {
        List<BookingResponseDto> responseList = List.of(new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingResponseDto.Item(1L, "Item Name"), new BookingResponseDto.Booker(2L), "REJECTED"));

        when(bookingClient.getAllBookingsByOwner(eq(1L), eq("REJECTED"))).thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseList)));
    }

    @Test
    void getAllBookingsByOwner_userNotFound() throws Exception {
        when(bookingClient.getAllBookingsByOwner(eq(1L), eq("ALL"))).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден"));

        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBookingsByOwner_invalidState() throws Exception {
        when(bookingClient.getAllBookingsByOwner(eq(1L), eq("INVALID"))).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Недопустимое состояние"));

        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}