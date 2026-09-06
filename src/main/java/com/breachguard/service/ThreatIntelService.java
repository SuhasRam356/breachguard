package com.breachguard.service;

import com.breachguard.dto.AttackChain;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Threat Intelligence — exact port of Python threat_intel.py. */
@Service
public class ThreatIntelService {

    public double estimateMarketValue(List<String> dataClasses) {
        double value = 0.0;
        for (String cls : dataClasses) {
            String lower = cls.toLowerCase();
            if (lower.contains("password")) value += 2.0;
            else if (lower.contains("email")) value += 1.0;
            else if (lower.contains("phone")) value += 5.0;
            else if (lower.contains("date of birth")) value += 2.0;
            else if (lower.contains("address") || lower.contains("physical")) value += 4.0;
            else if (lower.contains("credit") || lower.contains("card") || lower.contains("payment")) value += 15.0;
            else if (lower.contains("social security") || lower.contains("passport")
                    || lower.contains("aadhaar") || lower.contains("national")) value += 40.0;
            else value += 0.50;
        }
        return Math.round(value * 100.0) / 100.0;
    }

    public List<AttackChain> evaluateAttackChains(List<String> dataClasses) {
        List<String> lower = dataClasses.stream().map(String::toLowerCase).toList();
        boolean hasEmail = lower.stream().anyMatch(c -> c.contains("email"));
        boolean hasPhone = lower.stream().anyMatch(c -> c.contains("phone"));
        boolean hasPassword = lower.stream().anyMatch(c -> c.contains("password"));
        boolean hasDob = lower.stream().anyMatch(c -> c.contains("date of birth"));
        boolean hasFinancial = lower.stream().anyMatch(c -> c.contains("credit") || c.contains("card") || c.contains("payment") || c.contains("bank"));
        boolean hasId = lower.stream().anyMatch(c -> c.contains("social security") || c.contains("passport") || c.contains("aadhaar") || c.contains("national id"));

        List<AttackChain> chains = new ArrayList<>();

        if (hasEmail && hasPassword)
            chains.add(new AttackChain("Credential Stuffing",
                    "Attackers will use bots to try this exact email and password combination across hundreds of other sites (banks, social media, shopping) to see if you reused it."));
        if (hasPhone && hasEmail)
            chains.add(new AttackChain("SIM Swapping / Hijacking",
                    "Attackers can call your mobile carrier impersonating you using your exposed details to port your number, allowing them to intercept SMS 2FA codes."));
        if (hasEmail && hasDob)
            chains.add(new AttackChain("Targeted Spear-Phishing",
                    "Scammers can send highly convincing emails referencing your actual birth date to build trust and trick you into clicking malicious links."));
        if (hasFinancial)
            chains.add(new AttackChain("Direct Financial Fraud",
                    "Your payment information can be used directly for unauthorized purchases or sold in carding forums on the dark web."));
        if (hasId)
            chains.add(new AttackChain("Synthetic Identity Theft",
                    "Criminals can combine your real ID numbers with fake names/addresses to open lines of credit or file fraudulent tax returns."));

        return chains;
    }
}
