package com.example.iisdrugcrm.service.adverse;

import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AddNoteRequestDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectAnalyticsSummaryDTO;
import com.example.iisdrugcrm.dto.adverse.AnalystNoteResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportVersionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.ChangeStatusRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreateDoctorReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreatePatientReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.StatusTransitionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.UpdateDoctorReportRequestDTO;

import java.util.List;
import java.time.LocalDate;

public interface AdverseEffectReportService {

    // US-01: Doctor creates a report
    AdverseEffectReportResponseDTO createDoctorReport(CreateDoctorReportRequestDTO dto, String username);

    // US-02: Patient creates a report
    AdverseEffectReportResponseDTO createPatientReport(CreatePatientReportRequestDTO dto, String username);

    // US-03: Doctor sees their reports
    List<AdverseEffectReportResponseDTO> getMyReports(String username);

    // US-03: Report editing only while SUBMITTED
    AdverseEffectReportResponseDTO updateDoctorReport(Long id, UpdateDoctorReportRequestDTO dto, String username);

    // US-04: Pharmacovigilance user sees all reports
    List<AdverseEffectReportResponseDTO> getAllReports();

    List<AdverseEffectReportResponseDTO> getAllReportsFiltered(String status, String medicationName, String severity);

    // US-03: Single report details
    AdverseEffectReportResponseDTO getReportById(Long id);

    AdverseEffectReportResponseDTO changeStatus(Long reportId, ChangeStatusRequestDTO dto, String currentUsername);

    AnalystNoteResponseDTO addNote(Long reportId, AddNoteRequestDTO dto, String currentUsername);

    List<StatusTransitionResponseDTO> getStatusHistory(Long reportId);

    List<AnalystNoteResponseDTO> getNotes(Long reportId);

    List<AdverseEffectReportVersionResponseDTO> getReportVersions(Long reportId, String currentUsername);

    AdverseEffectAnalyticsSummaryDTO getAnalyticsSummary(LocalDate from, LocalDate to);

    byte[] generateAnalyticsPdfReport(LocalDate from, LocalDate to, String analystInterpretation);
}
