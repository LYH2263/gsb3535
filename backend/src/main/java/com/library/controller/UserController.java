package com.library.controller;

import com.library.common.ApiResponse;
import com.library.domain.LibraryUser;
import com.library.dto.UserRequest;
import com.library.service.UserService;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<LibraryUser>> list() {
        return ApiResponse.success(userService.listAll());
    }

    @PostMapping
    public ApiResponse<LibraryUser> create(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success(userService.create(request, "123456"));
    }

    @PutMapping("/{id}")
    public ApiResponse<LibraryUser> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success();
    }
}
