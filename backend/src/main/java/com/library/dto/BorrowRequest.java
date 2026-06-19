package com.library.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class BorrowRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long bookId;
    @NotNull
    @Min(1)
    private Integer borrowDays;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getBorrowDays() {
        return borrowDays;
    }

    public void setBorrowDays(Integer borrowDays) {
        this.borrowDays = borrowDays;
    }
}
