package user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceIml;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceIml userService;

    private User user;
    private UserDto userDto;
    private NewUserDto newUserDto;
    private UpdateUserDto updateUserDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Тест пользователя");
        user.setEmail("test@example.com");

        userDto = new UserDto(1L, "Тест пользователя", "test@example.com");

        newUserDto = new NewUserDto();
        newUserDto.setName("Новый пользователь");
        newUserDto.setEmail("new@example.com");

        updateUserDto = new UpdateUserDto();
        updateUserDto.setId(1L);
        updateUserDto.setName("Updated Name");
        updateUserDto.setEmail("updated@example.com");
    }

    @Test
    void getUsers_ShouldReturnListOfUserDtos() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = userService.getUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto, result.iterator().next());
        verify(userRepository).findAll();
        verify(userMapper).toDto(user);
    }

    @Test
    void getUsers_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        var result = userService.getUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void addNewUser_ShouldReturnUserDto_WhenValid() {
        when(userRepository.existsByEmail(newUserDto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(newUserDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = userService.addNewUser(newUserDto);

        assertEquals(userDto, result);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(user);
        verify(userMapper).toEntity(newUserDto);
        verify(userMapper).toDto(user);
    }

    @Test
    void addNewUser_ShouldThrowRuntimeException_WhenEmailExists() {
        when(userRepository.existsByEmail(newUserDto.getEmail())).thenReturn(true);

        var exception = assertThrows(RuntimeException.class, () -> userService.addNewUser(newUserDto));
        assertEquals("Пользователь с email new@example.com уже существует", exception.getMessage());
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserDto_WhenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = userService.updateUser(updateUserDto);

        assertEquals(userDto, result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).save(user);
        verify(userMapper).updateUserFromDto(updateUserDto, user);
        verify(userMapper).toDto(user);
    }

    @Test
    void updateUser_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> userService.updateUser(updateUserDto));
        assertEquals("Пользователь с ID 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrowConflictException_WhenEmailExists() {
        updateUserDto.setEmail("existing@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        var exception = assertThrows(ConflictException.class, () -> userService.updateUser(updateUserDto));
        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrowConflictException_WhenDataIntegrityViolation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenThrow(new DataIntegrityViolationException("Email already exists"));

        var exception = assertThrows(ConflictException.class, () -> userService.updateUser(updateUserDto));
        assertEquals("Ошибка обновления: возможно, email уже используется", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_ShouldNotCheckEmail_WhenEmailUnchanged() {
        updateUserDto.setEmail("test@example.com"); // Тот же email
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = userService.updateUser(updateUserDto);

        assertEquals(userDto, result);
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void getUserById_ShouldReturnUserDto_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = userService.getUserById(1L);

        assertEquals(userDto, result);
        verify(userRepository).findById(1L);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_ShouldThrowRuntimeException_WhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(RuntimeException.class, () -> userService.getUserById(1L));
        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
    }

    @Test
    void deleteUser_ShouldDelete_WhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrowRuntimeException_WhenNotExists() {
        when(userRepository.existsById(1L)).thenReturn(false);

        var exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}