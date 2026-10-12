package com.plotvista.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CompanyRequest {
    private String name;
    private String email;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal squareFeet;
    private String layoutType;
    private LocalDate contractStart;
    private LocalDate contractEnd;
    private String status;
    private List<ContactDto> contacts;

    @Data
    public static class ContactDto {
        private String contactName;
        private String contactMobile;
        private boolean isPrimary;
    }
}
