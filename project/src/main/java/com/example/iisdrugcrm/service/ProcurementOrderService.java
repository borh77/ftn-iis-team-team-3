package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.procurement.ConfirmProcurementRequestDTO;
import com.example.iisdrugcrm.dto.procurement.ProcurementOrderResponseDTO;
import java.util.List;

public interface ProcurementOrderService {

    ProcurementOrderResponseDTO confirm(String username, ConfirmProcurementRequestDTO request);

    List<ProcurementOrderResponseDTO> listMine(String username);

    ProcurementOrderResponseDTO getMine(String username, Long id);
}
