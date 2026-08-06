package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SubscriptionRequest(
        @NotBlank @Email String userEmail
) {
}
