package item;

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
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate rest;

    private ItemClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.uriTemplateHandler(any(UriTemplateHandler.class))).thenReturn(builder);
        when(builder.build()).thenReturn(rest);
        client = new ItemClient("http://test-server", builder);
    }

    @Test
    void createItem_shouldReturnCreated() {
        NewItemDto newItemDto = NewItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
        Object responseBody = new Object();
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.createItem(newItemDto, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

    @Test
    void updateItem_shouldReturnOk() {
        UpdateItemDto updateItemDto = UpdateItemDto.builder()
                .name("Updated Name")
                .build();
        Object responseBody = new Object();
        ResponseEntity<Object> expected = ResponseEntity.ok(responseBody);
        when(rest.exchange(eq("/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.updateItem(1L, updateItemDto, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

    @Test
    void getItemById_shouldReturnOk() {
        Object item = new Object();
        ResponseEntity<Object> expected = ResponseEntity.ok(item);
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getItemById(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(item, response.getBody());
    }

    @Test
    void getAllItemsByOwner_shouldReturnOk() {
        Object items = new Object();
        ResponseEntity<Object> expected = ResponseEntity.ok(items);
        when(rest.exchange(eq("?from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(Map.of("from", 0, "size", 10))))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getAllItemsByOwner(1L, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(items, response.getBody());
    }

    @Test
    void searchItems_shouldReturnOk() {
        Object items = new Object();
        ResponseEntity<Object> expected = ResponseEntity.ok(items);
        when(rest.exchange(eq("/search?text={text}&from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(Map.of("text", "test", "from", 0, "size", 10))))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.searchItems("test", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(items, response.getBody());
    }

    @Test
    void addComment_shouldReturnCreated() {
        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);
        Object responseBody = new Object();
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        when(rest.exchange(eq("/1/comment?from={from}&size={size}"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class), eq(Map.of("from", 0, "size", 10))))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.addComment(1L, 1L, commentDto, 0, 10);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

    // Error case tests
    @Test
    void createItem_shouldHandleBadRequest() {
        NewItemDto newItemDto = NewItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
        ResponseEntity<Object> expected = ResponseEntity.badRequest().body("Invalid data");
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.createItem(newItemDto, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateItem_shouldHandleNotFound() {
        UpdateItemDto updateItemDto = UpdateItemDto.builder()
                .name("Updated Name")
                .build();
        ResponseEntity<Object> expected = ResponseEntity.notFound().build();
        when(rest.exchange(eq("/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.updateItem(1L, updateItemDto, 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getItemById_shouldHandleForbidden() {
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getItemById(1L, 1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void searchItems_shouldHandleInternalServerError() {
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        when(rest.exchange(eq("/search?text={text}&from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(Map.of("text", "test", "from", 0, "size", 10))))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.searchItems("test", 0, 10);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
