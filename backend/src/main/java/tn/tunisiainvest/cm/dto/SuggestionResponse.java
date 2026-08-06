package tn.tunisiainvest.cm.dto;

import java.time.Instant;
import java.time.LocalDate;
import tn.tunisiainvest.cm.model.ContentType;
import tn.tunisiainvest.cm.model.SuggestionStatus;

public record SuggestionResponse(
        Long id,
        ContentType contentType,
        String title,
        String description,
        String suggestedHashtags,
        String sourceReference,
        LocalDate suggestedDate,
        SuggestionStatus status,
        Instant createdAt
) {
}
