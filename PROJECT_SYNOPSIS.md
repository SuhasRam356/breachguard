# Project Synopsis — BreachGuard: Beyond Breach Notification

## 1. Problem statement

Services like **Have I Been Pwned (HIBP)** tell a user *whether* and *where*
their email appeared in a public data breach, but they stop there. They do not:

1. quantify how much **risk** the exposed data creates for that individual;
2. tell the user **exactly what to do** next (which password to change, where to
   enable 2FA);
3. show what happens to credentials **after** a breach (credential stuffing);
4. monitor an entire **household**;
5. cover **Indian incidents** or guide users through their rights under the
   **Digital Personal Data Protection (DPDP) Act, 2023**;
6. quantify the dark-web value of leaked data or the attack chains it enables.

**BreachGuard** fills these gaps as an educational web application built with **Java 17 + Spring Boot**.

## 2. Objectives

- Check an email against live breach databases and present results with a
  **personal 0–100 risk score** and prioritized fix plan.
- Check passwords using the **k-anonymity** Pwned Passwords protocol without
  exposing the password.
- Provide a **remediation hub** linking directly to password-reset and 2FA pages.
- Implement **honeypot canary credentials** that record attacker reuse events
  (timestamp, IP, geolocation) and flag **impossible-travel anomalies** —
  demonstrating the observable aftermath of a breach.
- Offer a **family monitoring** dashboard for household emails.
- Provide an **India breach awareness feed** and an automated **DPDP Act erasure
  / grievance letter generator**.
- Show **threat intel**: estimated dark-web market value and realistic attack-chain narratives (SIM swap, credential stuffing, spear-phishing, financial fraud, synthetic identity).
- Allow **resolved-breach tracking** — risk score recalculates when you mark a breach fixed.

## 3. Scope & feasibility

All data comes from free, public, legitimate APIs (LeakCheck, Pwned Passwords,
ipwho.is) with optional HIBP integration; the app also ships built-in demo data
so it works fully offline for evaluation (any `@example.com` email). No stolen data is handled, keeping the
project legal and ethical under the DPDP Act and IT Act. Persistence is zero-config via H2 file DB.

## 4. System architecture

Three-layer architecture on **Spring Boot 3.4 (Java 17)**:

- **Presentation layer** — Thymeleaf templates + Bootstrap 5 (responsive dark UI), Layout Dialect for inheritance.
- **Application layer** — Spring MVC controllers (`AuthController`, `MainController`, `HoneypotController`)
  orchestrating a service layer:
  - *BreachProviderService* (adapter pattern for LeakCheck/HIBP/demo),
  - *RiskScoreService* (weighted sensitivity + recency model),
  - *PasswordCheckService* (SHA-1 + k-anonymity),
  - *HoneypotService* (event capture, geolocation, haversine-based impossible-travel),
  - *RemediationService*, *GeoIpService*, *LegalService*, *ThreatIntelService*.
- **Data layer** — Spring Data JPA / Hibernate ORM on H2: `User`, `MonitoredAccount`,
  `Canary`, `HoneypotEvent`, `Notification`, `ResolvedBreach`.
- **Security** — Spring Security with BCrypt, CSRF (except public trap), H2 console guarded.

## 5. Key algorithms

- **Risk score** — for each breach, sum weights of exposed data classes
  (passwords 40, payment data 25, phone 15, … capped at 70), apply a recency multiplier
  (≤1 yr ×1.5, ≤2 yr ×1.3, ≤5 yr ×1.15), add +8 per additional breach, cap at 100. Level thresholds: ≥70 critical, ≥45 high, ≥20 medium.
- **k-anonymity password check** — SHA-1 hash locally; send only the first 5 hex
  chars to `api.pwnedpasswords.com/range/<prefix>`; match the suffix among ~800 returned hashes locally. Password never leaves the device.
- **Impossible-travel detection** — great-circle (haversine) distance between
  consecutive canary-login coordinates divided by elapsed time; speeds exceeding
  ~900 km/h over >1000 km (or >500 km flagged as distant) are flagged as physically impossible.
- **Threat intel** — per-data-class market value heuristic ($1 email → $40 SSN/Aadhaar) + rule-based attack chains (email+password → credential stuffing, phone+email → SIM swap, etc.).

## 6. Novelty vs. HIBP

| Capability | HIBP | BreachGuard |
|---|---|---|
| Email breach lookup | ✅ | ✅ (multi-source: LeakCheck + HIBP + demo) |
| Personal risk scoring & prioritization | ❌ | ✅ 0–100 + color + per-breach points |
| One-click remediation (reset/2FA links) | ❌ | ✅ 25+ sites + 2fa.directory fallback |
| Post-breach honeypot attack detection | ❌ | ✅ canary tokens → IP / city / geo |
| Impossible-travel anomaly flagging | ❌ | ✅ haversine >900 km/h + distant-login |
| Family/household dashboard | partial (paid) | ✅ free, unlimited |
| India breach feed + DPDP letter | ❌ | ✅ 6 curated Indian breaches + rights + letter |
| Dark-web market value + attack chains | ❌ | ✅ $ estimate + 5 chain narratives |
| Broker opt-out hub | ❌ | ✅ 5 data brokers |
| Resolved-breach tracking | ❌ | ✅ live risk recalc |
| Regional legal guidance | ❌ | ✅ DPDP Act 2023 mapping |

## 7. Testing

Spring Boot Test (`@SpringBootTest`) context load test included. Manual end-to-end (and viva script): registration → demo breach check (`test@example.com`), pwned-password (`password123`), canary creation → simulate attack twice → impossible travel flag, family monitoring, DPDP letter, public trap page. Tested services in isolation: risk scoring, HIBP/LeakCheck adapters, haversine, password k-anonymity.

## 8. Future work

WhatsApp/Telegram/email alerts via HIBP webhook + async worker, breach-trend visualizations (Chart.js) on India feed, H2 → PostgreSQL + cloud deployment (Render/Railway), browser extension that warns on breached sites, username/handle search, ML-based phishing detection, OAuth2 login.

## 9. References

- Have I Been Pwned — https://haveibeenpwned.com
- LeakCheck public API — https://leakcheck.io/api
- Pwned Passwords (k-anonymity) — https://haveibeenpwned.com/Passwords
- ipwho.is — https://ipwho.is
- 2FA Directory — https://2fa.directory
- CERT-In — https://www.cert-in.org.in
- Digital Personal Data Protection Act, 2023 (India)
