package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import tn.tunisiainvest.cm.model.Network;

/**
 * CDC section 7: "Reutiliser un ancien post comme base pour en generer un
 * similaire". The original post (any status: validated, rejected, pending)
 * is kept untouched in the library; a brand new post is generated using it
 * as inspiration.
 */
public record ReusePostRequest(
        @NotBlank String userEmail,
        Network network,
        LocalDate plannedDate,
        String additionalInstructions
) {
}

