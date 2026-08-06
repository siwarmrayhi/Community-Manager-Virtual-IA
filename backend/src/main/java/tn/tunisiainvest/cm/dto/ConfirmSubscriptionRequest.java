package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmSubscriptionRequest(
        @NotBlank String userEmail,
        @NotBlank String paypalSubscriptionId
) {
}
