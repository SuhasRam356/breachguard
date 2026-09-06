package com.breachguard.service;

import com.breachguard.dto.BreachInfo;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Remediation hub — exact port of Python remediation.py.
 */
@Service
public class RemediationService {

    // Site keyword -> {reset, twofa (bool), twofa_url}
    private static final Map<String, Map<String, Object>> SITES = new LinkedHashMap<>();

    static {
        SITES.put("linkedin", Map.of("reset","https://www.linkedin.com/uas/request-password-reset","twofa",true,"twofa_url","https://www.linkedin.com/psettings/two-step-verification"));
        SITES.put("adobe", Map.of("reset","https://account.adobe.com/security/password-reset","twofa",true,"twofa_url","https://account.adobe.com/security/2-step-verification"));
        SITES.put("dropbox", Map.of("reset","https://www.dropbox.com/forgot","twofa",true,"twofa_url","https://www.dropbox.com/account/security"));
        SITES.put("canva", Map.of("reset","https://www.canva.com/help/reset-password/","twofa",true,"twofa_url","https://www.canva.com/settings/your-account"));
        SITES.put("facebook", Map.of("reset","https://www.facebook.com/login/identify/","twofa",true,"twofa_url","https://www.facebook.com/security/2fac/settings/"));
        SITES.put("twitter", Map.of("reset","https://twitter.com/account/begin_password_reset","twofa",true,"twofa_url","https://x.com/settings/account/security"));
        SITES.put("instagram", Map.of("reset","https://www.instagram.com/accounts/password/reset/","twofa",true,"twofa_url","https://www.instagram.com/accounts/two_factor_authentication/"));
        SITES.put("google", Map.of("reset","https://accounts.google.com/signin/recovery","twofa",true,"twofa_url","https://myaccount.google.com/signinoptions/two-step-verification"));
        SITES.put("gmail", Map.of("reset","https://accounts.google.com/signin/recovery","twofa",true,"twofa_url","https://myaccount.google.com/signinoptions/two-step-verification"));
        SITES.put("yahoo", Map.of("reset","https://login.yahoo.com/forgot","twofa",true,"twofa_url","https://login.yahoo.com/account/security"));
        SITES.put("microsoft", Map.of("reset","https://account.live.com/password/reset","twofa",true,"twofa_url","https://account.microsoft.com/security/"));
        SITES.put("amazon", Map.of("reset","https://www.amazon.in/ap/forgotpassword","twofa",true,"twofa_url","https://www.amazon.in/a/settings/approval"));
        SITES.put("flipkart", Map.of("reset","https://www.flipkart.com/account/forgotpassword","twofa",true,"twofa_url","https://www.flipkart.com/account/security"));
        SITES.put("netflix", Map.of("reset","https://www.netflix.com/in/password","twofa",true,"twofa_url","https://www.netflix.com/account/security"));
        SITES.put("spotify", Map.of("reset","https://www.spotify.com/password-reset/","twofa",true,"twofa_url","https://www.spotify.com/account/overview/"));
        SITES.put("zomato", Map.of("reset","https://www.zomato.com/forgot-password","twofa",true,"twofa_url","https://www.zomato.com/profile/settings"));
        SITES.put("swiggy", Map.of("reset","https://www.swiggy.com/account/forgot-password","twofa",false,"twofa_url",""));
        SITES.put("airbnb", Map.of("reset","https://www.airbnb.co.in/forgot_password","twofa",true,"twofa_url","https://www.airbnb.co.in/account-settings/login-and-security"));
        SITES.put("uber", Map.of("reset","https://auth.uber.com/v2/forgot-password","twofa",true,"twofa_url","https://account.uber.com/security"));
        SITES.put("myfitnesspal", Map.of("reset","https://www.myfitnesspal.com/account/forgot_password","twofa",false,"twofa_url",""));
        SITES.put("bookmyshow", Map.of("reset","https://in.bookmyshow.com/forgotpassword","twofa",false,"twofa_url",""));
    }

    public static final List<String> GENERIC_ADVICE = List.of(
            "Open the site/app → Settings → Security/Password → change password.",
            "Use a UNIQUE password you have never used anywhere else.",
            "Enable two-factor authentication (2FA), preferably an authenticator app (Google Authenticator / Authy), not SMS.",
            "Never reuse the new password on any other site.",
            "Watch for phishing emails referencing this breach — do not click links in emails; go to the site directly."
    );

    public static final String TWOFA_DIRECTORY = "https://2fa.directory/";

    public Map<String, Object> remediationFor(BreachInfo breach) {
        String haystack = ((breach.getName() != null ? breach.getName() : "") + " "
                + (breach.getDomain() != null ? breach.getDomain() : "")).toLowerCase();

        for (var entry : SITES.entrySet()) {
            if (haystack.contains(entry.getKey())) {
                Map<String, Object> result = new HashMap<>(entry.getValue());
                result.put("matched", true);
                return result;
            }
        }

        String domain = breach.getDomain() != null ? breach.getDomain() : "";
        String guessed = !domain.isEmpty() ? "https://" + domain + "/" : "#";
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("matched", false);
        fallback.put("reset", guessed);
        fallback.put("twofa", null);
        fallback.put("twofa_url", TWOFA_DIRECTORY);
        fallback.put("note", "Generic link — navigate to the site's Settings → Security. Check 2fa.directory to confirm 2FA support.");
        return fallback;
    }
}
