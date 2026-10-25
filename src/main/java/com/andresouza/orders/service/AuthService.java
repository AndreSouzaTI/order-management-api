package com.andresouza.orders.service;

import com.andresouza.orders.domain.AppUser;
import com.andresouza.orders.domain.Role;
import com.andresouza.orders.exception.BusinessException;
import com.andresouza.orders.repository.UserRepository;
import com.andresouza.orders.security.TokenService;
import com.andresouza.orders.web.dto.AuthDtos.LoginRequest;
import com.andresouza.orders.web.dto.AuthDtos.RegisterRequest;
import com.andresouza.orders.web.dto.AuthDtos.TokenResponse;
import com.andresouza.orders.web.dto.AuthDtos.UserResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("E-mail já cadastrado");
        }
        AppUser user = users.save(new AppUser(request.name(), request.email().toLowerCase(),
                passwordEncoder.encode(request.password()), Role.USER));
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));
        return new TokenResponse(tokenService.generate(user), "Bearer", tokenService.expirationSeconds());
    }
}
