package com.example.iisdrugcrm.service.adverse;

import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportResponseDTO;
import com.example.iisdrugcrm.dto.adverse.CreateDoctorReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreatePatientReportRequestDTO;

import java.util.List;

public interface AdverseEffectReportService {

    // US-01: Lekar kreira nalog
    AdverseEffectReportResponseDTO createDoctorReport(CreateDoctorReportRequestDTO dto, String username);

    // US-02: Pacijent kreira nalog
    AdverseEffectReportResponseDTO createPatientReport(CreatePatientReportRequestDTO dto, String username);

    // US-03: Lekar vidi svoje naloge
    List<AdverseEffectReportResponseDTO> getMyReports(String username);

    // US-04: Farmakovigilant vidi sve naloge
    List<AdverseEffectReportResponseDTO> getAllReports();

    // US-03: Detalji jednog naloga
    AdverseEffectReportResponseDTO getReportById(Long id);
}
