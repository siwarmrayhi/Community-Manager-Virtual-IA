package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditPostRequest(
        @NotBlank String userEmail,
        @NotBlank @Size(max = 5000) String text
) {
}
