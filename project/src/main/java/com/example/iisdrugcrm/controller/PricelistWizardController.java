package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.PricelistWizardStateDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistWizardSummaryDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveBasicInfoStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveItemsStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveTeamAccessStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveThresholdsStepDTO;
import com.example.iisdrugcrm.dto.pricelist.StartPricelistWizardResponseDTO;
import com.example.iisdrugcrm.service.PricelistWizardService;
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
@RequestMapping("/api/cenovnici")
@PreAuthorize("hasRole('PRICELIST_CREATOR')")
public class PricelistWizardController {

    private final PricelistWizardService wizardService;
    private final UserService userService;

    public PricelistWizardController(PricelistWizardService wizardService, UserService userService) {
        this.wizardService = wizardService;
        this.userService = userService;
    }

    @PostMapping("/wizard")
    public ResponseEntity<StartPricelistWizardResponseDTO> start(Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(wizardService.startWizard(currentUserId));
    }

    @GetMapping("/wizard/drafts")
    public ResponseEntity<List<PricelistWizardStateDTO>> drafts(Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.ok(wizardService.getDrafts(currentUserId));
    }

    @GetMapping("/{id}/wizard")
    public ResponseEntity<PricelistWizardStateDTO> state(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.ok(wizardService.getWizardState(id, currentUserId));
    }

    @PutMapping("/{id}/wizard/basic-info")
    public ResponseEntity<PricelistWizardStateDTO> saveBasicInfo(
            @PathVariable Long id,
            @Valid @RequestBody SaveBasicInfoStepDTO dto,
            Authentication authentication
    ) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.ok(wizardService.saveBasicInfo(id, dto, currentUserId));
    }

    @PutMapping("/{id}/wizard/team-access")
    public ResponseEntity<PricelistWizardStateDTO> saveTeamAccess(
            @PathVariable Long id,
            @Valid @RequestBody SaveTeamAccessStepDTO dto,
            Authentication authentication
    ) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.ok(wizardService.saveTeamAccess(id, dto, currentUserId));
    }

    @PutMapping("/{id}/wizard/items")
    public ResponseEntity<PricelistWizardStateDTO> saveItems(
            @PathVariable Long id,
            @Valid @RequestBody SaveItemsStepDTO dto,
            Authentication authentication
    ) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.ok(wizardService.saveItems(id, dto, currentUserId));
    }

    @PutMapping("/{id}/wizard/thresholds")
    public ResponseEntity<PricelistWizardStateDTO> saveThresholds(
            @PathVariable Long id,
            @Valid @RequestBody SaveThresholdsStepDTO dto,
            Authentication authentication
    ) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.ok(wizardService.saveThresholds(id, dto, currentUserId));
    }

    @GetMapping("/{id}/wizard/summary")
    public ResponseEntity<PricelistWizardSummaryDTO> summary(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.ok(wizardService.getSummary(id, currentUserId));
    }

    @PostMapping("/{id}/wizard/finish")
    public ResponseEntity<PricelistWizardStateDTO> finish(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        return ResponseEntity.ok(wizardService.finishWizard(id, currentUserId));
    }

    private Long currentUserId(Authentication authentication) {
        return userService.getUserIdByUsername(authentication.getName());
    }
}
