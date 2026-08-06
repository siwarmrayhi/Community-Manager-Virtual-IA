package tn.tunisiainvest.cm.dto;

import java.math.BigDecimal;
import java.time.Instant;
import tn.tunisiainvest.cm.model.SubscriptionStatus;

public record SubscriptionResponse(
        Long id,
        String userEmail,
        SubscriptionStatus status,
        BigDecimal monthlyPrice,
        String currency,
        String paypalSubscriptionId,
        String paypalApprovalUrl,
        Instant startedAt,
        Instant cancelledAt
) {
}
