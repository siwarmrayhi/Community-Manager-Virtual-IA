package tn.tunisiainvest.cm.dto;

import java.time.Instant;
import java.time.LocalDate;
import tn.tunisiainvest.cm.model.ContentType;
import tn.tunisiainvest.cm.model.Network;
import tn.tunisiainvest.cm.model.PostStatus;

public record PostResponse(
        Long id,
        Network network,
        ContentType contentType,
        PostStatus status,
        String text,
        String imageDescription,
        String hashtags,
        String callToAction,
        LocalDate plannedDate,
        boolean generatedByFallback,
        String generationNotice,
        Instant createdAt,
        Instant updatedAt,
        Instant validatedAt,
        Instant rejectedAt
) {
}
