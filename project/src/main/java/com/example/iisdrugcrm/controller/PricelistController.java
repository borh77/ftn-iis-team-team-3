package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.ChangePricelistStatusDTO;
import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.ReplacePricelistItemVariantDTO;
import com.example.iisdrugcrm.service.PricelistService;
import com.example.iisdrugcrm.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
public class PricelistController {

    private final PricelistService pricelistService;
    private final UserService userService;

    public PricelistController(PricelistService pricelistService, UserService userService) {
        this.pricelistService = pricelistService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<PricelistResponseDTO> create(@Valid @RequestBody CreatePricelistDTO dto, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(pricelistService.createCenovnik(dto, currentUserId));
    }

    @GetMapping
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<List<PricelistResponseDTO>> list() {
        return ResponseEntity.ok(pricelistService.listCenovnici());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<List<PricelistResponseDTO>> mine(Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.listCenovniciForUser(currentUserId));
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<List<PricelistResponseDTO>> team(Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.listTeamCenovniciForUser(currentUserId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<PricelistResponseDTO> getById(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.getById(id, currentUserId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<PricelistResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CreatePricelistDTO dto, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.update(id, dto, currentUserId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PRICELIST_CREATOR', 'ADMIN')")
    public ResponseEntity<PricelistResponseDTO> changeStatus(@PathVariable Long id, @Valid @RequestBody ChangePricelistStatusDTO dto, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.changeStatus(id, dto, currentUserId, isAdmin(authentication)));
    }

    @PostMapping("/{id}/versions")
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<PricelistResponseDTO> createNewVersion(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(pricelistService.createNewVersion(id, currentUserId));
    }

    @PutMapping("/{pricelistId}/items/{itemId}/replace-variant")
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<PricelistResponseDTO> replaceItemVariant(
            @PathVariable Long pricelistId,
            @PathVariable Long itemId,
            @Valid @RequestBody ReplacePricelistItemVariantDTO dto,
            Authentication authentication
    ) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(pricelistService.replaceItemVariant(pricelistId, itemId, dto.getReplacementVariantId(), currentUserId));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
