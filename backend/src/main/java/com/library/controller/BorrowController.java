package com.library.controller;

import com.library.common.ApiResponse;
import com.library.domain.BorrowRecord;
import com.library.domain.LibraryUser;
import com.library.dto.BorrowRequest;
import com.library.dto.ReturnRequest;
import com.library.service.BorrowService;
import com.library.service.UserService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {
    private final BorrowService borrowService;
    private final UserService userService;

    public BorrowController(BorrowService borrowService, UserService userService) {
        this.borrowService = borrowService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BorrowRecord>> listAll() {
        return ApiResponse.success(borrowService.listAll());
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<BorrowRecord>> listByUser(@PathVariable Long userId) {
        if (!isAdmin() && !isSelf(userId)) {
            return ApiResponse.success(List.of());
        }
        return ApiResponse.success(borrowService.listByUser(userId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BorrowRecord>> history() {
        return ApiResponse.success(borrowService.listHistory());
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BorrowRecord>> overdue() {
        return ApiResponse.success(borrowService.listOverdue());
    }

    @PostMapping
    public ApiResponse<BorrowRecord> borrow(@Valid @RequestBody BorrowRequest request) {
        if (!isAdmin() && !isSelf(request.getUserId())) {
            return ApiResponse.error(403, "无权为他人借书");
        }
        return ApiResponse.success(borrowService.borrow(request));
    }

    @PostMapping("/return")
    public ApiResponse<BorrowRecord> returnBook(@Valid @RequestBody ReturnRequest request) {
        BorrowRecord existing = borrowService.getById(request.getBorrowId());
        if (existing == null) {
            return ApiResponse.error(404, "借阅记录不存在");
        }
        if (!isAdmin() && !isSelf(existing.getUserId())) {
            return ApiResponse.error(403, "无权归还他人借书");
        }
        return ApiResponse.success(borrowService.returnBook(request.getBorrowId()));
    }

    private boolean isAdmin() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return false;
        }
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");
        return "ROLE_ADMIN".equals(role);
    }

    private boolean isSelf(Long userId) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return false;
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LibraryUser user = userService.findByUsername(username);
        return user != null && user.getId().equals(userId);
    }
}
