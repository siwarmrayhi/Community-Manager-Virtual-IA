package tn.tunisiainvest.cm.dto;

import java.util.Set;
import tn.tunisiainvest.cm.model.ContentType;
import tn.tunisiainvest.cm.model.Frequency;
import tn.tunisiainvest.cm.model.Language;
import tn.tunisiainvest.cm.model.Network;
import tn.tunisiainvest.cm.model.Tone;

public record GenerationContext(
        Network network,
        ContentType contentType,
        Tone tone,
        Language language,
        Frequency frequency,
        String sector,
        Set<String> keywords,
        String profileDescription,
        String topic,
        String additionalInstructions
) {
}
