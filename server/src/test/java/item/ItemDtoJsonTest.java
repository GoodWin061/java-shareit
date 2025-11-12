package item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
public class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testItemDtoSerialization() throws Exception {
        ItemDto dto = new ItemDto(1L, "Test Item", "Test Description", true, 1L);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Test Item\"");
        assertThat(json).contains("\"available\":true");

        ItemDto deserialized = objectMapper.readValue(json, ItemDto.class);
        assertThat(deserialized.getId()).isEqualTo(1L);
        assertThat(deserialized.getName()).isEqualTo("Test Item");
        assertThat(deserialized.getAvailable()).isTrue();
    }
}