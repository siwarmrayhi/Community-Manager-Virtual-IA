package tn.tunisiainvest.cm.dto;

import java.util.List;
import tn.tunisiainvest.cm.model.SubscriptionStatus;

/**
 * Backing data for the CM main dashboard (CDC-implied "Tableau de bord
 * Community Manager" task): a summary of the activation status, the next
 * planned post, a compact upcoming calendar, quick stats and the list of
 * quick-access actions the frontend should surface as buttons.
 */
public record DashboardResponse(
        SubscriptionStatus subscriptionStatus,
        ConfigurationResponse configuration,
        long pendingValidationCount,
        long newSuggestionsCount,
        PostResponse nextPlannedPost,
        List<CalendarEntryResponse> upcomingCalendar,
        StatisticsResponse statistics,
        List<String> quickActions
) {
}
