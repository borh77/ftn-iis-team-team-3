package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.RegionDTO;
import com.example.iisdrugcrm.service.RegionService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regions")
@PreAuthorize("isAuthenticated()")
public class RegionLookupController {

    private final RegionService regionService;

    public RegionLookupController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping
    public List<RegionDTO> getAllRegions() {
        return regionService.getAllRegions();
    }
}