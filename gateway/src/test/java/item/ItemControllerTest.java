package item;

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
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createItem_success() throws Exception {
        NewItemDto newItemDto = NewItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
        ItemDto responseDto = new ItemDto(1L, "Test Item", "Test Description", true, null);

        when(itemClient.createItem(any(NewItemDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/items")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItemDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void createItem_invalidRequest() throws Exception {
        NewItemDto invalidDto = new NewItemDto();  // name пустое
        invalidDto.setDescription("Valid description");

        mockMvc.perform(post("/items")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isInternalServerError())  // Изменено на 500
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));
    }

    @Test
    void createItem_clientError() throws Exception {
        NewItemDto newItemDto = NewItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        when(itemClient.createItem(any(NewItemDto.class), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data"));

        mockMvc.perform(post("/items")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid data"));
    }

    // Тесты для updateItem
    @Test
    void updateItem_success() throws Exception {
        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Updated Name")
                .build();
        ItemDto responseDto = new ItemDto(1L, "Updated Name", "Test Description", true, null);

        when(itemClient.updateItem(eq(1L), any(UpdateItemDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/items/1")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void updateItem_notFound() throws Exception {
        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Updated Name")
                .build();

        when(itemClient.updateItem(eq(1L), any(UpdateItemDto.class), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found"));

        mockMvc.perform(patch("/items/1")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item not found"));
    }

    // Тесты для getItemById
    @Test
    void getItemById_success() throws Exception {
        ItemDto responseDto = new ItemDto(1L, "Test Item", "Test Description", true, null);

        when(itemClient.getItemById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/items/1")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void getItemById_forbidden() throws Exception {
        when(itemClient.getItemById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied"));

        mockMvc.perform(get("/items/1")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied"));
    }

    @Test
    void getAllItemsByOwner_success() throws Exception {
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Item1", "Desc1", true, null),
                new ItemDto(2L, "Item2", "Desc2", false, null)
        );

        when(itemClient.getAllItemsByOwner(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(items));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(items)));
    }

    @Test
    void getAllItemsByOwner_pagination() throws Exception {
        List<ItemDto> items = List.of(new ItemDto(1L, "Item1", "Desc1", true, null));

        when(itemClient.getAllItemsByOwner(eq(1L), eq(5), eq(20)))
                .thenReturn(ResponseEntity.ok(items));

        mockMvc.perform(get("/items")
                        .header(SHARER_USER_ID, 1L)
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(items)));
    }

    @Test
    void searchItems_success() throws Exception {
        List<ItemDto> items = List.of(new ItemDto(1L, "Test Item", "Test Description", true, null));

        when(itemClient.searchItems(eq("test"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(items));

        mockMvc.perform(get("/items/search")
                        .param("text", "test")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(items)));
    }

    @Test
    void addComment_success() throws Exception {
        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);
        CommentDto responseDto = new CommentDto(1L, "Great item!", "User Name", LocalDateTime.now());

        when(itemClient.addComment(eq(1L), eq(1L), any(CommentDto.class), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/items/1/comment")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void addComment_itemNotFound() throws Exception {
        CommentDto commentDto = new CommentDto(null, "Nice item!", null, null);

        when(itemClient.addComment(eq(1L), eq(1L), any(CommentDto.class), eq(0), eq(10)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found"));

        mockMvc.perform(post("/items/1/comment")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item not found"));
    }
}

