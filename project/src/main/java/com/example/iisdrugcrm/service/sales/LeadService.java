package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Lead;
import com.example.iisdrugcrm.dto.sales.lead.LeadRequestDTO;
import com.example.iisdrugcrm.dto.sales.lead.LeadResponseDTO;
import com.example.iisdrugcrm.repository.sales.LeadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadService {

    private final LeadRepository leadRepository;

    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public List<LeadResponseDTO> getAll() {
        return leadRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public LeadResponseDTO create(LeadRequestDTO dto) {
        if (leadRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Lead with this email already exists.");
        }

        Lead lead = new Lead(
                dto.getName(),
                dto.getEmail(),
                dto.getAddress(),
                dto.getSource(),
                dto.getScore()
        );

        return mapToDto(leadRepository.save(lead));
    }

    public LeadResponseDTO update(Long id, LeadRequestDTO dto) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found."));

        lead.update(dto.getName(), dto.getEmail(), dto.getAddress(), dto.getSource(), dto.getScore());

        return mapToDto(leadRepository.save(lead));
    }

    public LeadResponseDTO qualify(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found."));

        lead.qualify();

        return mapToDto(leadRepository.save(lead));
    }

    private LeadResponseDTO mapToDto(Lead lead) {
        return new LeadResponseDTO(
                lead.getId(),
                lead.getName(),
                lead.getEmail(),
                lead.getAddress(),
                lead.getSource(),
                lead.getScore(),
                lead.getStatus(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }
}