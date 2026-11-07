package com.jewellery.erp.identity.api;

import com.jewellery.erp.identity.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthDtos.UserInfo me(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }

    /** JWT is stateless — client discards the token. Endpoint exists for explicit logout UX/API symmetry. */
    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("status", "ok");
    }
}
