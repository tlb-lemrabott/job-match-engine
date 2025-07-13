package com.jobfitengine.code.service;

import com.jobfitengine.code.dto.AuthResponse;
import com.jobfitengine.code.entity.User;
import com.jobfitengine.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationProvider authenticationProvider;
    
    public AuthResponse registerUser(String name, String email, String password, String phone) {
        if (userRepository.existsByEmail(email)) {
            return new AuthResponse(false, "User with this email already exists", null, null);
        }
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        
        User savedUser = userRepository.save(user);
        
        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());
        
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName()
        );
        
        return new AuthResponse(true, "User registered successfully", token, userDto);
    }
    
    public AuthResponse authenticateUser(String email, String password) {
        try {
            authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return new AuthResponse(false, "User not found", null, null);
            }
            
            User user = userOpt.get();
            String token = jwtService.generateToken(user.getId(), user.getEmail());
            
            AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                    user.getId(),
                    user.getEmail(),
                    user.getName()
            );
            
            return new AuthResponse(true, "Login successful", token, userDto);
            
        } catch (Exception e) {
            return new AuthResponse(false, "Invalid email or password", null, null);
        }
    }
    
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
} 