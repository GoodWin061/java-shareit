package request;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repisitory.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceIml;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestServiceIml itemRequestService;

    private ItemRequestDto itemRequestDto;
    private User user;
    private ItemRequest itemRequest;
    private ItemDto itemDto;
    private Item item;
    private Long userId = 1L;
    private Long requestId = 1L;

    @BeforeEach
    void setUp() {
        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(requestId);
        itemRequestDto.setDescription("Test description");
        itemRequestDto.setRequestorId(userId);
        itemRequestDto.setCreated(LocalDateTime.now());

        user = new User();
        user.setId(userId);

        itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setDescription("Test description");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        itemDto = new ItemDto();
        itemDto.setId(1L);

        item = new Item();
        item.setId(1L);
        item.setRequestId(requestId);
    }

    @Test
    void createRequest_ShouldReturnCreatedRequest_WhenValidInput() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestMapper.toEntity(itemRequestDto, user)).thenReturn(itemRequest);
        when(itemRequestRepository.save(itemRequest)).thenReturn(itemRequest);
        when(itemRequestMapper.toDto(itemRequest, Collections.emptyList())).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.createRequest(itemRequestDto, userId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Test description", result.getDescription());
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).save(any(ItemRequest.class));
        verify(itemRequestMapper).toDto(any(ItemRequest.class), eq(Collections.emptyList()));
    }

    @Test
    void createRequest_ShouldThrowValidationException_WhenDescriptionIsBlank() {
        itemRequestDto.setDescription("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemRequestService.createRequest(itemRequestDto, userId));
        assertEquals("Описание запроса не может быть пустым", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void createRequest_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(itemRequestDto, userId));
        assertEquals("Пользователь не найден", exception.getMessage());
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getAllRequestsByRequestor_ShouldReturnListOfRequests_WhenUserExists() {
        List<ItemRequest> requests = List.of(itemRequest);
        List<Long> requestIds = List.of(requestId);
        List<Item> items = List.of(item);
        Map<Long, List<ItemDto>> itemsByRequest = Map.of(requestId, List.of(itemDto));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId)).thenReturn(requests);
        when(itemRepository.findByRequestIdIn(requestIds)).thenReturn(items);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);
        when(itemRequestMapper.toDto(itemRequest, List.of(itemDto))).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getAllRequestsByRequestor(userId);

        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(userId);
        verify(itemRepository).findByRequestIdIn(requestIds);
    }

    @Test
    void getAllRequestsByRequestor_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllRequestsByRequestor(userId));
        assertEquals("Пользователь не найден", exception.getMessage());
        verify(itemRequestRepository, never()).findByRequestorIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequestsByRequestor_ShouldReturnEmptyList_WhenNoRequests() {
        List<ItemRequest> emptyRequests = Collections.emptyList();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId)).thenReturn(emptyRequests);

        List<ItemRequestDto> result = itemRequestService.getAllRequestsByRequestor(userId);

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).findByRequestIdIn(anyList());
    }

    @Test
    void getAllRequests_ShouldReturnPagedListOfRequests_WhenValidParams() {
        int from = 0;
        int size = 10;
        List<ItemRequest> requests = List.of(itemRequest);
        List<Long> requestIds = List.of(requestId);
        List<Item> items = List.of(item);
        Map<Long, List<ItemDto>> itemsByRequest = Map.of(requestId, List.of(itemDto));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(eq(userId), any())).thenReturn(requests);
        when(itemRepository.findByRequestIdIn(requestIds)).thenReturn(items);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);
        when(itemRequestMapper.toDto(itemRequest, List.of(itemDto))).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(userId, from, size);

        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        verify(itemRequestRepository).findByRequestorIdNotOrderByCreatedDesc(eq(userId), any());
    }

    @Test
    void getAllRequests_ShouldThrowValidationException_WhenInvalidPagination() {
        int from = -1;
        int size = 0;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemRequestService.getAllRequests(userId, from, size));
        assertEquals("Параметры пагинации должны быть from >= 0 и size > 0", exception.getMessage());
        verify(itemRequestRepository, never()).findByRequestorIdNotOrderByCreatedDesc(anyLong(), any());
    }

    @Test
    void getAllRequests_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllRequests(userId, 0, 10));
        assertEquals("Пользователь не найден", exception.getMessage());
        verify(itemRequestRepository, never()).findByRequestorIdNotOrderByCreatedDesc(anyLong(), any());
    }

    @Test
    void getAllRequests_ShouldReturnEmptyList_WhenNoRequests() {
        List<ItemRequest> emptyRequests = Collections.emptyList();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(eq(userId), any())).thenReturn(emptyRequests);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(userId, 0, 10);

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).findByRequestIdIn(anyList());
    }

    @Test
    void getRequestById_ShouldReturnRequest_WhenExists() {
        List<Item> items = List.of(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(requestId)).thenReturn(items);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);
        when(itemRequestMapper.toDto(itemRequest, List.of(itemDto))).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.getRequestById(requestId, userId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository).findByRequestId(requestId);
    }

    @Test
    void getRequestById_ShouldThrowNotFoundException_WhenRequestNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requestId, userId));
        assertEquals("Запрос не найден", exception.getMessage());
        verify(itemRepository, never()).findByRequestId(anyLong());
    }

    @Test
    void getRequestById_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requestId, userId));
        assertEquals("Пользователь не найден", exception.getMessage());
        verify(itemRequestRepository, never()).findById(anyLong());
    }
}
