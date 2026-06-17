package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.CreateSpecialOfferDTO;
import com.example.iisdrugcrm.dto.pricelist.SpecialOfferResponseDTO;
import com.example.iisdrugcrm.service.SpecialOfferService;
import com.example.iisdrugcrm.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/special-offers")
@PreAuthorize("hasRole('PRICELIST_CREATOR')")
public class SpecialOfferController {

    private final SpecialOfferService specialOfferService;
    private final UserService userService;

    public SpecialOfferController(SpecialOfferService specialOfferService, UserService userService) {
        this.specialOfferService = specialOfferService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<SpecialOfferResponseDTO> create(@Valid @RequestBody CreateSpecialOfferDTO dto, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(specialOfferService.createOffer(dto, currentUserId));
    }

    @GetMapping("/pricelist/{pricelistId}")
    public ResponseEntity<List<SpecialOfferResponseDTO>> listForPricelist(@PathVariable Long pricelistId, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(specialOfferService.listOffersForPricelist(pricelistId, currentUserId));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<SpecialOfferResponseDTO> activate(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(specialOfferService.activateOffer(id, currentUserId));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<SpecialOfferResponseDTO> archive(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(specialOfferService.archiveOffer(id, currentUserId));
    }
}
