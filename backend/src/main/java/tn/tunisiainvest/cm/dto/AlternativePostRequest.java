package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.NotBlank;

public record AlternativePostRequest(
        @NotBlank String userEmail,
        String additionalInstructions
) {
}
