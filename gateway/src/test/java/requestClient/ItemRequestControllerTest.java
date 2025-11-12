package requestClient;

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
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createRequest_success() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        ItemRequestDto responseDto = new ItemRequestDto();
        responseDto.setId(1L);
        responseDto.setDescription("Нужна дрель");
        responseDto.setRequestorId(1L);
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(Collections.emptyList());

        when(itemRequestClient.createRequest(any(ItemRequestDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/requests")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void createRequest_userNotFound() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        when(itemRequestClient.createRequest(any(ItemRequestDto.class), eq(999L)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден"));

        mockMvc.perform(post("/requests")
                        .header(SHARER_USER_ID, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Пользователь не найден"));
    }

    @Test
    void getUserRequests_success() throws Exception {
        ItemRequestDto request = new ItemRequestDto();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setRequestorId(1L);
        request.setCreated(LocalDateTime.now());
        request.setItems(Collections.emptyList());
        List<ItemRequestDto> requests = List.of(request);

        when(itemRequestClient.getUserRequests(eq(1L)))
                .thenReturn(ResponseEntity.ok(requests));

        mockMvc.perform(get("/requests")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getAllRequests_success() throws Exception {
        ItemRequestDto request = new ItemRequestDto();
        request.setId(2L);
        request.setDescription("Нужен молоток");
        request.setRequestorId(2L);
        request.setCreated(LocalDateTime.now());
        request.setItems(Collections.emptyList());
        List<ItemRequestDto> requests = List.of(request);

        when(itemRequestClient.getAllRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(requests));

        mockMvc.perform(get("/requests/all")
                        .header(SHARER_USER_ID, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].description").value("Нужен молоток"));
    }

    @Test
    void getRequestById_success() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Нужна дрель");
        requestDto.setRequestorId(1L);
        requestDto.setCreated(LocalDateTime.now());
        requestDto.setItems(Collections.emptyList());

        when(itemRequestClient.getRequestById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(get("/requests/1")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void getRequestById_notFound() throws Exception {
        when(itemRequestClient.getRequestById(eq(999L), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Запрос не найден"));

        mockMvc.perform(get("/requests/999")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Запрос не найден"));
    }
}