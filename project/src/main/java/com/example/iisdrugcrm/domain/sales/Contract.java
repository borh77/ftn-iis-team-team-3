package com.example.iisdrugcrm.domain.sales;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String contractNumber;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false, unique = true)
    private Offer offer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_process_id", nullable = false)
    private SalesProcess salesProcess;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContractStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Column(length = 2000)
    private String terms;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime signedAt;

    protected Contract() {
    }

    public Contract(String contractNumber, Offer offer, LocalDate startDate, LocalDate endDate, String terms) {
        this.contractNumber = contractNumber;
        this.offer = offer;
        this.customer = offer.getCustomer();
        this.salesProcess = offer.getSalesProcess();
        this.status = ContractStatus.PENDING;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalValue = offer.getTotalAmount();
        this.terms = terms;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void update(LocalDate startDate, LocalDate endDate, String terms) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.terms = terms;
    }

    public void markAsSigned() {
        this.status = ContractStatus.SIGNED;
        this.signedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getContractNumber() { return contractNumber; }
    public Offer getOffer() { return offer; }
    public Customer getCustomer() { return customer; }
    public SalesProcess getSalesProcess() { return salesProcess; }
    public ContractStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getTotalValue() { return totalValue; }
    public String getTerms() { return terms; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSignedAt() { return signedAt; }
}