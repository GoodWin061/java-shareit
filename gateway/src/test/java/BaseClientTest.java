import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BaseClientTest {

    @Mock
    private RestTemplate restTemplate;

    private TestBaseClient baseClient;

    @BeforeEach
    void setUp() {
        baseClient = new TestBaseClient(restTemplate);
    }

    private static class TestBaseClient extends BaseClient {
        public TestBaseClient(RestTemplate rest) {
            super(rest);
        }

        public ResponseEntity<Object> testGet(String path) {
            return get(path);
        }

        public ResponseEntity<Object> testGet(String path, Long userId) {
            return get(path, userId);
        }

        public ResponseEntity<Object> testGet(String path, Long userId, Map<String, Object> parameters) {
            return get(path, userId, parameters);
        }

        public <T> ResponseEntity<Object> testPost(String path, T body) {
            return post(path, body);
        }

        public <T> ResponseEntity<Object> testPost(String path, Long userId, T body) {
            return post(path, userId, body);
        }

        public <T> ResponseEntity<Object> testPost(String path, Long userId, Map<String, Object> parameters, T body) {
            return post(path, userId, parameters, body);
        }

        public <T> ResponseEntity<Object> testPut(String path, Long userId, T body) {
            return put(path, userId, body);
        }

        public <T> ResponseEntity<Object> testPut(String path, Long userId, Map<String, Object> parameters, T body) {
            return put(path, userId, parameters, body);
        }

        public <T> ResponseEntity<Object> testPatch(String path, T body) {
            return patch(path, body);
        }

        public <T> ResponseEntity<Object> testPatch(String path, Long userId, T body) {
            return patch(path, userId, body);
        }

        public <T> ResponseEntity<Object> testPatch(String path, Long userId, Map<String, Object> parameters, T body) {
            return patch(path, userId, parameters, body);
        }

        public ResponseEntity<Object> testDelete(String path) {
            return delete(path);
        }

        public ResponseEntity<Object> testDelete(String path, Long userId) {
            return delete(path, userId);
        }

        public ResponseEntity<Object> testDelete(String path, Long userId, Map<String, Object> parameters) {
            return delete(path, userId, parameters);
        }
    }

    @Test
    void get_withoutParameters() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testGet("path");

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void get_withUserId() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testGet("path", 1L);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.GET),
                argThat(entity -> entity.getHeaders().getFirst("X-Sharer-User-Id").equals("1")), eq(Object.class));
    }

    @Test
    void get_withUserIdAndParameters() {
        Map<String, Object> parameters = Map.of("key", "value");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(parameters)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testGet("path", 1L, parameters);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(parameters));
    }

    @Test
    void post_withBody() {
        String body = "testBody";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testPost("path", body);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.POST),
                argThat(entity -> entity.getBody().equals(body)), eq(Object.class));
    }

    @Test
    void post_withUserIdAndBody() {
        String body = "testBody";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testPost("path", 1L, body);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.POST),
                argThat(entity -> entity.getHeaders().getFirst("X-Sharer-User-Id").equals("1") && entity.getBody().equals(body)),
                eq(Object.class));
    }

    @Test
    void post_withUserIdParametersAndBody() {
        Map<String, Object> parameters = Map.of("key", "value");
        String body = "testBody";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class), eq(parameters)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testPost("path", 1L, parameters, body);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class), eq(parameters));
    }

    @Test
    void put_withUserIdAndBody() {
        String body = "testBody";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testPut("path", 1L, body);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void patch_withUserIdAndBody() {
        String body = "testBody";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testPatch("path", 1L, body);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void delete_withUserId() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.noContent().build();
        when(restTemplate.exchange(eq("path"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.testDelete("path", 1L);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(eq("path"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void makeAndSendRequest_handlesHttpStatusCodeException() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("error body".getBytes());

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = baseClient.testGet("path");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertArrayEquals("error body".getBytes(), (byte[]) response.getBody());
    }
}