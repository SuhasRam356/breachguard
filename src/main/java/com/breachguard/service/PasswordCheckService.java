package com.breachguard.service;

import com.breachguard.dto.PasswordResult;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

/**
 * Pwned Passwords check using k-anonymity — exact port of Python password_check.py.
 */
@Service
public class PasswordCheckService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)).build();

    public PasswordResult checkPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            String sha1 = bytesToHex(hash).toUpperCase();
            String prefix = sha1.substring(0, 5);
            String suffix = sha1.substring(5);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.pwnedpasswords.com/range/" + prefix))
                    .header("User-Agent", "BreachGuard-StudentProject/1.0")
                    .header("Add-Padding", "true")
                    .timeout(Duration.ofSeconds(15))
                    .GET().build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return new PasswordResult(-1, false, prefix, "network");
            }

            for (String line : resp.body().split("\n")) {
                String[] parts = line.trim().split(":");
                if (parts.length == 2 && parts[0].equals(suffix)) {
                    int count = Integer.parseInt(parts[1].trim());
                    return new PasswordResult(count, count > 0, prefix, null);
                }
            }
            return new PasswordResult(0, false, prefix, null);

        } catch (Exception e) {
            return new PasswordResult(-1, false, "", "network");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
