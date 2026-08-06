package tn.tunisiainvest.cm.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/**
 * Inbound webhook payload used by another module of the platform (binome 3 -
 * "annonces", or the events module) to notify the Community Manager Virtuel
 * that a new listing/opportunity or platform event was just published, so
 * the CM can automatically suggest a promotional post (CDC section 6).
 *
 * <p>Called as: {@code POST /api/cm/suggestions/annonce} from the listings
 * service's backend once a listing is published (server-to-server call, not
 * a browser call - no end-user auth is attached to it, only a shared
 * reference id).</p>
 */
public record NewListingEvent(
        @NotBlank String listingReference,
        @NotBlank String title,
        String sector,
        String description,
        Set<String> keywords,
        /**
         * Optional: restrict the suggestion to a single partner's
         * configuration (matched by subscription email). When absent, every
         * active subscription whose configured sector matches (or has no
         * sector restriction) receives the suggestion.
         */
        String targetUserEmail
) {
}
