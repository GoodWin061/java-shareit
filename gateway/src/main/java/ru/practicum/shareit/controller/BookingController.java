package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.client.BookingClient;

import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@Valid @RequestBody BookingDto bookingDto,
                                                @RequestHeader(SHARER_USER_ID) Long bookerId) {
        ResponseEntity<Object> response = bookingClient.createBooking(bookingDto, bookerId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@PathVariable Long bookingId,
                                                 @RequestParam boolean approved,
                                                 @RequestHeader(SHARER_USER_ID) Long ownerId) {
        ResponseEntity<Object> response = bookingClient.approveBooking(bookingId, approved, ownerId);  // Исправлено: порядок параметров теперь bookingId, approved, ownerId
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable Long bookingId,
                                                 @RequestHeader(SHARER_USER_ID) Long userId) {
        ResponseEntity<Object> response = bookingClient.getBookingById(bookingId, userId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsByBooker(@RequestHeader(SHARER_USER_ID) Long bookerId,
                                                         @RequestParam(defaultValue = "ALL") String state) {
        ResponseEntity<Object> response = bookingClient.getAllBookingsByBooker(bookerId, state);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsByOwner(@RequestHeader(SHARER_USER_ID) Long ownerId,
                                                        @RequestParam(defaultValue = "ALL") String state) {
        ResponseEntity<Object> response = bookingClient.getAllBookingsByOwner(ownerId, state);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        return ResponseEntity.ok(response.getBody());
    }
}
