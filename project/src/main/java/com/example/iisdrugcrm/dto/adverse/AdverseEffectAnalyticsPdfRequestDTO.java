package com.example.iisdrugcrm.dto.adverse;

import java.time.LocalDate;

public class AdverseEffectAnalyticsPdfRequestDTO {
    private LocalDate from;
    private LocalDate to;
    private String analystInterpretation;

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public String getAnalystInterpretation() {
        return analystInterpretation;
    }

    public void setAnalystInterpretation(String analystInterpretation) {
        this.analystInterpretation = analystInterpretation;
    }
}
