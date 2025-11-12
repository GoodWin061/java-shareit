package comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = ShareItServer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private final LocalDateTime testCreated1 = LocalDateTime.of(2023, 10, 1, 12, 0, 0);
    private final LocalDateTime testCreated2 = LocalDateTime.of(2023, 10, 2, 12, 0, 0);

    @Test
    @Sql(scripts = "/test-data/comments.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByItemIdOrderByCreatedDesc_ShouldReturnCommentsForItemOrderedDesc() {
        List<Comment> result = commentRepository.findByItemIdOrderByCreatedDesc(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCreated()).isEqualTo(testCreated2);
        assertThat(result.get(1).getCreated()).isEqualTo(testCreated1);
        assertThat(result.get(0).getText()).isEqualTo("Comment 2 Text");
    }

    @Test
    @Sql(scripts = "/test-data/comments.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByItemIdOrderByCreatedDesc_ForNonExistentItem_ShouldReturnEmptyList() {
        List<Comment> result = commentRepository.findByItemIdOrderByCreatedDesc(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @Sql(scripts = "/test-data/items-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByItemIdInOrderByCreatedDesc_ShouldReturnCommentsForMultipleItemsOrderedDesc() {
        List<Comment> result = commentRepository.findByItemIdInOrderByCreatedDesc(List.of(1L, 2L));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCreated()).isAfterOrEqualTo(result.get(1).getCreated());
    }

    @Test
    @Sql(scripts = "/test-data/items-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByItemIdInOrderByCreatedDesc_EmptyList_ShouldReturnEmpty() {
        List<Comment> result = commentRepository.findByItemIdInOrderByCreatedDesc(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    void saveComment_ShouldPersistAndReturnComment() {
        User author = new User();
        author.setName("Test Author");
        author.setEmail("author@example.com");
        entityManager.persistAndFlush(author);

        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(author);  // Добавлено: установите владельца
        entityManager.persistAndFlush(item);

        Comment comment = new Comment();
        comment.setText("New Comment");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(testCreated1);

        Comment saved = commentRepository.save(comment);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("New Comment");
        assertThat(saved.getItem().getId()).isEqualTo(item.getId());
        assertThat(saved.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(saved.getCreated()).isEqualTo(testCreated1);

        Comment found = commentRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getText()).isEqualTo("New Comment");
    }

    @Test
    @Transactional
    void deleteComment_ShouldRemoveComment() {
        User author = new User();
        author.setName("Test Author");
        author.setEmail("author@example.com");
        entityManager.persistAndFlush(author);

        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(author);  // Добавлено
        entityManager.persistAndFlush(item);

        Comment comment = new Comment();
        comment.setText("Comment to Delete");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(testCreated1);

        Comment saved = commentRepository.save(comment);
        entityManager.flush();

        commentRepository.deleteById(saved.getId());
        entityManager.flush();

        assertThat(commentRepository.findById(saved.getId())).isEmpty();
    }
}