package com.helpdesk.service;

import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.RegisterRequest;
import com.helpdesk.dto.LoginRequest;

import com.helpdesk.entity.AppUser;
import com.helpdesk.entity.Role;

import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.AuthenticationException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Register a new user
    public AuthResponse register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        // Create user
        AppUser user = new AppUser();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Hash password using BCrypt
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Never allow the client to choose ADMIN
        user.setRole(Role.USER);

        // Save user in PostgreSQL
        AppUser savedUser = userRepository.save(user);

        // Generate JWT
        String token = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    // Login an existing user
    public AuthResponse login(LoginRequest request) {

        // Authenticate email and password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Find authenticated user
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid email or password"
                        )
                );

        // Generate JWT
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole().name()
        );
    }
}