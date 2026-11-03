package com.bibliotech.rental.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bibliotech.rental.dto.BorrowRequest;
import com.bibliotech.rental.entity.Rental;
import com.bibliotech.rental.service.RentalService;

@RestController
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    public RentalController(RentalService rentalService) { this.rentalService = rentalService; }

    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                        @Valid @RequestBody BorrowRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.borrowBook(userId, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                        @RequestHeader(value = "X-Role", required = false, defaultValue = "USER") String role) {
        try {
            boolean admin = "ADMIN".equalsIgnoreCase(role) || "LIBRARIAN".equalsIgnoreCase(role);
            return ResponseEntity.ok(rentalService.returnBook(id, userId, admin));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public List<Rental> getRentals(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                   @RequestHeader(value = "X-Role", required = false, defaultValue = "USER") String role) {
        if ("ADMIN".equalsIgnoreCase(role) || "LIBRARIAN".equalsIgnoreCase(role)) return rentalService.getAllRentals();
        return rentalService.getMyRentals(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRental(@PathVariable Long id,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                       @RequestHeader(value = "X-Role", required = false, defaultValue = "USER") String role) {
        try {
            Rental rental = rentalService.getRental(id);
            if (!"ADMIN".equalsIgnoreCase(role) && !"LIBRARIAN".equalsIgnoreCase(role)
                    && (userId == null || !rental.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
            }
            return ResponseEntity.ok(rental);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/due-date")
    public ResponseEntity<?> updateDueDate(@PathVariable Long id, @RequestBody Map<String, String> body,
                                           @RequestHeader(value = "X-Role", required = false, defaultValue = "USER") String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !"LIBRARIAN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only ADMIN or LIBRARIAN can update due dates"));
        }
        try {
            LocalDate date = LocalDate.parse(body.get("dueDate"));
            return ResponseEntity.ok(rentalService.updateDueDate(id, date));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage() == null ? "Invalid due date" : ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRental(@PathVariable Long id,
                                          @RequestHeader(value = "X-Role", required = false, defaultValue = "USER") String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !"LIBRARIAN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only ADMIN or LIBRARIAN can delete rentals"));
        }
        try {
            rentalService.deleteRental(id);
            return ResponseEntity.ok(Map.of("message", "Rental deleted successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }
}
