package com.bibliotech.fine.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.bibliotech.fine.dto.FineCalculationRequest;
import com.bibliotech.fine.entity.Fine;
import com.bibliotech.fine.service.FineService;

@RestController
@RequestMapping("/fines")
public class FineController {
    private final FineService service;

    public FineController(FineService service) {
        this.service = service;
    }

    @PostMapping("/calculate")
    @ResponseStatus(HttpStatus.CREATED)
    public Fine calculate(@Valid @RequestBody FineCalculationRequest request) {
        return service.calculate(request);
    }

    @GetMapping
    public List<Fine> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Fine findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/rental/{rentalId}")
    public Fine findByRentalId(@PathVariable Long rentalId) {
        return service.findByRentalId(rentalId);
    }

    @PatchMapping("/{id}/pay")
    public Fine markPaid(@PathVariable Long id) {
        return service.markPaid(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
