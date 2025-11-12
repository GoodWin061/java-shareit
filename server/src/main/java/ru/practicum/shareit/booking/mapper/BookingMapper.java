package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "item.id", source = "item.id")
    @Mapping(target = "item.name", source = "item.name")
    @Mapping(target = "booker.id", source = "booker.id")
    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    BookingResponseDto toBookingResponseDto(Booking booking);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    BookingDto toBookingDto(Booking booking);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "start", source = "dto.start")
    @Mapping(target = "end", source = "dto.end")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "booker", source = "booker")
    @Mapping(target = "status", expression = "java(mapStatus(dto.getStatus()))")
    Booking toBooking(BookingDto dto, Item item, User booker);

    default BookingStatus mapStatus(String status) {
        if (status != null) {
            try {
                return BookingStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                return BookingStatus.WAITING;
            }
        }
        return BookingStatus.WAITING;
    }
}
