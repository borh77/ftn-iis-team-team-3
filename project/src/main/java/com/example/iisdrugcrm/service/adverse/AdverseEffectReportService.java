package com.example.iisdrugcrm.service.adverse;

import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AddNoteRequestDTO;
import com.example.iisdrugcrm.dto.adverse.AnalystNoteResponseDTO;
import com.example.iisdrugcrm.dto.adverse.ChangeStatusRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreateDoctorReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreatePatientReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.StatusTransitionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.UpdateDoctorReportRequestDTO;

import java.util.List;

public interface AdverseEffectReportService {

    // US-01: Lekar kreira nalog
    AdverseEffectReportResponseDTO createDoctorReport(CreateDoctorReportRequestDTO dto, String username);

    // US-02: Pacijent kreira nalog
    AdverseEffectReportResponseDTO createPatientReport(CreatePatientReportRequestDTO dto, String username);

    // US-03: Lekar vidi svoje naloge
    List<AdverseEffectReportResponseDTO> getMyReports(String username);

    // US-03: Editovanje naloga (samo dok je SUBMITTED)
    AdverseEffectReportResponseDTO updateDoctorReport(Long id, UpdateDoctorReportRequestDTO dto, String username);

    // US-04: Farmakovigilant vidi sve naloge
    List<AdverseEffectReportResponseDTO> getAllReports();

    List<AdverseEffectReportResponseDTO> getAllReportsFiltered(String status, String medicationName, String severity);

    // US-03: Detalji jednog naloga
    AdverseEffectReportResponseDTO getReportById(Long id);

    AdverseEffectReportResponseDTO changeStatus(Long reportId, ChangeStatusRequestDTO dto, String currentUsername);

    AnalystNoteResponseDTO addNote(Long reportId, AddNoteRequestDTO dto, String currentUsername);

    List<StatusTransitionResponseDTO> getStatusHistory(Long reportId);

    List<AnalystNoteResponseDTO> getNotes(Long reportId);
}
