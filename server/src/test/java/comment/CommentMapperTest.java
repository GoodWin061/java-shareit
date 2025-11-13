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

@SpringBootTest(classes = ShareItServer.class)
@ContextConfiguration(classes = ShareItServer.class)
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
        author.setName("Тест автора");

        item = new Item();
        item.setId(1L);
        item.setName("Тест Item");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Тест текста");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(testCreated);

        commentDto = new CommentDto(1L, "Тест текста", "Тест автора", testCreated);
    }

    @Test
    void toCommentDto_ShouldMapCommentToDto() {
        CommentDto result = commentMapper.toCommentDto(comment);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Тест текста");
        assertThat(result.getAuthorName()).isEqualTo("Тест автора");
        assertThat(result.getCreated()).isEqualTo(testCreated);
    }

    @Test
    void toCommentDto_WithNullAuthor_ShouldMapWithoutException() {
        comment.setAuthor(null);

        CommentDto result = commentMapper.toCommentDto(comment);

        assertThat(result.getAuthorName()).isNull();
        assertThat(result.getText()).isEqualTo("Тест текста");
    }

    @Test
    void toComment_ShouldMapDtoToCommentWithCurrentTimeIfNull() {
        commentDto.setCreated(null);

        Comment result = commentMapper.toComment(commentDto, item, author);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Тест текста");
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getCreated()).isAfterOrEqualTo(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void toComment_ShouldMapDtoToCommentWithProvidedTime() {
        Comment result = commentMapper.toComment(commentDto, item, author);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Тест текста");
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCreated()).isEqualTo(testCreated);
    }

    @Test
    void toCommentFromCreate_ShouldMapDtoToCommentWithCurrentTimeAndIgnoreId() {
        CommentDto createDto = new CommentDto();
        createDto.setText("Новый комментарий");

        Comment result = commentMapper.toCommentFromCreate(createDto, item, author);

        assertThat(result.getId()).isNull();
        assertThat(result.getText()).isEqualTo("Новый комментарий");
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getCreated()).isAfterOrEqualTo(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void toCommentFromCreate_WithNullText_ShouldMapButValidationInService() {
        CommentDto createDto = new CommentDto();
        createDto.setText("");

        Comment result = commentMapper.toCommentFromCreate(createDto, item, author);

        assertThat(result.getText()).isEqualTo("");
    }
}