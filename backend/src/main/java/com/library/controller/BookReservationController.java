package com.library.controller;

import com.library.common.ApiResponse;
import com.library.domain.BookReservation;
import com.library.domain.LibraryUser;
import com.library.dto.BookReservationRequest;
import com.library.service.BookReservationService;
import com.library.service.UserService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/book-reservations")
public class BookReservationController {
    private final BookReservationService reservationService;
    private final UserService userService;

    public BookReservationController(BookReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BookReservation>> listAll(
            @RequestParam(required = false) String bookKeyword,
            @RequestParam(required = false) String userKeyword) {
        return ApiResponse.success(reservationService.listAll(bookKeyword, userKeyword));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<BookReservation>> listByUser(@PathVariable Long userId) {
        if (!isAdmin() && !isSelf(userId)) {
            return ApiResponse.error(403, "无权查看他人预约");
        }
        return ApiResponse.success(reservationService.listByUser(userId));
    }

    @PostMapping
    public ApiResponse<BookReservation> reserve(@Valid @RequestBody BookReservationRequest request) {
        if (!isAdmin() && !isSelf(request.getUserId())) {
            return ApiResponse.error(403, "无权为他人预约");
        }
        return ApiResponse.success(reservationService.reserve(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<BookReservation> cancel(@PathVariable Long id) {
        BookReservation existing = reservationService.getById(id);
        if (existing == null) {
            return ApiResponse.error(404, "预约记录不存在");
        }
        if (!isAdmin() && !isSelf(existing.getUserId())) {
            return ApiResponse.error(403, "无权取消他人预约");
        }
        return ApiResponse.success(reservationService.cancel(id));
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
