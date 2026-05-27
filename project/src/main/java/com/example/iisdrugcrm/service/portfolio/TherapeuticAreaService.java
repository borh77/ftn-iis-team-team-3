package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaCreateDTO;
import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaUpdateDTO;

import java.util.List;

public interface TherapeuticAreaService {

    List<TherapeuticAreaResponseDTO> getAllActive();

    TherapeuticAreaResponseDTO create(TherapeuticAreaCreateDTO dto);

    TherapeuticAreaResponseDTO update(Long id, TherapeuticAreaUpdateDTO dto);

    void archive(Long id);
}