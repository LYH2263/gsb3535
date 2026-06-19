package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.ApiResponse;
import com.library.domain.Book;
import com.library.dto.BookRequest;
import com.library.service.BookService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<IPage<Book>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(bookService.listPage(current, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> get(@PathVariable Long id) {
        return ApiResponse.success(bookService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Book> create(@Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Book> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ApiResponse.success();
    }
}
