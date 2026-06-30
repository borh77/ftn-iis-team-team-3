package com.example.iisdrugcrm.domain.pricelist;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import com.example.iisdrugcrm.exception.InvalidPricelistThresholdException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "pricelist_items")
public class PricelistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricelist_id", nullable = false)
    private Pricelist pricelist;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "variant_name", nullable = false, length = 255)
    private String variantName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pricelist_item_thresholds", joinColumns = @JoinColumn(name = "pricelist_item_id"))
    @OrderBy("quantityFrom ASC")
    private List<QuantityThreshold> thresholds = new ArrayList<>();

    public PricelistItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pricelist getPricelist() {
        return pricelist;
    }

    public void setPricelist(Pricelist pricelist) {
        this.pricelist = pricelist;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public List<QuantityThreshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<QuantityThreshold> thresholds) {
        this.thresholds = thresholds;
    }

    public void validateThresholds() {
        if (thresholds == null || thresholds.isEmpty()) {
            throw invalid("Thresholds for variant [" + variantName + "] cannot be empty.");
        }

        List<QuantityThreshold> sortedThresholds = new ArrayList<>(thresholds);
        String prefix = "Invalid thresholds for variant [" + variantName + "]: ";
        for (QuantityThreshold threshold : sortedThresholds) {
            if (threshold == null) {
                throw invalid(prefix + "threshold cannot be empty.");
            }
            if (threshold.getQuantityFrom() == null || threshold.getQuantityFrom() <= 0) {
                throw invalid(prefix + "quantityFrom must be greater than 0.");
            }
            if (threshold.getPrice() == null || threshold.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw invalid(prefix + "price must be greater than 0.");
            }
        }
        sortedThresholds.sort(Comparator.comparingInt(QuantityThreshold::getQuantityFrom));

        QuantityThreshold previous = null;
        for (int index = 0; index < sortedThresholds.size(); index++) {
            QuantityThreshold current = sortedThresholds.get(index);
            if (current.getQuantityTo() != null && current.getQuantityTo() <= current.getQuantityFrom()) {
                throw invalid(prefix + "quantityTo must be greater than quantityFrom.");
            }
            if (previous != null) {
                if (previous.getQuantityTo() == null) {
                    throw invalid(prefix + "no threshold can follow an open-ended threshold.");
                }
                int expectedFrom = previous.getQuantityTo() + 1;
                if (current.getQuantityFrom() < expectedFrom) {
                    throw invalid(prefix + "thresholds cannot overlap.");
                }
                if (current.getQuantityFrom() > expectedFrom) {
                    throw invalid(prefix + "thresholds must be continuous without gaps.");
                }
                if (current.getPrice().compareTo(previous.getPrice()) > 0) {
                    throw invalid(prefix + "price for a higher quantity threshold must be equal to or lower than the previous one.");
                }
            }
            if (current.getQuantityTo() == null && index < sortedThresholds.size() - 1) {
                throw invalid(prefix + "open-ended threshold must be last.");
            }
            previous = current;
        }
    }

    private InvalidPricelistThresholdException invalid(String message) {
        return new InvalidPricelistThresholdException(message);
    }
}
