package tn.tunisiainvest.cm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypal")
public class PayPalProperties {

    /**
     * PayPal REST app client id (sandbox or live).
     */
    private String clientId;

    /**
     * PayPal REST app client secret.
     */
    private String clientSecret;

    private String baseUrl = "https://api-m.sandbox.paypal.com";

    /**
     * Billing plan id created once in the PayPal (sandbox) dashboard for the
     * "Community Manager Virtuel IA" monthly subscription (see README for setup).
     */
    private String planId;

    private String returnUrl = "http://localhost:5173/cm/abonnement/succes";

    private String cancelUrl = "http://localhost:5173/cm/abonnement/annule";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
}
