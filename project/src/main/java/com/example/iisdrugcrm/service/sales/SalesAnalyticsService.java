package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.*;
import com.example.iisdrugcrm.dto.sales.analytics.SalesAnalyticsSummaryDTO;
import com.example.iisdrugcrm.repository.sales.*;
import com.example.iisdrugcrm.dto.sales.analytics.SalesStagnationAlertDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Font;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesAnalyticsService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final SalesProcessRepository salesProcessRepository;
    private final OfferRepository offerRepository;
    private final ContractRepository contractRepository;
    private final JdbcTemplate jdbcTemplate;

    public SalesAnalyticsService(
            LeadRepository leadRepository,
            CustomerRepository customerRepository,
            SalesProcessRepository salesProcessRepository,
            OfferRepository offerRepository,
            ContractRepository contractRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
        this.salesProcessRepository = salesProcessRepository;
        this.offerRepository = offerRepository;
        this.contractRepository = contractRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public SalesAnalyticsSummaryDTO getSummary() {
        List<Lead> leads = leadRepository.findAll();
        List<SalesProcess> processes = salesProcessRepository.findAll();
        List<Offer> offers = offerRepository.findAll();
        List<Contract> contracts = contractRepository.findAll();

        long totalLeads = leads.size();
        long qualifiedLeads = leads.stream().filter(lead -> lead.getStatus() == LeadStatus.QUALIFIED).count();
        long convertedLeads = leads.stream().filter(lead -> lead.getStatus() == LeadStatus.CONVERTED).count();

        long totalProcesses = processes.size();
        long activeProcesses = processes.stream().filter(process -> process.getStatus() == SalesProcessStatus.ACTIVE).count();
        long wonProcesses = processes.stream().filter(process -> process.getOutcome() == SalesProcessOutcome.CLOSED_WON).count();
        long lostProcesses = processes.stream().filter(process -> process.getOutcome() == SalesProcessOutcome.CLOSED_LOST).count();

        long totalOffers = offers.size();
        long acceptedOffers = offers.stream().filter(offer -> offer.getStatus() == OfferStatus.ACCEPTED).count();

        BigDecimal totalOfferValue = offers.stream()
                .map(Offer::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalContracts = contracts.size();
        long signedContracts = contracts.stream().filter(contract -> contract.getStatus() == ContractStatus.SIGNED).count();

        BigDecimal totalContractValue = contracts.stream()
                .map(Contract::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> processesByStage = processes.stream()
                .collect(Collectors.groupingBy(SalesProcess::getStage, Collectors.counting()));

        Map<String, Long> offersByStatus = offers.stream()
                .collect(Collectors.groupingBy(offer -> offer.getStatus().name(), Collectors.counting()));

        Map<String, Long> contractsByStatus = contracts.stream()
                .collect(Collectors.groupingBy(contract -> contract.getStatus().name(), Collectors.counting()));

        return new SalesAnalyticsSummaryDTO(
                totalLeads,
                qualifiedLeads,
                convertedLeads,
                customerRepository.count(),
                totalProcesses,
                activeProcesses,
                wonProcesses,
                lostProcesses,
                totalOffers,
                acceptedOffers,
                totalOfferValue,
                totalContracts,
                signedContracts,
                totalContractValue,
                processesByStage,
                offersByStatus,
                contractsByStatus
        );
    }

    @Transactional
    public void runStagnationCheck() {
        jdbcTemplate.execute("CALL pr_run_sales_stagnation_check()");
    }

    @Transactional(readOnly = true)
    public List<SalesStagnationAlertDTO> getOpenStagnationAlerts() {
        String sql = """
                SELECT
                    alert.id,
                    alert.sales_process_id,
                    process.title AS process_title,
                    alert.stage_name,
                    alert.severity,
                    alert.days_in_stage,
                    alert.message,
                    alert.status,
                    alert.created_at
                FROM sales_stagnation_alerts alert
                JOIN sales_processes process
                    ON process.id = alert.sales_process_id
                WHERE alert.status = 'OPEN'
                ORDER BY
                    CASE alert.severity
                        WHEN 'CRITICAL' THEN 1
                        WHEN 'WARNING' THEN 2
                        ELSE 3
                    END,
                    alert.days_in_stage DESC,
                    alert.created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new SalesStagnationAlertDTO(
                        resultSet.getLong("id"),
                        resultSet.getLong("sales_process_id"),
                        resultSet.getString("process_title"),
                        resultSet.getString("stage_name"),
                        resultSet.getString("severity"),
                        resultSet.getInt("days_in_stage"),
                        resultSet.getString("message"),
                        resultSet.getString("status"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                )
        );
    }

    public byte[] generatePdfReport() {
        SalesAnalyticsSummaryDTO summary = getSummary();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 13, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11);

            document.add(new Paragraph("Drug CRM", titleFont));
            document.add(new Paragraph("Sales Analytics Report", titleFont));
            document.add(new Paragraph("Generated at: " + LocalDateTime.now(), normalFont));
            document.add(new Paragraph(" "));

            addSection(document, "LEADS", sectionFont);
            addLine(document, "Total Leads", summary.getTotalLeads(), normalFont);
            addLine(document, "Qualified Leads", summary.getQualifiedLeads(), normalFont);
            addLine(document, "Converted Leads", summary.getConvertedLeads(), normalFont);

            addSection(document, "CUSTOMERS", sectionFont);
            addLine(document, "Total Customers", summary.getTotalCustomers(), normalFont);

            addSection(document, "SALES PROCESS", sectionFont);
            addLine(document, "Total Processes", summary.getTotalProcesses(), normalFont);
            addLine(document, "Active Processes", summary.getActiveProcesses(), normalFont);
            addLine(document, "Won Processes", summary.getWonProcesses(), normalFont);
            addLine(document, "Lost Processes", summary.getLostProcesses(), normalFont);

            addSection(document, "OFFERS", sectionFont);
            addLine(document, "Total Offers", summary.getTotalOffers(), normalFont);
            addLine(document, "Accepted Offers", summary.getAcceptedOffers(), normalFont);
            addLine(document, "Total Offer Value", summary.getTotalOfferValue(), normalFont);

            addSection(document, "CONTRACTS", sectionFont);
            addLine(document, "Total Contracts", summary.getTotalContracts(), normalFont);
            addLine(document, "Signed Contracts", summary.getSignedContracts(), normalFont);
            addLine(document, "Total Contract Value", summary.getTotalContractValue(), normalFont);

            addMapSection(document, "PROCESSES BY STAGE", summary.getProcessesByStage(), sectionFont, normalFont);
            addMapSection(document, "OFFERS BY STATUS", summary.getOffersByStatus(), sectionFont, normalFont);
            addMapSection(document, "CONTRACTS BY STATUS", summary.getContractsByStatus(), sectionFont, normalFont);

            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate sales analytics PDF report.", e);
        }
    }

    private void addSection(Document document, String title, Font font) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(title, font));
    }

    private void addLine(Document document, String label, Object value, Font font) throws DocumentException {
        document.add(new Paragraph(label + ": " + value, font));
    }

    private void addMapSection(
            Document document,
            String title,
            Map<String, Long> values,
            Font sectionFont,
            Font normalFont
    ) throws DocumentException {
        addSection(document, title, sectionFont);

        values.forEach((key, value) -> {
            try {
                document.add(new Paragraph(key + ": " + value, normalFont));
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        });
    }
}