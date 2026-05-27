package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import com.example.iisdrugcrm.service.PricelistService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cenovnici")
@PreAuthorize("hasRole('PRICELIST_CREATOR')")
public class PricelistController {

    private final PricelistService pricelistService;

    public PricelistController(PricelistService pricelistService) {
        this.pricelistService = pricelistService;
    }

    @PostMapping
    public ResponseEntity<PricelistResponseDTO> create(@Valid @RequestBody CreatePricelistDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricelistService.createCenovnik(dto));
    }

    @GetMapping
    public ResponseEntity<List<PricelistResponseDTO>> list() {
        return ResponseEntity.ok(pricelistService.listCenovnici());
    }
}