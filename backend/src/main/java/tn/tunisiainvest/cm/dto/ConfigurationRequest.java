package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import tn.tunisiainvest.cm.model.Frequency;
import tn.tunisiainvest.cm.model.Language;
import tn.tunisiainvest.cm.model.Network;
import tn.tunisiainvest.cm.model.Tone;

public record ConfigurationRequest(
        @NotBlank String userEmail,
        @NotEmpty Set<Network> targetNetworks,
        Tone tone,
        Language language,
        Frequency frequency,
        @NotBlank String sector,
        Set<String> keywords,
        String profileDescription
) {
}
