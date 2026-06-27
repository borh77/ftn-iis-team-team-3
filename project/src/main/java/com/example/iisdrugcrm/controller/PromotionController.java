package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.PromotionSuggestionDTO;
import com.example.iisdrugcrm.service.PromotionSuggestionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
@PreAuthorize("hasRole('PRICELIST_CREATOR')")
public class PromotionController {

    private final PromotionSuggestionService promotionSuggestionService;

    public PromotionController(PromotionSuggestionService promotionSuggestionService) {
        this.promotionSuggestionService = promotionSuggestionService;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<PromotionSuggestionDTO>> getSuggestions(@RequestParam String segment) {
        return ResponseEntity.ok(promotionSuggestionService.getSuggestions(segment));
    }
}
