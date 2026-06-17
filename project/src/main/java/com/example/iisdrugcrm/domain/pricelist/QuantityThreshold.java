package com.example.iisdrugcrm.domain.pricelist;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class QuantityThreshold {

    @Column(name = "quantity_from", nullable = false)
    private Integer quantityFrom;

    @Column(name = "quantity_to")
    private Integer quantityTo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    public Integer getQuantityFrom() {
        return quantityFrom;
    }

    public void setQuantityFrom(Integer quantityFrom) {
        this.quantityFrom = quantityFrom;
    }

    public Integer getQuantityTo() {
        return quantityTo;
    }

    public void setQuantityTo(Integer quantityTo) {
        this.quantityTo = quantityTo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
