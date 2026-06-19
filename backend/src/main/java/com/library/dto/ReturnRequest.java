package com.library.dto;

import javax.validation.constraints.NotNull;

public class ReturnRequest {
    @NotNull
    private Long borrowId;

    public Long getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Long borrowId) {
        this.borrowId = borrowId;
    }
}
