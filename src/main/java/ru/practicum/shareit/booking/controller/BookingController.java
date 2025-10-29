package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@Valid @RequestBody BookingDto bookingDto,
                                            @RequestHeader(SHARER_USER_ID) Long bookerId) {
        return bookingService.createBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@PathVariable Long bookingId,
                                             @RequestParam boolean approved,
                                             @RequestHeader(SHARER_USER_ID) Long ownerId) {
        return bookingService.approveBooking(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId,
                                             @RequestHeader(SHARER_USER_ID) Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllBookingsByBooker(@RequestHeader(SHARER_USER_ID) Long bookerId,
                                                           @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsByBooker(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllBookingsByOwner(@RequestHeader(SHARER_USER_ID) Long ownerId,
                                                          @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsByOwner(ownerId, state);
    }
}
