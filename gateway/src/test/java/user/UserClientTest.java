package user;

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
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate rest;

    private UserClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.uriTemplateHandler(any(UriTemplateHandler.class))).thenReturn(builder);
        when(builder.build()).thenReturn(rest);
        client = new UserClient("http://test-server", builder);
    }

    @Test
    void createUser_shouldReturnCreated() {
        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setName("John Doe");
        newUserDto.setEmail("john@example.com");
        Object responseBody = new UserDto(1L, "John Doe", "john@example.com");
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.createUser(newUserDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

    @Test
    void updateUser_shouldReturnOk() {
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setName("Updated Name");
        Object responseBody = new UserDto(1L, "Updated Name", "john@example.com");
        ResponseEntity<Object> expected = ResponseEntity.ok(responseBody);
        when(rest.exchange(eq("/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.updateUser(1L, updateUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

    @Test
    void getUserById_shouldReturnOk() {
        Object user = new UserDto(1L, "John Doe", "john@example.com");
        ResponseEntity<Object> expected = ResponseEntity.ok(user);
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void getAllUsers_shouldReturnOk() {
        Object users = java.util.List.of(new UserDto(1L, "John Doe", "john@example.com"));
        ResponseEntity<Object> expected = ResponseEntity.ok(users);
        when(rest.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    void deleteUser_shouldReturnNoContent() {
        ResponseEntity<Object> expected = ResponseEntity.noContent().build();
        when(rest.exchange(eq("/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // Error case tests
    @Test
    void createUser_shouldHandleBadRequest() {
        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setName("John Doe");
        newUserDto.setEmail("john@example.com");
        ResponseEntity<Object> expected = ResponseEntity.badRequest().body("Invalid data");
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.createUser(newUserDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUser_shouldHandleNotFound() {
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setName("Updated Name");
        ResponseEntity<Object> expected = ResponseEntity.notFound().build();
        when(rest.exchange(eq("/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.updateUser(1L, updateUserDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUserById_shouldHandleForbidden() {
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getUserById(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllUsers_shouldHandleInternalServerError() {
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        when(rest.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.getAllUsers();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteUser_shouldHandleConflict() {
        ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot delete user");
        when(rest.exchange(eq("/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> response = client.deleteUser(1L);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // Add more error tests as needed for other methods and statuses
}
