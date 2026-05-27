package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.TherapeuticArea;
import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaCreateDTO;
import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaUpdateDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.TherapeuticAreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TherapeuticAreaServiceImpl implements TherapeuticAreaService {

    private final TherapeuticAreaRepository therapeuticAreaRepository;

    public TherapeuticAreaServiceImpl(TherapeuticAreaRepository therapeuticAreaRepository) {
        this.therapeuticAreaRepository = therapeuticAreaRepository;
    }

    @Override
    public List<TherapeuticAreaResponseDTO> getAllActive() {
        return therapeuticAreaRepository.findByStatus(EntityStatus.ACTIVE)
                .stream()
                .map(TherapeuticAreaResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public TherapeuticAreaResponseDTO create(TherapeuticAreaCreateDTO dto) {
        if (therapeuticAreaRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new PortfolioDuplicateResourceException("Therapeutic area name already exists");
        }

        TherapeuticArea area = new TherapeuticArea(dto.getName(), dto.getDescription());
        return TherapeuticAreaResponseDTO.fromEntity(therapeuticAreaRepository.save(area));
    }

    @Override
    @Transactional
    public TherapeuticAreaResponseDTO update(Long id, TherapeuticAreaUpdateDTO dto) {
        TherapeuticArea area = getTherapeuticArea(id);

        if (therapeuticAreaRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new PortfolioDuplicateResourceException("Therapeutic area name already exists");
        }

        area.update(dto.getName(), dto.getDescription());
        return TherapeuticAreaResponseDTO.fromEntity(therapeuticAreaRepository.save(area));
    }

    @Override
    @Transactional
    public void archive(Long id) {
        TherapeuticArea area = getTherapeuticArea(id);
        area.archive();
        therapeuticAreaRepository.save(area);
    }

    private TherapeuticArea getTherapeuticArea(Long id) {
        return therapeuticAreaRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Therapeutic area not found"));
    }
}