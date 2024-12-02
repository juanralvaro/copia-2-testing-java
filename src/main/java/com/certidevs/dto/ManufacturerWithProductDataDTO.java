package com.certidevs.dto;

public record ManufacturerWithProductDataDTO(
        Long manufacturerId,
        String manufacturerName,
        Long productsCount,
        Double productsSumTotalPrice) {

}
