package requestClient;

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
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate rest;

    private ItemRequestClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.uriTemplateHandler(any(UriTemplateHandler.class))).thenReturn(builder);
        when(builder.build()).thenReturn(rest);
        client = new ItemRequestClient("http://test-server", builder);
    }

    @Test
    void createRequest_shouldReturnCreated() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        Object responseBody = new ItemRequestDto(1L, "Нужна дрель", 1L, LocalDateTime.now(), Collections.emptyList());
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.createRequest(requestDto, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

    @Test
    void getUserRequests_shouldReturnOk() {
        Object requests = List.of(new ItemRequestDto(1L, "Нужна дрель", 1L, LocalDateTime.now(), Collections.emptyList()));
        ResponseEntity<Object> expected = ResponseEntity.ok(requests);
        when(rest.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getUserRequests(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(requests, response.getBody());
    }

    @Test
    void getAllRequests_shouldReturnOk() {
        Map<String, Object> parameters = Map.of("from", 0, "size", 10);
        Object requests = List.of(new ItemRequestDto(2L, "Нужен молоток", 2L, LocalDateTime.now(), Collections.emptyList()));
        ResponseEntity<Object> expected = ResponseEntity.ok(requests);
        when(rest.exchange(eq("/all?from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(parameters)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getAllRequests(1L, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(requests, response.getBody());
    }

    @Test
    void getRequestById_shouldReturnOk() {
        Object request = new ItemRequestDto(1L, "Нужна дрель", 1L, LocalDateTime.now(), Collections.emptyList());
        ResponseEntity<Object> expected = ResponseEntity.ok(request);
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getRequestById(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(request, response.getBody());
    }

    // Error case tests
    @Test
    void createRequest_shouldHandleBadRequest() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ResponseEntity<Object> expected = ResponseEntity.badRequest().body("Invalid data");
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.createRequest(requestDto, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getUserRequests_shouldHandleNotFound() {
        ResponseEntity<Object> expected = ResponseEntity.notFound().build();
        when(rest.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getUserRequests(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllRequests_shouldHandleForbidden() {
        Map<String, Object> parameters = Map.of("from", 0, "size", 10);
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        when(rest.exchange(eq("/all?from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(parameters)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getAllRequests(1L, 0, 10);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getRequestById_shouldHandleInternalServerError() {
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getRequestById(1L, 1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Add more error tests as needed for other methods and statuses
}
