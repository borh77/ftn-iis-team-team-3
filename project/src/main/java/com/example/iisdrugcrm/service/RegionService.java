package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.RegionDTO;
import java.util.List;

public interface RegionService {

    List<RegionDTO> getAllRegions();

    RegionDTO createRegion(RegionDTO dto);

    RegionDTO updateRegion(Long id, RegionDTO dto);

    void deleteRegion(Long id);
}