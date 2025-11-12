package request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto requestDto;
    private Long userId = 1L;
    private Long requestId = 1L;

    @BeforeEach
    void setUp() {
        requestDto = new ItemRequestDto();
        requestDto.setId(requestId);
        requestDto.setDescription("Test request description");
        requestDto.setRequestorId(userId);
        requestDto.setCreated(LocalDateTime.now());
        requestDto.setItems(List.of());
    }

    @Test
    void createRequest_ShouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createRequest(any(ItemRequestDto.class), eq(userId))).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Описание тестового запроса"))
                .andExpect(jsonPath("$.requestorId").value(userId));
    }

    @Test
    void createRequest_ShouldReturn400_WhenDescriptionIsBlank() throws Exception {
        requestDto.setDescription("");

        when(itemRequestService.createRequest(any(ItemRequestDto.class), eq(userId)))
                .thenThrow(new jakarta.validation.ValidationException("Описание запроса не может быть пустым"));

        mockMvc.perform(post("/requests")
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequestsByRequestor_ShouldReturnListOfRequests() throws Exception {
        List<ItemRequestDto> requests = List.of(requestDto);
        when(itemRequestService.getAllRequestsByRequestor(userId)).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value("Описание тестового запроса"));
    }

    @Test
    void getAllRequestsByRequestor_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(itemRequestService.getAllRequestsByRequestor(userId)).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/requests")
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRequests_ShouldReturnPagedListOfRequests() throws Exception {
        List<ItemRequestDto> requests = List.of(requestDto);
        when(itemRequestService.getAllRequests(userId, 0, 10)).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header(SHARER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(requestId));
    }

    @Test
    void getAllRequests_ShouldReturn400_WhenInvalidPagination() throws Exception {
        when(itemRequestService.getAllRequests(userId, -1, 0))
                .thenThrow(new jakarta.validation.ValidationException("Параметры пагинации должны быть from >= 0 и size > 0"));

        mockMvc.perform(get("/requests/all")
                        .header(SHARER_USER_ID, userId)
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_ShouldReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(requestId, userId)).thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(requestId));
    }

    @Test
    void getRequestById_ShouldReturn404_WhenRequestNotFound() throws Exception {
        when(itemRequestService.getRequestById(requestId, userId)).thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isNotFound());
    }
}