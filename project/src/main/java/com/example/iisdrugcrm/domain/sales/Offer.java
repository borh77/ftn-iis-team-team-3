package com.example.iisdrugcrm.domain.sales;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String offerNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_process_id", nullable = false)
    private SalesProcess salesProcess;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OfferStatus status;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferItem> items = new ArrayList<>();

    protected Offer() {
    }

    public Offer(String offerNumber, Customer customer, SalesProcess salesProcess,
                 LocalDate validUntil, String notes) {
        this.offerNumber = offerNumber;
        this.customer = customer;
        this.salesProcess = salesProcess;
        this.validUntil = validUntil;
        this.notes = notes;
        this.status = OfferStatus.DRAFT;
        this.totalAmount = BigDecimal.ZERO;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void addItem(OfferItem item) {
        items.add(item);
        item.assignToOffer(this);
        recalculateTotal();
    }

    public void markAsAccepted() {
        this.status = OfferStatus.ACCEPTED;
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OfferItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public String getOfferNumber() { return offerNumber; }
    public Customer getCustomer() { return customer; }
    public SalesProcess getSalesProcess() { return salesProcess; }
    public OfferStatus getStatus() { return status; }
    public LocalDate getValidUntil() { return validUntil; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OfferItem> getItems() { return items; }
}