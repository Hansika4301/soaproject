package com.bibliotech.rental.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bibliotech.rental.entity.Rental;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
    List<Rental> findByUserId(Long userId);
    List<Rental> findByStatus(String status);
}
