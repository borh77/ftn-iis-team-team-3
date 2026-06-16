package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Contract;
import com.example.iisdrugcrm.domain.sales.Offer;
import com.example.iisdrugcrm.domain.sales.OfferStatus;
import com.example.iisdrugcrm.dto.sales.contract.ContractResponseDTO;
import com.example.iisdrugcrm.dto.sales.contract.CreateContractRequestDTO;
import com.example.iisdrugcrm.repository.sales.ContractRepository;
import com.example.iisdrugcrm.repository.sales.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final OfferRepository offerRepository;

    public ContractService(
            ContractRepository contractRepository,
            OfferRepository offerRepository
    ) {
        this.contractRepository = contractRepository;
        this.offerRepository = offerRepository;
    }

    @Transactional
    public ContractResponseDTO create(CreateContractRequestDTO dto) {

        Offer offer = offerRepository.findById(dto.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException("Offer not found."));

        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new IllegalArgumentException(
                    "Contract can only be created from accepted offer."
            );
        }

        if (contractRepository.existsByOfferId(offer.getId())) {
            throw new IllegalArgumentException(
                    "Contract already exists for this offer."
            );
        }

        Contract contract = new Contract(
                generateContractNumber(),
                offer,
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getTerms()
        );

        return mapToDto(contractRepository.save(contract));
    }

    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getAll() {
        return contractRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContractResponseDTO getById(Long id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found."));

        return mapToDto(contract);
    }

    private String generateContractNumber() {

        Long nextId = contractRepository.findTopByOrderByIdDesc()
                .map(Contract::getId)
                .orElse(0L) + 1;

        return "CNT-" + String.format("%05d", nextId);
    }

    @Transactional
    public ContractResponseDTO sign(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found."));

        contract.markAsSigned();

        return mapToDto(contract);
    }

    private ContractResponseDTO mapToDto(Contract contract) {

        return new ContractResponseDTO(
                contract.getId(),
                contract.getContractNumber(),
                contract.getOffer().getId(),
                contract.getOffer().getOfferNumber(),
                contract.getCustomer().getId(),
                contract.getCustomer().getName(),
                contract.getSalesProcess().getId(),
                contract.getSalesProcess().getTitle(),
                contract.getStatus(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getTotalValue(),
                contract.getTerms(),
                contract.getCreatedAt(),
                contract.getSignedAt()
        );
    }
}