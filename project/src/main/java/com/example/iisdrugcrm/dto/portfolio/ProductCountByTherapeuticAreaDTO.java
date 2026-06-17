package com.example.iisdrugcrm.dto.portfolio;

public class ProductCountByTherapeuticAreaDTO {

    private Long therapeuticAreaId;
    private String therapeuticAreaName;
    private long productCount;

    public ProductCountByTherapeuticAreaDTO(
            Long therapeuticAreaId,
            String therapeuticAreaName,
            long productCount
    ) {
        this.therapeuticAreaId = therapeuticAreaId;
        this.therapeuticAreaName = therapeuticAreaName;
        this.productCount = productCount;
    }

    public Long getTherapeuticAreaId() {
        return therapeuticAreaId;
    }

    public String getTherapeuticAreaName() {
        return therapeuticAreaName;
    }

    public long getProductCount() {
        return productCount;
    }
}