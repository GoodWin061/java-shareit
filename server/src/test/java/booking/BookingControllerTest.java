package booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

@AutoConfigureMockMvc
@SpringBootTest(classes = ShareItServer.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_success() throws Exception {
        BookingDto dto = new BookingDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 2L, null);
        BookingResponseDto response = new BookingResponseDto(1L, dto.getStart(), dto.getEnd(), new BookingResponseDto.Item(1L, "Item"), new BookingResponseDto.Booker(2L), "WAITING");

        when(bookingService.createBooking(any(), any())).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, "2")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void approveBooking_success() throws Exception {
        BookingResponseDto response = new BookingResponseDto(1L, LocalDateTime.now(), LocalDateTime.now(), null, null, "APPROVED");

        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(response);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header(SHARER_USER_ID, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_success() throws Exception {
        BookingResponseDto response = new BookingResponseDto(1L, LocalDateTime.now(), LocalDateTime.now(), null, null, "WAITING");

        when(bookingService.getBookingById(1L, 2L)).thenReturn(response);

        mockMvc.perform(get("/bookings/1")
                        .header(SHARER_USER_ID, "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllBookingsByBooker_success() throws Exception {
        List<BookingResponseDto> responses = List.of(new BookingResponseDto(1L, LocalDateTime.now(), LocalDateTime.now(), null, null, "WAITING"));

        when(bookingService.getAllBookingsByBooker(2L, "ALL")).thenReturn(responses);

        mockMvc.perform(get("/bookings")
                        .header(SHARER_USER_ID, "2")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void getAllBookingsByOwner_success() throws Exception {
        List<BookingResponseDto> responses = List.of(new BookingResponseDto(1L, LocalDateTime.now(), LocalDateTime.now(), null, null, "WAITING"));

        when(bookingService.getAllBookingsByOwner(1L, "ALL")).thenReturn(responses);

        mockMvc.perform(get("/bookings/owner")
                        .header(SHARER_USER_ID, "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
}