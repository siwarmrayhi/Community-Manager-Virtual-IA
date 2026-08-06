package tn.tunisiainvest.cm.dto;

import java.util.Map;
import tn.tunisiainvest.cm.model.ContentType;
import tn.tunisiainvest.cm.model.Network;

public record StatisticsResponse(
        long postsGenerated,
        long postsValidated,
        double validationRate,
        ContentType mostUsedContentType,
        Network mostUsedNetwork,
        Map<String, Long> postsByStatus
) {
}
