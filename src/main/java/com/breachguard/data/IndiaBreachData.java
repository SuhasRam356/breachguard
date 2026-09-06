package com.breachguard.data;

import java.util.List;
import java.util.Map;

/**
 * Curated India-specific breach feed and DPDP Act rights.
 * Port of Python data/india_breaches.py — was missing and broke compilation.
 */
public final class IndiaBreachData {

    private IndiaBreachData() {}

    public static final List<Map<String, String>> INDIA_BREACHES = List.of(
        Map.of(
            "name", "BigBasket (2020)",
            "year", "2020",
            "users", "20M+",
            "status", "Verified — CERT-In advisory",
            "summary", "20M BigBasket users had names, emails, phone numbers, addresses and hashed passwords leaked and sold on a dark-web forum.",
            "data", "Names, Emails, Phone, Addresses, Hashed Passwords, DOB"
        ),
        Map.of(
            "name", "MobiKwik (2021)",
            "year", "2021",
            "users", "3.5M (8.2 TB dump)",
            "status", "Verified — company denied, later acknowledged",
            "summary", "KYC data including PAN, Aadhaar last-4, wallet balances, phone, email and device info dumped on a hacker forum. One of India's largest KYC leaks.",
            "data", "Phone, Email, Hashed Passwords, KYC (PAN/Aadhaar), Wallet balance, Device ID"
        ),
        Map.of(
            "name", "Air India (2021)",
            "year", "2021",
            "users", "4.5M",
            "status", "Verified — Air India disclosure + SITA breach",
            "summary", "Air India's passenger system via SITA was breached. Passport, contact, ticket and credit-card data (including CVV era) exposed.",
            "data", "Names, Passport, Contact, Ticket, Credit Cards"
        ),
        Map.of(
            "name", "Domino's India (Jubilant FoodWorks, 2021)",
            "year", "2021",
            "users", "18M orders, 1M cards",
            "status", "Verified — dark-web sale",
            "summary", "18M Dominos India orders leaked including names, phones, emails, addresses; 1M credit-card details circulated separately.",
            "data", "Names, Phone, Email, Address, Order history, Payment cards (partial)"
        ),
        Map.of(
            "name", "AIIMS Delhi Ransomware (2022)",
            "year", "2022",
            "users", "40M patient records",
            "status", "Verified — Govt / CERT-In investigation",
            "summary", "Ransomware locked AIIMS servers; patient health records of crores including VIPs were exfiltrated and services disrupted for weeks.",
            "data", "Patient health records, PII, Contact details"
        ),
        Map.of(
            "name", "Aadhaar-adjacent exposures (2018-present)",
            "year", "Ongoing",
            "users", "Varies",
            "status", "Reports + UIDAI denials",
            "summary", "Multiple aggregator leaks and misconfigured databases exposed Aadhaar numbers, addresses and biometrics-adjacent data. CERT-In has flagged Aadhaar data handling repeatedly.",
            "data", "Aadhaar number, Name, Address, Phone, Photo"
        )
    );

    /**
     * Rights under the Digital Personal Data Protection Act, 2023.
     * Each entry is [Title, Description]
     */
    public static final List<String[]> DPDP_RIGHTS = List.of(
        new String[]{
            "Right to Access & Know (Sec. 11)",
            "Ask any Data Fiduciary what personal data it holds about you, the purpose of processing, and with whom it was shared. They must respond in plain language."
        },
        new String[]{
            "Right to Correction & Erasure (Sec. 12)",
            "Demand correction of inaccurate data and erasure of data that is no longer needed for the purpose it was collected — including data shared with processors."
        },
        new String[]{
            "Right to Grievance Redressal (Sec. 13)",
            "Every fiduciary must publish a Data Protection Officer / Grievance Officer and redress your complaint within prescribed timelines, or you can escalate to the Data Protection Board."
        },
        new String[]{
            "Right to Nominate (Sec. 14)",
            "Nominate another person to exercise your rights in case of death or incapacity — critical for family / household protection."
        },
        new String[]{
            "Duty to Report Breaches (Sec. 8 + CERT-In Directions 2022)",
            "Fiduciaries must report personal data breaches to the Data Protection Board and to affected principals, and notify CERT-In within 6 hours for cyber incidents."
        },
        new String[]{
            "Penalties for Misuse (Sec. 33)",
            "The Board can impose penalties up to ₹250 crore per violation for failing to protect data, failing to report breaches, or processing without valid consent."
        }
    );
}
