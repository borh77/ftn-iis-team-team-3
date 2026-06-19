package com.example.iisdrugcrm.domain.pricelist;

import com.example.iisdrugcrm.exception.InvalidPricelistThresholdException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricelistItemTest {

    @Test
    void validContinuousThresholdsPass() {
        PricelistItem item = itemWithThresholds(
                threshold(1, 10, "100.00"),
                threshold(11, 50, "95.00"),
                threshold(51, null, "90.00")
        );

        assertDoesNotThrow(item::validateThresholds);
    }

    @Test
    void gapBetweenThresholdsFails() {
        PricelistItem item = itemWithThresholds(
                threshold(1, 10, "100.00"),
                threshold(12, 50, "95.00")
        );

        assertThrows(InvalidPricelistThresholdException.class, item::validateThresholds);
    }

    @Test
    void overlappingThresholdsFail() {
        PricelistItem item = itemWithThresholds(
                threshold(1, 10, "100.00"),
                threshold(5, 20, "95.00")
        );

        assertThrows(InvalidPricelistThresholdException.class, item::validateThresholds);
    }

    @Test
    void priceIncreaseInHigherThresholdFails() {
        PricelistItem item = itemWithThresholds(
                threshold(1, 10, "100.00"),
                threshold(11, 50, "110.00")
        );

        assertThrows(InvalidPricelistThresholdException.class, item::validateThresholds);
    }

    @Test
    void openEndedThresholdNotLastFails() {
        PricelistItem item = itemWithThresholds(
                threshold(1, null, "100.00"),
                threshold(11, 50, "95.00")
        );

        assertThrows(InvalidPricelistThresholdException.class, item::validateThresholds);
    }

    @Test
    void emptyThresholdsFail() {
        PricelistItem item = itemWithThresholds();

        assertThrows(InvalidPricelistThresholdException.class, item::validateThresholds);
    }

    @Test
    void nullQuantityFromFailsWithBusinessException() {
        PricelistItem item = itemWithThresholds(
                threshold(null, 10, "100.00")
        );

        assertThrows(InvalidPricelistThresholdException.class, item::validateThresholds);
    }

    @Test
    void nullPriceFailsWithBusinessException() {
        PricelistItem item = itemWithThresholds(
                threshold(1, 10, null)
        );

        assertThrows(InvalidPricelistThresholdException.class, item::validateThresholds);
    }

    private PricelistItem itemWithThresholds(QuantityThreshold... thresholds) {
        PricelistItem item = new PricelistItem();
        item.setVariantName("Variant A");
        item.setThresholds(List.of(thresholds));
        return item;
    }

    private QuantityThreshold threshold(Integer quantityFrom, Integer quantityTo, String price) {
        QuantityThreshold threshold = new QuantityThreshold();
        threshold.setQuantityFrom(quantityFrom);
        threshold.setQuantityTo(quantityTo);
        threshold.setPrice(price == null ? null : new BigDecimal(price));
        return threshold;
    }
}
