package com.bibliotech.rental.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

public class BorrowRequest {
    @NotNull
    private Long bookId;

    @NotNull
    @FutureOrPresent
    private LocalDate dueDate;

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
