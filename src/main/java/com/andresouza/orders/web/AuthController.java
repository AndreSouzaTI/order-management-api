package com.andresouza.orders.web;

import com.andresouza.orders.service.AuthService;
import com.andresouza.orders.web.dto.AuthDtos.LoginRequest;
import com.andresouza.orders.web.dto.AuthDtos.RegisterRequest;
import com.andresouza.orders.web.dto.AuthDtos.TokenResponse;
import com.andresouza.orders.web.dto.AuthDtos.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo cliente")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica e devolve um token JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
