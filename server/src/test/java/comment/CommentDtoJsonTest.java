package comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
public class CommentDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final LocalDateTime testCreated = LocalDateTime.of(2023, 10, 1, 12, 0, 0);

    @Test
    void testCommentDtoSerialization() throws Exception {
        CommentDto dto = new CommentDto(1L, "Test Comment Text", "Test Author", testCreated);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"text\":\"Test Comment Text\"");
        assertThat(json).contains("\"authorName\":\"Test Author\"");
        assertThat(json).contains("\"created\":\"2023-10-01T12:00:00\"");

        CommentDto deserialized = objectMapper.readValue(json, CommentDto.class);
        assertThat(deserialized.getId()).isEqualTo(1L);
        assertThat(deserialized.getText()).isEqualTo("Test Comment Text");
        assertThat(deserialized.getAuthorName()).isEqualTo("Test Author");
        assertThat(deserialized.getCreated()).isEqualTo(testCreated);
    }

    @Test
    void testCommentDtoDeserialization_WithEmptyText_ShouldDeserializeSuccessfully() throws Exception {
        String jsonWithEmptyText = "{\"id\":1,\"text\":\"\",\"authorName\":\"Test Author\",\"created\":\"2023-10-01T12:00:00\"}";

        CommentDto deserialized = objectMapper.readValue(jsonWithEmptyText, CommentDto.class);

        assertThat(deserialized.getId()).isEqualTo(1L);
        assertThat(deserialized.getText()).isEmpty();
        assertThat(deserialized.getAuthorName()).isEqualTo("Test Author");
        assertThat(deserialized.getCreated()).isEqualTo(testCreated);
    }

    @Test
    void testCommentDto_WithNullFields_Serialization() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setCreated(testCreated);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":null");
        assertThat(json).contains("\"text\":null");
        assertThat(json).contains("\"authorName\":null");
        assertThat(json).contains("\"created\":\"2023-10-01T12:00:00\"");
    }
}