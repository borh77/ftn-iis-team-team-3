package com.example.iisdrugcrm.dto.portfolio;

public class MarketProductCountByRegionDTO {

    private Long regionId;
    private String regionName;
    private String regionCode;
    private long marketProductCount;

    public MarketProductCountByRegionDTO(
            Long regionId,
            String regionName,
            String regionCode,
            long marketProductCount
    ) {
        this.regionId = regionId;
        this.regionName = regionName;
        this.regionCode = regionCode;
        this.marketProductCount = marketProductCount;
    }

    public Long getRegionId() {
        return regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public long getMarketProductCount() {
        return marketProductCount;
    }
}