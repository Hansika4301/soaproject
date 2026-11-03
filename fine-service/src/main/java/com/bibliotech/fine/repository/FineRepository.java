package com.bibliotech.fine.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bibliotech.fine.entity.Fine;

public interface FineRepository extends JpaRepository<Fine, Long> {
    Optional<Fine> findByRentalId(Long rentalId);
}
