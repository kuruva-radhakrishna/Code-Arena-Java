package com.codearena.backend.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codearena.backend.exception.EmailAlreadyExistsException;
import com.codearena.backend.exception.InvalidCredentialsException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.security.JwtService;
import com.codearena.backend.user.dto.AuthResponse;
import com.codearena.backend.user.dto.LoginRequest;
import com.codearena.backend.user.dto.RegisterRequest;
import com.codearena.backend.user.dto.UserResponse;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    public UserResponse getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.from(user);
    }
}
