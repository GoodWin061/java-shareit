package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(Object bookingDto, Long bookerId) {
        return post("", bookerId, bookingDto);
    }

    public ResponseEntity<Object> approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", ownerId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(Long bookingId, Long userId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllBookingsByBooker(Long bookerId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("?state={state}", bookerId, parameters);
    }

    public ResponseEntity<Object> getAllBookingsByOwner(Long ownerId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("/owner?state={state}", ownerId, parameters);
    }
}