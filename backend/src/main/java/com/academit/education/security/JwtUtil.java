package com.academit.education.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtUtil {

    private final String secretKey;
    private final long expirationMillis;

    public JwtUtil(
            @Value("${jwt.secret:academit-super-secret-key-for-jwt-signing-2026-very-secure}") String secretKey,
            @Value("${jwt.expiration:86400000}") long expirationMillis) {
        this.secretKey = secretKey;
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username, String role, Integer idUsuario) {
        try {
            long now = System.currentTimeMillis();
            long expiry = now + expirationMillis;

            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payloadJson = String.format(
                    "{\"sub\":\"%s\",\"rol\":\"%s\",\"idUsuario\":%d,\"iat\":%d,\"exp\":%d}",
                    escapeJson(username),
                    escapeJson(role),
                    idUsuario != null ? idUsuario : 0,
                    now / 1000,
                    expiry / 1000
            );

            String headerEncoded = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadEncoded = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String dataToSign = headerEncoded + "." + payloadEncoded;

            String signature = sign(dataToSign);

            return dataToSign + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar token JWT", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            if (token == null) return false;
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            String dataToSign = parts[0] + "." + parts[1];
            String expectedSignature = sign(dataToSign);

            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            String payload = getPayload(token);
            Long exp = extractLongClaim(payload, "exp");
            if (exp != null) {
                long expiryMillis = exp * 1000;
                if (new Date().getTime() > expiryMillis) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String payload = getPayload(token);
            return extractStringClaim(payload, "sub");
        } catch (Exception e) {
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            String payload = getPayload(token);
            return extractStringClaim(payload, "rol");
        } catch (Exception e) {
            return null;
        }
    }

    public Integer extractIdUsuario(String token) {
        try {
            String payload = getPayload(token);
            Long val = extractLongClaim(payload, "idUsuario");
            return val != null ? val.intValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getPayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token JWT inválido");
        }
        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private String extractStringClaim(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Long extractLongClaim(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(rawHmac);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

