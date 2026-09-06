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
   **Digital Personal Data Protection (DPDP) Act, 2023**.

**BreachGuard** fills these gaps as an educational web application.

## 2. Objectives

- Check an email against live breach databases and present results with a
  **personal 0–100 risk score** and prioritized fix plan.
- Check passwords using the **k-anonymity** Pwned Passwords protocol without
  exposing the password.
- Provide a **remediation hub** linking directly to password-reset and 2FA pages.
- Implement **honeypot canary credentials** that record attacker reuse events
  (timestamp, IP, geolocation) and flag **impossible-travel anomalies** —
  demonstrating the observable aftermath of a breach.
- Offer a **family monitoring** dashboard.
- Provide an **India breach awareness feed** and an automated **DPDP Act erasure
  / grievance letter generator**.

## 3. Scope & feasibility

All data comes from free, public, legitimate APIs (LeakCheck, Pwned Passwords,
ipwho.is) with optional HIBP integration; the app also ships built-in demo data
so it works fully offline for evaluation. No stolen data is handled, keeping the
project legal and ethical under the DPDP Act and IT Act.

## 4. System architecture

Three-layer architecture on Flask:

- **Presentation layer** — Jinja2 templates with Bootstrap 5 (responsive dark UI).
- **Application/business layer** — Flask blueprints (`auth`, `main`, `honeypot`)
  orchestrating a service layer:
  - *breach_providers* (adapter pattern for LeakCheck/HIBP/demo),
  - *risk_score* (weighted sensitivity + recency model),
  - *password_check* (SHA-1 + k-anonymity),
  - *honeypot* (event capture, geolocation, haversine-based impossible-travel),
  - *remediation*, *geoip*, *legal*.
- **Data layer** — SQLite via SQLAlchemy ORM: `User`, `MonitoredAccount`,
  `Canary`, `HoneypotEvent`, `Notification`.

## 5. Key algorithms

- **Risk score** — for each breach, sum weights of exposed data classes
  (passwords 40, payment data 25, phone 15, …), apply a recency multiplier
  (≤1 yr ×1.5), add a per-additional-breach reuse penalty, cap at 100.
- **k-anonymity password check** — SHA-1 hash locally; send only the first 5 hex
  chars; match the suffix among ~500 returned hashes locally.
- **Impossible-travel detection** — great-circle (haversine) distance between
  consecutive login coordinates divided by elapsed time; speeds exceeding
  ~900 km/h over >1000 km are flagged as physically impossible.

## 6. Novelty vs. HIBP

| Capability | HIBP | BreachGuard |
|---|---|---|
| Email breach lookup | ✅ | ✅ (multi-source) |
| Personal risk scoring & prioritization | ❌ | ✅ |
| One-click remediation (reset/2FA links) | ❌ | ✅ |
| Post-breach honeypot attack detection | ❌ | ✅ |
| Impossible-travel anomaly flagging | ❌ | ✅ |
| Family/household dashboard | partial (paid) | ✅ |
| India breach feed + DPDP letter | ❌ | ✅ |
| Regional legal guidance | ❌ | ✅ |

## 7. Testing

End-to-end tested with Flask's test client covering: registration, demo and live
breach checks, pwned-password detection, canary creation, simulated attacks,
impossible-travel flagging, family monitoring, letter generation, and the public
trap page.

## 8. Future work

WhatsApp/Telegram/email alerts, breach-trend visualizations, username search,
browser extension, PostgreSQL + cloud deployment, and ML-based phishing detection.

## 9. References

- Have I Been Pwned — https://haveibeenpwned.com
- LeakCheck public API — https://leakcheck.io/api
- Pwned Passwords (k-anonymity) — https://haveibeenpwned.com/Passwords
- CERT-In — https://www.cert-in.org.in
- Digital Personal Data Protection Act, 2023
