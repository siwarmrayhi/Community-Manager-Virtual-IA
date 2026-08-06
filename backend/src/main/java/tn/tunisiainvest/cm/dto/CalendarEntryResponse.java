package tn.tunisiainvest.cm.dto;

import java.time.LocalDate;
import java.util.List;
import tn.tunisiainvest.cm.model.ContentType;

public record CalendarEntryResponse(
        LocalDate date,
        boolean postingDay,
        ContentType suggestedContentType,
        List<PostResponse> posts
) {
}
