package com.example.iisdrugcrm.dto.pricelist;

public class CatalogVariantDTO {
    private Long id;
    private String name;
    private boolean active;
    private Long replacementVariantId;
    private String replacementVariantName;

    public CatalogVariantDTO() {
    }

    public CatalogVariantDTO(Long id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public CatalogVariantDTO(Long id, String name, boolean active, Long replacementVariantId, String replacementVariantName) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.replacementVariantId = replacementVariantId;
        this.replacementVariantName = replacementVariantName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getReplacementVariantId() {
        return replacementVariantId;
    }

    public void setReplacementVariantId(Long replacementVariantId) {
        this.replacementVariantId = replacementVariantId;
    }

    public String getReplacementVariantName() {
        return replacementVariantName;
    }

    public void setReplacementVariantName(String replacementVariantName) {
        this.replacementVariantName = replacementVariantName;
    }
}
