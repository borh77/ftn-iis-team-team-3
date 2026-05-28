package com.example.iisdrugcrm.domain.sales;

import java.time.LocalDateTime;
import jakarta.persistence.*;


@Entity
@Table(name = "customer_communications")
public class CustomerCommunication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationType type;

    @Column(nullable = false)
    private LocalDateTime communicationDate;

    @Column(nullable = false, length = 1000)
    private String summary;

    protected CustomerCommunication() {}

    public CustomerCommunication(Customer customer, CommunicationType type,
                                 LocalDateTime communicationDate, String summary) {
        this.customer = customer;
        this.type = type;
        this.communicationDate = communicationDate;
        this.summary = summary;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public CommunicationType getType() { return type; }
    public LocalDateTime getCommunicationDate() { return communicationDate; }
    public String getSummary() { return summary; }
}