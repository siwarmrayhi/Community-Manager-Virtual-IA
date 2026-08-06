package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Backs the calendar's "Generer le contenu du mois" button (CDC section 4):
 * fills the editorial calendar for the next 30 days according to the
 * partner's configured frequency, network mix and content-type rotation.
 */
public record GenerateMonthlyContentRequest(
        @NotBlank String userEmail
) {
}
