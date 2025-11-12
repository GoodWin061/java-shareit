package user;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ShareItServer.class)
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void testAddNewUser() {
        NewUserDto dto = new NewUserDto();
        dto.setName("Test User");
        dto.setEmail("test@example.com");

        UserDto result = userService.addNewUser(dto);

        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void testUpdateUser() {
        NewUserDto newDto = new NewUserDto();
        newDto.setName("Original");
        newDto.setEmail("original@example.com");
        UserDto created = userService.addNewUser(newDto);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setId(created.getId());
        updateDto.setName("Updated");

        UserDto updated = userService.updateUser(updateDto);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getEmail()).isEqualTo("original@example.com");
    }

    @Test
    void testGetUserById() {
        NewUserDto dto = new NewUserDto();
        dto.setName("Test");
        dto.setEmail("test@example.com");
        UserDto created = userService.addNewUser(dto);

        UserDto found = userService.getUserById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
    }

    @Test
    void testDeleteUser() {
        NewUserDto dto = new NewUserDto();
        dto.setName("Test");
        dto.setEmail("test@example.com");
        UserDto created = userService.addNewUser(dto);

        userService.deleteUser(created.getId());

        assertThatThrownBy(() -> userService.getUserById(created.getId()))
                .isInstanceOf(RuntimeException.class);
    }
}