package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.ChangePricelistStatusDTO;
import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.ReplacePricelistItemVariantDTO;
import com.example.iisdrugcrm.service.PricelistService;
import com.example.iisdrugcrm.service.UserService;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricelists")
@PreAuthorize("hasRole('PRICELIST_CREATOR')")
public class PricelistController {

    private final PricelistService pricelistService;
    private final UserService userService;

    public PricelistController(PricelistService pricelistService, UserService userService) {
        this.pricelistService = pricelistService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<PricelistResponseDTO> create(@Valid @RequestBody CreatePricelistDTO dto, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(pricelistService.createCenovnik(dto, currentUserId));
    }

    @GetMapping
    public ResponseEntity<List<PricelistResponseDTO>> list() {
        return ResponseEntity.ok(pricelistService.listCenovnici());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<PricelistResponseDTO>> mine(Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.listCenovniciForUser(currentUserId));
    }

    @GetMapping("/team")
    public ResponseEntity<List<PricelistResponseDTO>> team(Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.listTeamCenovniciForUser(currentUserId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PricelistResponseDTO> changeStatus(@PathVariable Long id, @Valid @RequestBody ChangePricelistStatusDTO dto, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.changeStatus(id, dto, currentUserId));
    }

    @PostMapping("/{id}/versions")
    public ResponseEntity<PricelistResponseDTO> createNewVersion(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(pricelistService.createNewVersion(id, currentUserId));
    }

    @PutMapping("/{pricelistId}/items/{itemId}/replace-variant")
    public ResponseEntity<PricelistResponseDTO> replaceItemVariant(
            @PathVariable Long pricelistId,
            @PathVariable Long itemId,
            @Valid @RequestBody ReplacePricelistItemVariantDTO dto,
            Authentication authentication
    ) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.replaceItemVariant(pricelistId, itemId, dto.getReplacementVariantId(), currentUserId));
    }
}
