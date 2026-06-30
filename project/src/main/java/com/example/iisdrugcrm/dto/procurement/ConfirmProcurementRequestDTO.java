package com.example.iisdrugcrm.dto.procurement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class ConfirmProcurementRequestDTO {

    @Size(max = 255, message = "Source file name must be at most 255 characters.")
    private String sourceFileName;

    @Valid
    @NotEmpty(message = "Procurement cannot be confirmed because it contains invalid items.")
    private List<ConfirmProcurementItemDTO> items = new ArrayList<>();

    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }
    public List<ConfirmProcurementItemDTO> getItems() { return items; }
    public void setItems(List<ConfirmProcurementItemDTO> items) { this.items = items; }
}
