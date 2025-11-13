package item;

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
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.error.exception.AccessDeniedException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HttpHeaders.SHARER_USER_ID;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private ItemWithBookingsDto itemWithBookingsDto;
    private CommentDto commentDto;
    private Long userId = 1L;
    private Long itemId = 1L;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(itemId);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(1L);

        itemWithBookingsDto = new ItemWithBookingsDto();
        itemWithBookingsDto.setId(itemId);
        itemWithBookingsDto.setName("Test Item");
        itemWithBookingsDto.setDescription("Test Description");
        itemWithBookingsDto.setAvailable(true);
        itemWithBookingsDto.setRequestId(1L);
        itemWithBookingsDto.setLastBooking(null);
        itemWithBookingsDto.setNextBooking(null);
        itemWithBookingsDto.setComments(List.of());

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Test Comment");
        commentDto.setAuthorName("Test Author");
        commentDto.setCreated(LocalDateTime.now());
    }

    @Test
    void createItem_ShouldReturnCreatedItem() throws Exception {
        when(itemService.createItem(any(ItemDto.class), eq(userId))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createItem_ShouldReturn400_WhenValidationFails() throws Exception {
        itemDto.setName("");

        when(itemService.createItem(any(ItemDto.class), eq(userId)))
                .thenThrow(new jakarta.validation.ValidationException("Название не может быть пустым"));

        mockMvc.perform(post("/items")
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        when(itemService.updateItem(eq(itemId), any(ItemDto.class), eq(userId))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void updateItem_ShouldReturn404_WhenItemNotFound() throws Exception {
        when(itemService.updateItem(eq(itemId), any(ItemDto.class), eq(userId)))
                .thenThrow(new NotFoundException("Продукт не найден"));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_ShouldReturn403_WhenAccessDenied() throws Exception {
        when(itemService.updateItem(eq(itemId), any(ItemDto.class), eq(userId)))
                .thenThrow(new AccessDeniedException("Пользователь не является владельцем"));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getItemById_ShouldReturnItemWithBookings() throws Exception {
        when(itemService.getItemByIdWithBookings(eq(itemId), eq(userId))).thenReturn(itemWithBookingsDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.comments").isArray());
    }

    @Test
    void getItemById_ShouldReturn404_WhenItemNotFound() throws Exception {
        when(itemService.getItemByIdWithBookings(eq(itemId), eq(userId)))
                .thenThrow(new NotFoundException("Продукт не найден"));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllItemsByOwner_ShouldReturnListOfItems() throws Exception {
        List<ItemWithBookingsDto> items = List.of(itemWithBookingsDto);
        when(itemService.getAllItemsByOwner(eq(userId))).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header(SHARER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(itemId));
    }

    @Test
    void getAllItemsByOwner_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(itemService.getAllItemsByOwner(eq(userId)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/items")
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchItems_ShouldReturnListOfItems() throws Exception {
        List<ItemDto> items = List.of(itemDto);
        when(itemService.searchItems("test")).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "test")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsBlank() throws Exception {
        when(itemService.searchItems("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void addComment_ShouldReturnCreatedComment() throws Exception {
        when(itemService.addComment(eq(itemId), eq(userId), any(CommentDto.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Test Comment"))
                .andExpect(jsonPath("$.authorName").value("Test Author"));
    }

    @Test
    void addComment_ShouldReturn400_WhenValidationFails() throws Exception {
        commentDto.setText("");

        when(itemService.addComment(eq(itemId), eq(userId), any(CommentDto.class)))
                .thenThrow(new jakarta.validation.ValidationException("Текст комментария не может быть пустым"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_ShouldReturn404_WhenItemNotFound() throws Exception {
        when(itemService.addComment(eq(itemId), eq(userId), any(CommentDto.class)))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(SHARER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isNotFound());
    }
}