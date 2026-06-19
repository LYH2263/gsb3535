package com.library.controller;

import com.library.common.ApiResponse;
import com.library.domain.BookReservation;
import com.library.domain.LibraryUser;
import com.library.dto.BookReservationRequest;
import com.library.mapper.BookReservationMapper;
import com.library.service.BookReservationService;
import com.library.service.UserService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-reservations")
public class BookReservationController {
    private final BookReservationService reservationService;
    private final BookReservationMapper reservationMapper;
    private final UserService userService;

    public BookReservationController(BookReservationService reservationService, BookReservationMapper reservationMapper, UserService userService) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BookReservation>> listAll(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(reservationService.listAll(keyword));
    }

    @GetMapping("/my/{userId}")
    public ApiResponse<List<BookReservation>> listMyReservations(@PathVariable Long userId) {
        if (!isAdmin() && !isSelf(userId)) {
            return ApiResponse.success(List.of());
        }
        return ApiResponse.success(reservationService.listByUser(userId));
    }

    @PostMapping
    public ApiResponse<BookReservation> reserve(@Valid @RequestBody BookReservationRequest request) {
        if (!isAdmin() && !isSelf(request.getUserId())) {
            return ApiResponse.error(403, "无权为他人预约");
        }
        try {
            return ApiResponse.success(reservationService.reserve(request));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BookReservation> cancel(@PathVariable Long id) {
        try {
            BookReservation existing = reservationMapper.selectById(id);
            if (existing == null) {
                return ApiResponse.error(404, "预约记录不存在");
            }
            if (!isAdmin() && !isSelf(existing.getUserId())) {
                return ApiResponse.error(403, "无权取消他人预约");
            }
            return ApiResponse.success(reservationService.cancel(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    private LibraryUser getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByUsername(username);
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
        LibraryUser user = getCurrentUser();
        return user != null && user.getId().equals(userId);
    }
}
