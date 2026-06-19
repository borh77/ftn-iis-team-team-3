package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.offer.CreateOfferRequestDTO;
import com.example.iisdrugcrm.dto.sales.offer.OfferResponseDTO;
import com.example.iisdrugcrm.service.sales.OfferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    public OfferResponseDTO create(@Valid @RequestBody CreateOfferRequestDTO dto) {
        return offerService.create(dto);
    }

    @GetMapping
    public List<OfferResponseDTO> getAll() {
        return offerService.getAll();
    }

    @GetMapping("/{id}")
    public OfferResponseDTO getById(@PathVariable Long id) {
        return offerService.getById(id);
    }

    @PatchMapping("/{id}/accept")
    public OfferResponseDTO accept(@PathVariable Long id) {
        return offerService.acceptOffer(id);
    }
}