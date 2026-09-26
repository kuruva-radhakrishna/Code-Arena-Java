package com.codearena.backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.codearena.backend.exception.EmailAlreadyExistsException;
import com.codearena.backend.exception.InvalidCredentialsException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.security.JwtService;
import com.codearena.backend.user.dto.AuthResponse;
import com.codearena.backend.user.dto.LoginRequest;
import com.codearena.backend.user.dto.RegisterRequest;
import com.codearena.backend.user.dto.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_savesNewUserWithHashedPassword_andReturnsToken() {
        RegisterRequest request = new RegisterRequest("Ada", "Lovelace", "Ada@Example.com", "secret123");
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("generated-id");
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = userService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("ada@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(saved.getRole()).isEqualTo(Role.USER);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("ada@example.com");
    }

    @Test
    void register_throws_whenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("Ada", "Lovelace", "ada@example.com", "secret123");
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request)).isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void login_returnsToken_whenCredentialsMatch() {
        User user = User.builder().id("id-1").email("ada@example.com").passwordHash("hashed").role(Role.USER).build();
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = userService.login(new LoginRequest("ada@example.com", "secret123"));

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_throws_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(new LoginRequest("missing@example.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_whenPasswordDoesNotMatch() {
        User user = User.builder().id("id-1").email("ada@example.com").passwordHash("hashed").role(Role.USER).build();
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(new LoginRequest("ada@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void getById_returnsUser_whenFound() {
        User user = User.builder().id("id-1").firstname("Ada").lastname("Lovelace").email("ada@example.com").role(Role.USER).build();
        when(userRepository.findById("id-1")).thenReturn(Optional.of(user));

        UserResponse response = userService.getById("id-1");

        assertThat(response.email()).isEqualTo("ada@example.com");
    }

    @Test
    void getById_throws_whenNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById("missing")).isInstanceOf(ResourceNotFoundException.class);
    }
}
