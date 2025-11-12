import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommentDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidation_whenTextIsValid() {
        CommentDto commentDto = new CommentDto(
                1L,
                "Это корректный комментарий.",
                "Автор",
                LocalDateTime.now()
        );

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidation_whenTextIsNull() {
        CommentDto commentDto = new CommentDto(
                1L,
                null,  // null text
                "Автор",
                LocalDateTime.now()
        );

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Текст комментария не может быть пустым или состоять только из пробелов");
    }

    @Test
    void shouldFailValidation_whenTextIsEmpty() {
        CommentDto commentDto = new CommentDto(
                1L,
                "",  // Пустая строка
                "Автор",
                LocalDateTime.now()
        );

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Текст комментария не может быть пустым или состоять только из пробелов");
    }

    @Test
    void shouldFailValidation_whenTextIsBlank() {
        CommentDto commentDto = new CommentDto(
                1L,
                "   ",  // Только пробелы
                "Автор",
                LocalDateTime.now()
        );

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Текст комментария не может быть пустым или состоять только из пробелов");
    }

    @Test
    void shouldPassValidation_whenOtherFieldsAreNull() {
        CommentDto commentDto = new CommentDto(
                null,
                "Корректный текст.",
                null,
                null
        );

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertThat(violations).isEmpty();
    }
}