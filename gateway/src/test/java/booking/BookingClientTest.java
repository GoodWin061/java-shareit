package booking;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;
import ru.practicum.shareit.client.BookingClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate rest;

    private BookingClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.uriTemplateHandler(any(UriTemplateHandler.class))).thenReturn(builder);
        when(builder.build()).thenReturn(rest);
        client = new BookingClient("http://test-server", builder);
    }

    @Test
    void createBooking_shouldReturnCreated() {
        Object bookingDto = new Object();
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.CREATED).body(bookingDto);
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.createBooking(bookingDto, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void approveBooking_shouldReturnOk() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(rest.exchange(eq("/1?approved={approved}"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class), eq(Map.of("approved", true))))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.approveBooking(1L, true, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getBookingById_shouldReturnOk() {
        Object booking = new Object();
        ResponseEntity<Object> expected = ResponseEntity.ok(booking);
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getBookingById(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllBookingsByBooker_shouldReturnOk() {
        Object bookings = new Object();
        ResponseEntity<Object> expected = ResponseEntity.ok(bookings);
        when(rest.exchange(eq("?state={state}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(Map.of("state", "ALL"))))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getAllBookingsByBooker(1L, "ALL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllBookingsByOwner_shouldReturnOk() {
        Object bookings = new Object();
        ResponseEntity<Object> expected = ResponseEntity.ok(bookings);
        when(rest.exchange(eq("/owner?state={state}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(Map.of("state", "ALL"))))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getAllBookingsByOwner(1L, "ALL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Error case tests
    @Test
    void createBooking_shouldHandleBadRequest() {
        Object bookingDto = new Object();
        ResponseEntity<Object> expected = ResponseEntity.badRequest().body(Map.of("error", "Validation error"));
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.createBooking(bookingDto, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void approveBooking_shouldHandleInternalServerError() {
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        when(rest.exchange(eq("/1?approved={approved}"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class), eq(Map.of("approved", true))))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.approveBooking(1L, true, 1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getBookingById_shouldHandleNotFound() {
        ResponseEntity<Object> expected = ResponseEntity.notFound().build();
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getBookingById(1L, 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}