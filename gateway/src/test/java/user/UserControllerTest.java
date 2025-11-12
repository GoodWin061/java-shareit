package user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void createUser_success() throws Exception {
        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setName("John Doe");
        newUserDto.setEmail("john@example.com");

        UserDto responseDto = new UserDto(1L, "John Doe", "john@example.com");

        when(userClient.createUser(any(NewUserDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void updateUser_success() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setName("Updated Name");

        UserDto responseDto = new UserDto(1L, "Updated Name", "john@example.com");

        when(userClient.updateUser(eq(1L), any(UpdateUserDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateUser_userNotFound() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setName("Updated Name");

        when(userClient.updateUser(eq(999L), any(UpdateUserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Пользователь не найден"));
    }

    @Test
    void getUserById_success() throws Exception {
        UserDto responseDto = new UserDto(1L, "John Doe", "john@example.com");

        when(userClient.getUserById(eq(1L)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getUserById_notFound() throws Exception {
        when(userClient.getUserById(eq(999L)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Пользователь не найден"));
    }

    @Test
    void findAll_success() throws Exception {
        UserDto user1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto user2 = new UserDto(2L, "Jane Doe", "jane@example.com");
        List<UserDto> users = List.of(user1, user2);

        when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok(users));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].name").value("Jane Doe"));
    }

    @Test
    void deleteUser_success() throws Exception {
        when(userClient.deleteUser(eq(1L)))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound() throws Exception {
        when(userClient.deleteUser(eq(999L)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }
}