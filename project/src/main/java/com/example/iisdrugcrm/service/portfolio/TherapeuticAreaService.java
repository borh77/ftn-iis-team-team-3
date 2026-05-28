package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaResponseDTO;

import java.util.List;

public interface TherapeuticAreaService {

    List<TherapeuticAreaResponseDTO> getAllActive();

    TherapeuticAreaResponseDTO create(TherapeuticAreaRequestDTO dto);

    TherapeuticAreaResponseDTO update(Long id, TherapeuticAreaRequestDTO dto);

    void archive(Long id);
}