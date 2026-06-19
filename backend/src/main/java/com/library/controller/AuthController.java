package com.library.controller;

import com.library.common.ApiResponse;
import com.library.domain.LibraryUser;
import com.library.dto.LoginRequest;
import com.library.dto.RegisterRequest;
import com.library.security.JwtTokenProvider;
import com.library.service.UserService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        LibraryUser user = userService.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        String token = tokenProvider.generateToken(user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("fullName", user.getFullName());
        result.put("role", user.getRole());
        return ApiResponse.success(result);
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.findByUsername(request.getUsername()) != null) {
            return ApiResponse.error(409, "用户名已存在");
        }
        com.library.dto.UserRequest userRequest = new com.library.dto.UserRequest();
        userRequest.setUsername(request.getUsername());
        userRequest.setFullName(request.getFullName());
        userRequest.setRole("USER");
        LibraryUser user = userService.create(userRequest, request.getPassword());
        String token = tokenProvider.generateToken(user.getUsername(), user.getRole());
        Map<String, Object> result = Map.of(
            "token", token,
            "id", user.getId(),
            "username", user.getUsername(),
            "fullName", user.getFullName(),
            "role", user.getRole()
        );
        return ApiResponse.success(result);
    }
}
