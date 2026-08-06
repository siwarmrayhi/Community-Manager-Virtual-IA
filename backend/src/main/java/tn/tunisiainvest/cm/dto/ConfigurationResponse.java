package tn.tunisiainvest.cm.dto;

import java.time.Instant;
import java.util.Set;
import tn.tunisiainvest.cm.model.Frequency;
import tn.tunisiainvest.cm.model.Language;
import tn.tunisiainvest.cm.model.Network;
import tn.tunisiainvest.cm.model.Tone;

public record ConfigurationResponse(
        Long id,
        Long subscriptionId,
        Set<Network> targetNetworks,
        Tone tone,
        Language language,
        Frequency frequency,
        String sector,
        Set<String> keywords,
        String profileDescription,
        Instant createdAt,
        Instant updatedAt
) {
}
