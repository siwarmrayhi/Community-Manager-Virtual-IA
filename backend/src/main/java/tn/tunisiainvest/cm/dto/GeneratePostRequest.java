package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;
import tn.tunisiainvest.cm.model.ContentType;
import tn.tunisiainvest.cm.model.Network;

public record GeneratePostRequest(
        @NotBlank String userEmail,
        Set<Network> networks,
        ContentType contentType,
        String topic,
        String additionalInstructions,
        LocalDate plannedDate
) {
}
