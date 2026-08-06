package tn.tunisiainvest.cm.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.tunisiainvest.cm.config.PayPalProperties;

/**
 * Thin client around the PayPal REST "Subscriptions" API
 * (https://developer.paypal.com/docs/api/subscriptions/v1/), used to bill
 * the CM Virtuel IA subscription every month.
 *
 * <p>When no PayPal sandbox credentials are configured (local dev / demo
 * without a PayPal app), this service degrades gracefully to a simulated
 * sandbox flow so the rest of the subscription workflow (activation,
 * welcome posts, cancellation) can still be exercised end to end - the same
 * pattern already used by {@link GroqContentService} for the Groq fallback.</p>
 */
@Service
public class PayPalService {

    private static final String SIMULATED_NOTICE =
            "PayPal sandbox non configure : abonnement simule localement.";

    private final WebClient paypalWebClient;
    private final PayPalProperties properties;

    public PayPalService(WebClient paypalWebClient, PayPalProperties properties) {
        this.paypalWebClient = paypalWebClient;
        this.properties = properties;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(properties.getClientId())
                && StringUtils.hasText(properties.getClientSecret())
                && StringUtils.hasText(properties.getPlanId());
    }

    /**
     * Creates a PayPal subscription in APPROVAL_PENDING state and returns the
     * approval link the partner must open to pay in the PayPal Sandbox.
     */
    public PayPalSubscriptionResult createSubscription(String userEmail) {
        if (!isConfigured()) {
            String simulatedId = "SANDBOX-SIM-" + UUID.randomUUID();
            return new PayPalSubscriptionResult(
                    simulatedId,
                    "https://www.sandbox.paypal.com/checkoutnow?subscription=" + simulatedId,
                    "APPROVAL_PENDING",
                    SIMULATED_NOTICE
            );
        }

        String accessToken = fetchAccessToken();
        Map<String, Object> body = Map.of(
                "plan_id", properties.getPlanId(),
                "subscriber", Map.of("email_address", userEmail),
                "application_context", Map.of(
                        "brand_name", "Tunisia Invest - Community Manager Virtuel IA",
                        "user_action", "SUBSCRIBE_NOW",
                        "return_url", properties.getReturnUrl(),
                        "cancel_url", properties.getCancelUrl()
                )
        );

        PayPalSubscriptionCreated created = paypalWebClient.post()
                .uri("/v1/billing/subscriptions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("PayPal-Request-Id", UUID.randomUUID().toString())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PayPalSubscriptionCreated.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        if (created == null) {
            throw new IllegalStateException("Reponse PayPal vide lors de la creation de l'abonnement.");
        }

        String approveLink = created.links() == null ? null : created.links().stream()
                .filter(link -> "approve".equalsIgnoreCase(link.rel()))
                .map(PayPalLink::href)
                .findFirst()
                .orElse(null);

        return new PayPalSubscriptionResult(created.id(), approveLink, created.status(), null);
    }

    /**
     * Confirms with PayPal that a subscription reached the ACTIVE status
     * after the buyer approved it on the sandbox checkout page.
     */
    public PayPalSubscriptionResult fetchSubscription(String paypalSubscriptionId) {
        if (!isConfigured() || paypalSubscriptionId == null || paypalSubscriptionId.startsWith("SANDBOX-SIM-")) {
            return new PayPalSubscriptionResult(paypalSubscriptionId, null, "ACTIVE", SIMULATED_NOTICE);
        }

        String accessToken = fetchAccessToken();
        PayPalSubscriptionCreated subscription = paypalWebClient.get()
                .uri("/v1/billing/subscriptions/{id}", paypalSubscriptionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PayPalSubscriptionCreated.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        if (subscription == null) {
            throw new IllegalStateException("Impossible de recuperer l'abonnement PayPal " + paypalSubscriptionId);
        }
        return new PayPalSubscriptionResult(subscription.id(), null, subscription.status(), null);
    }

    /**
     * Cancels the recurring billing on the PayPal side so no further monthly
     * charge occurs once the partner unsubscribes.
     */
    public void cancelSubscription(String paypalSubscriptionId, String reason) {
        if (!isConfigured() || paypalSubscriptionId == null || paypalSubscriptionId.startsWith("SANDBOX-SIM-")) {
            return;
        }

        String accessToken = fetchAccessToken();
        paypalWebClient.post()
                .uri("/v1/billing/subscriptions/{id}/cancel", paypalSubscriptionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(Map.of("reason", StringUtils.hasText(reason) ? reason : "Desabonnement demande par le client"))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(error -> Mono.empty())
                .block();
    }

    private String fetchAccessToken() {
        String credentials = properties.getClientId() + ":" + properties.getClientSecret();
        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");

        PayPalTokenResponse token = paypalWebClient.post()
                .uri("/v1/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(PayPalTokenResponse.class)
                .timeout(Duration.ofSeconds(15))
                .block();

        if (token == null || !StringUtils.hasText(token.accessToken())) {
            throw new IllegalStateException("Authentification PayPal impossible (client id/secret invalides).");
        }
        return token.accessToken();
    }

    public record PayPalSubscriptionResult(String paypalSubscriptionId, String approvalUrl, String status, String notice) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PayPalTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PayPalSubscriptionCreated(String id, String status, List<PayPalLink> links) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PayPalLink(String rel, String href) {
    }
}
