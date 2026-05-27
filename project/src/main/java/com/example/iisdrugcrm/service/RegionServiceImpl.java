package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.dto.RegionDTO;
import com.example.iisdrugcrm.exception.RegionConflictException;
import com.example.iisdrugcrm.exception.RegionInUseException;
import com.example.iisdrugcrm.repository.RegionRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    public RegionServiceImpl(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @Override
    public List<RegionDTO> getAllRegions() {
        return regionRepository.findAll().stream().map(RegionDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public RegionDTO createRegion(RegionDTO dto) {
        ensureUniqueName(dto.getName(), null);

        Region region = new Region(normalize(dto.getName()), normalize(dto.getCode()));
        return RegionDTO.fromEntity(regionRepository.save(region));
    }

    @Override
    @Transactional
    public RegionDTO updateRegion(Long id, RegionDTO dto) {
        Region region = getRegion(id);
        ensureUniqueName(dto.getName(), id);

        region.setName(normalize(dto.getName()));
        region.setCode(normalize(dto.getCode()));
        return RegionDTO.fromEntity(regionRepository.save(region));
    }

    @Override
    @Transactional
    public void deleteRegion(Long id) {
        Region region = getRegion(id);

        try {
            regionRepository.delete(region);
            regionRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RegionInUseException("Nije moguće obrisati region jer ga koriste aktivni korisnici ili cenovnici");
        }
    }

    private Region getRegion(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));
    }

    private void ensureUniqueName(String name, Long currentId) {
        String normalizedName = normalize(name);

        boolean duplicateName = currentId == null
                ? regionRepository.existsByNameIgnoreCase(normalizedName)
                : regionRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, currentId);
        if (duplicateName) {
            throw new RegionConflictException("Region sa tim imenom već postoji");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}