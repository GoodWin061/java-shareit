package comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ShareItServer.class)
@ContextConfiguration(classes = ShareItServer.class) // Для инжекта mapper
public class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    private Comment comment;
    private CommentDto commentDto;
    private Item item;
    private User author;
    private final LocalDateTime testCreated = LocalDateTime.of(2023, 10, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(1L);
        author.setName("Test Author");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Test Text");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(testCreated);

        commentDto = new CommentDto(1L, "Test Text", "Test Author", testCreated);
    }

    @Test
    void toCommentDto_ShouldMapCommentToDto() {
        CommentDto result = commentMapper.toCommentDto(comment);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test Text");
        assertThat(result.getAuthorName()).isEqualTo("Test Author"); // Из author.name
        assertThat(result.getCreated()).isEqualTo(testCreated);
    }

    @Test
    void toCommentDto_WithNullAuthor_ShouldMapWithoutException() {
        comment.setAuthor(null);

        CommentDto result = commentMapper.toCommentDto(comment);

        assertThat(result.getAuthorName()).isNull(); // Не бросает ошибку
        assertThat(result.getText()).isEqualTo("Test Text");
    }

    @Test
    void toComment_ShouldMapDtoToCommentWithCurrentTimeIfNull() {
        commentDto.setCreated(null); // null created

        Comment result = commentMapper.toComment(commentDto, item, author);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test Text");
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCreated()).isNotNull(); // Текущее время
        assertThat(result.getCreated()).isAfterOrEqualTo(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void toComment_ShouldMapDtoToCommentWithProvidedTime() {
        Comment result = commentMapper.toComment(commentDto, item, author);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test Text");
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCreated()).isEqualTo(testCreated); // Сохранено из DTO
    }

    @Test
    void toCommentFromCreate_ShouldMapDtoToCommentWithCurrentTimeAndIgnoreId() {
        CommentDto createDto = new CommentDto(); // Без ID
        createDto.setText("New Comment Text");

        Comment result = commentMapper.toCommentFromCreate(createDto, item, author);

        assertThat(result.getId()).isNull(); // Игнорируется
        assertThat(result.getText()).isEqualTo("New Comment Text");
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCreated()).isNotNull(); // Текущее время
        assertThat(result.getCreated()).isAfterOrEqualTo(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void toCommentFromCreate_WithNullText_ShouldMapButValidationInService() {
        CommentDto createDto = new CommentDto();
        createDto.setText(""); // Пустой, но mapper не валидирует

        Comment result = commentMapper.toCommentFromCreate(createDto, item, author);

        assertThat(result.getText()).isEqualTo(""); // Mapper пропустит, валидация в сервисе/контроллере
    }
}