package com.bibliotech.rental.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.bibliotech.rental.dto.BorrowRequest;
import com.bibliotech.rental.entity.Rental;
import com.bibliotech.rental.repository.RentalRepository;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final RestTemplate restTemplate;

    public RentalService(RentalRepository rentalRepository, RestTemplate restTemplate) {
        this.rentalRepository = rentalRepository;
        this.restTemplate = restTemplate;
    }

    public Rental borrowBook(Long userId, BorrowRequest request) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Valid authenticated user is required");
        }
        if (!request.getDueDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Due date must be after today");
        }
        if (rentalRepository.existsByUserIdAndBookIdAndStatus(userId, request.getBookId(), "ACTIVE")) {
            throw new IllegalStateException("This user already has an active rental for this book");
        }

        try {
            restTemplate.put("http://BOOK-SERVICE/books/" + request.getBookId() + "/issue", null);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to contact Book Service or book is unavailable");
        }

        Rental rental = new Rental();
        rental.setUserId(userId);
        rental.setBookId(request.getBookId());
        rental.setIssueDate(LocalDate.now());
        rental.setDueDate(request.getDueDate());
        rental.setStatus("ACTIVE");
        try {
            return rentalRepository.save(rental);
        } catch (RuntimeException ex) {
            try {
                restTemplate.put("http://BOOK-SERVICE/books/" + request.getBookId() + "/return", null);
            } catch (RestClientException ignored) { }
            throw ex;
        }
    }

    public Rental returnBook(Long rentalId, Long requestingUserId, boolean admin) {
        Rental rental = getRental(rentalId);
        if (!admin && !rental.getUserId().equals(requestingUserId)) {
            throw new SecurityException("You can return only your own rental");
        }
        if (!"ACTIVE".equals(rental.getStatus())) {
            throw new IllegalStateException("This rental has already been returned");
        }

        try {
            restTemplate.put("http://BOOK-SERVICE/books/" + rental.getBookId() + "/return", null);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to contact Book Service while returning the book");
        }

        rental.setReturnDate(LocalDate.now());
        rental.setStatus("RETURNED");
        return rentalRepository.save(rental);
    }

    public List<Rental> getAllRentals() { return rentalRepository.findAll(); }

    public List<Rental> getMyRentals(Long userId) { return rentalRepository.findByUserId(userId); }

    public Rental getRental(Long id) {
        return rentalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rental not found: " + id));
    }

    public Rental updateDueDate(Long id, LocalDate dueDate) {
        Rental rental = getRental(id);
        if (!"ACTIVE".equals(rental.getStatus())) throw new IllegalStateException("Cannot update a returned rental");
        if (dueDate == null || !dueDate.isAfter(rental.getIssueDate())) throw new IllegalArgumentException("Due date must be after issue date");
        rental.setDueDate(dueDate);
        return rentalRepository.save(rental);
    }

    public void deleteRental(Long id) {
        Rental rental = getRental(id);
        if ("ACTIVE".equals(rental.getStatus())) throw new IllegalStateException("Return the book before deleting the rental");
        rentalRepository.delete(rental);
    }
}
