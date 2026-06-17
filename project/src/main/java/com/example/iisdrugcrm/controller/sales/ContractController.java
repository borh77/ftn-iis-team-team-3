package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.contract.ContractResponseDTO;
import com.example.iisdrugcrm.dto.sales.contract.CreateContractRequestDTO;
import com.example.iisdrugcrm.service.sales.ContractService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public ContractResponseDTO create(@Valid @RequestBody CreateContractRequestDTO dto) {
        return contractService.create(dto);
    }

    @GetMapping
    public List<ContractResponseDTO> getAll() {
        return contractService.getAll();
    }

    @GetMapping("/{id}")
    public ContractResponseDTO getById(@PathVariable Long id) {
        return contractService.getById(id);
    }

    @PatchMapping("/{id}/sign")
    public ContractResponseDTO sign(@PathVariable Long id) {
        return contractService.sign(id);
    }
}