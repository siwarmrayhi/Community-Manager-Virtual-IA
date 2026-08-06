package tn.tunisiainvest.cm.dto;

import java.math.BigDecimal;
import java.util.List;

public record ServiceDescriptionResponse(
        String name,
        String description,
        BigDecimal monthlyPrice,
        String currency,
        List<String> includedNetworks,
        List<String> validationFlow,
        List<String> contentTypes
) {
}
