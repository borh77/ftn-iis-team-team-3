package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.exception.PricelistStartDateInPastException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

final class PricelistDateRules {

    private static final String START_DATE_IN_PAST_MESSAGE = "Pricelist start date cannot be in the past.";

    private PricelistDateRules() {
    }

    static void validateStartDateNotPast(OffsetDateTime periodStart) {
        if (isStartDateInPast(periodStart)) {
            throw new PricelistStartDateInPastException(START_DATE_IN_PAST_MESSAGE);
        }
    }

    static boolean isStartDateInPast(OffsetDateTime periodStart) {
        if (periodStart == null) {
            return false;
        }
        ZoneId businessZone = ZoneId.systemDefault();
        LocalDate startDate = periodStart.atZoneSameInstant(businessZone).toLocalDate();
        LocalDate today = LocalDate.now(businessZone);
        return startDate.isBefore(today);
    }
}
