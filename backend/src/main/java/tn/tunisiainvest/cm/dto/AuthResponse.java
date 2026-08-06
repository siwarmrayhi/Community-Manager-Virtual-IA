package tn.tunisiainvest.cm.dto;

public record AuthResponse(
        String token,
        String userEmail
) {
}
