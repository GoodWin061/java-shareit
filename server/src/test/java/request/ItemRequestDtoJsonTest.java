package request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
public class ItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Описание теста", 1L, LocalDateTime.now(), List.of(new ItemDto()));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Test description\"");
        assertThat(json).contains("\"requestorId\":1");

        ItemRequestDto deserialized = objectMapper.readValue(json, ItemRequestDto.class);
        assertThat(deserialized.getId()).isEqualTo(1L);
        assertThat(deserialized.getDescription()).isEqualTo("Описание теста");
        assertThat(deserialized.getRequestorId()).isEqualTo(1L);
    }
}