package com.example.iisdrugcrm.dto.sales.analytics;

import java.math.BigDecimal;
import java.util.Map;

public class SalesAnalyticsSummaryDTO {

    private long totalLeads;
    private long qualifiedLeads;
    private long convertedLeads;
    private long totalCustomers;

    private long totalProcesses;
    private long activeProcesses;
    private long wonProcesses;
    private long lostProcesses;

    private long totalOffers;
    private long acceptedOffers;
    private BigDecimal totalOfferValue;

    private long totalContracts;
    private long signedContracts;
    private BigDecimal totalContractValue;

    private Map<String, Long> processesByStage;
    private Map<String, Long> offersByStatus;
    private Map<String, Long> contractsByStatus;

    public SalesAnalyticsSummaryDTO(
            long totalLeads,
            long qualifiedLeads,
            long convertedLeads,
            long totalCustomers,
            long totalProcesses,
            long activeProcesses,
            long wonProcesses,
            long lostProcesses,
            long totalOffers,
            long acceptedOffers,
            BigDecimal totalOfferValue,
            long totalContracts,
            long signedContracts,
            BigDecimal totalContractValue,
            Map<String, Long> processesByStage,
            Map<String, Long> offersByStatus,
            Map<String, Long> contractsByStatus
    ) {
        this.totalLeads = totalLeads;
        this.qualifiedLeads = qualifiedLeads;
        this.convertedLeads = convertedLeads;
        this.totalCustomers = totalCustomers;
        this.totalProcesses = totalProcesses;
        this.activeProcesses = activeProcesses;
        this.wonProcesses = wonProcesses;
        this.lostProcesses = lostProcesses;
        this.totalOffers = totalOffers;
        this.acceptedOffers = acceptedOffers;
        this.totalOfferValue = totalOfferValue;
        this.totalContracts = totalContracts;
        this.signedContracts = signedContracts;
        this.totalContractValue = totalContractValue;
        this.processesByStage = processesByStage;
        this.offersByStatus = offersByStatus;
        this.contractsByStatus = contractsByStatus;
    }

    public long getTotalLeads() { return totalLeads; }
    public long getQualifiedLeads() { return qualifiedLeads; }
    public long getConvertedLeads() { return convertedLeads; }
    public long getTotalCustomers() { return totalCustomers; }

    public long getTotalProcesses() { return totalProcesses; }
    public long getActiveProcesses() { return activeProcesses; }
    public long getWonProcesses() { return wonProcesses; }
    public long getLostProcesses() { return lostProcesses; }

    public long getTotalOffers() { return totalOffers; }
    public long getAcceptedOffers() { return acceptedOffers; }
    public BigDecimal getTotalOfferValue() { return totalOfferValue; }

    public long getTotalContracts() { return totalContracts; }
    public long getSignedContracts() { return signedContracts; }
    public BigDecimal getTotalContractValue() { return totalContractValue; }

    public Map<String, Long> getProcessesByStage() { return processesByStage; }
    public Map<String, Long> getOffersByStatus() { return offersByStatus; }
    public Map<String, Long> getContractsByStatus() { return contractsByStatus; }
}