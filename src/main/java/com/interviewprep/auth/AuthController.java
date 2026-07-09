package com.interviewprep.auth;

import com.interviewprep.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new account")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody AuthRequest request) {
        String token = authService.register(request.username(), request.password());
        return ApiResponse.success("Registration successful", new LoginResponse(token, request.username()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody AuthRequest request) {
        String token = authService.login(request.username(), request.password());
        return ApiResponse.success("Login successful", new LoginResponse(token, request.username()));
    }

    public record AuthRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Password is required") String password) {
    }

    public record LoginResponse(String token, String username) {
    }
}
