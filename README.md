# 🛡️ BreachGuard

**A breach-awareness web app that goes beyond Have I Been Pwned.**

HIBP answers one question: *"Was my email in a breach?"* BreachGuard answers the
questions that actually matter afterwards:

- **How bad is it for me?** → a personal 0–100 **risk score** weighted by data
  sensitivity (passwords/cards score highest) and breach recency.
- **What do I do right now?** → a **remediation hub** with direct password-reset
  and 2FA-setup links for every breached site.
- **What happens to leaked credentials afterwards?** → **honeypot canaries**
  (decoy credentials) that capture the *exact time, IP and geolocation* of an
  attacker reusing them, plus **impossible-travel anomaly detection**.
- **Are my family safe?** → a **family dashboard** monitoring multiple emails.
- **What about India?** → a curated **India breach feed**, **DPDP Act 2023
  rights** guide, and an **auto-generated erasure/grievance letter**.
- **How much is my data worth / how will I be attacked?** → **threat intel** with dark-web market value + realistic attack chains (SIM swap, credential stuffing, spear-phishing, etc.)
- Passwords are checked with **k-anonymity** — the password never leaves the
  device.
- **Resolved breaches** → mark a breach as fixed and see your risk score drop in real time.

> This repo is a **Java 17 / Spring Boot** port of the original Python/Flask concept. All business logic (risk scoring, honeypot, breach providers, remediation, legal) is faithfully ported to idiomatic Spring Boot.

---

## 1. Tech stack

| Layer | Technology | Why |
|---|---|---|
| Language | **Java 17** | LTS, strong typing, enterprise hiring signal |
| Web framework | **Spring Boot 3.4.3 + Spring Security** | Auto-configuration, embedded Tomcat, production-ready |
| Database | **H2 (file-based) + Spring Data JPA / Hibernate** | Zero-setup like SQLite, swappable to Postgres (just change `spring.datasource.url`) |
| Frontend | **Thymeleaf + Bootstrap 5 (CDN) + Layout Dialect** | Server-side rendering, no build step; responsive dark UI |
| External APIs | **LeakCheck** (free breach data), **Pwned Passwords** (free, k-anonymity), **ipwho.is** (free IP geolocation) | All free, no API key needed |
| Optional | **Have I Been Pwned API** (paid key) | Drop-in better data if you have a key — set `breachguard.hibp-api-key` |
| Build | **Maven** (`pom.xml` + `mvnw` wrapper) | Reproducible builds, CI friendly |

## 2. System architecture

```
                         ┌──────────────────────── Browser ────────────────────────┐
                         │  Bootstrap 5 UI (Thymeleaf templates)                    │
                         └───────────────▲─────────────────────────────────────────┘
                                         │ HTTP
                        ┌────────────────┴───────────────┐
                        │        Spring Boot app           │
                        │  controller/                     │
                        │   ├─ AuthController  (register/login)   │
                        │   ├─ MainController  (check, family,    │
                        │   │                 india, legal…)     │
                        │   └─ HoneypotController (canaries +    │
                        │                      public trap)      │
                        └───┬───────────┬─────────────┬───┘
                            │           │             │
              ┌─────────────▼──┐  ┌─────▼────────┐  ┌─▼──────────────────┐
              │  service/      │  │  model/ (JPA)│  │  External APIs     │
              │ BreachProvider │  │  User        │  │  LeakCheck (breach)│
              │ RiskScore      │  │  Canary      │  │  Pwned Passwords   │
              │ PasswordCheck  │  │  HoneypotEvt │  │  ipwho.is (geo)    │
              │ Honeypot       │──│  MonitoredAcct│ │  HIBP (optional)   │
              │ Remediation    │  │  Notification│  └────────────────────┘
              │ GeoIp / Legal  │  │  ResolvedBreach │
              │ ThreatIntel    │  └──────┬───────┘
              └────────────────┘         │
                                  ┌──────▼──────┐
                                  │  H2 file DB │  (./data/breachguard.mv.db)
                                  └─────────────┘
```

**Request flow example (email check):**
`User submits email → MainController.check() → BreachProviderService.checkEmail()`
`(normalizes LeakCheck/HIBP/demo) → RiskScoreService.scoreBreaches()`
`→ RemediationService.remediationFor() per breach → ThreatIntelService → renders check.html`.

**Request flow example (honeypot hit):**
`Attacker opens /honeypot/t/<token> and submits decoy login →`
`HoneypotController.trapPost() → HoneypotService.recordAttack() → GeoIpService.lookup() + impossible-travel check`
`→ HoneypotEvent row + Notification → attacker sees fake "invalid login"`.

## 3. Setup & run

### Prerequisites
- **JDK 17** (Temurin/Adoptium recommended)
- **Maven 3.9+** (or use included `mvnw` wrapper — no install needed)

### Quick start

```bash
# Clone
git clone https://github.com/SuhasRam356/breachguard.git
cd breachguard

# Run in dev mode (auto-reload via spring-boot-devtools)
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run

# Or build a jar and run it
./mvnw clean package -DskipTests
java -jar target/breachguard-1.0.0.jar
```

Open **http://localhost:8080**, register, and explore.

> H2 file DB is created automatically at `./data/breachguard.mv.db`. No DB setup needed. H2 console at http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/breachguard`).

### Demo script (for viva / recording)

1. Register an account.
2. **Email check** → enter `test@example.com` → instant sample breaches + risk score + fix links (works offline, no API limits).
3. **Password check** → enter `password123` → shows it's been seen millions of times.
4. **Canaries** → create one → open it → click **⚡ Simulate attack** twice → watch the second event flag *impossible travel* between Sydney & California.
5. **India feed** and **DPDP letter** → generate a ready-to-send legal letter.
6. **Family monitoring** → add `family@example.org` → run check → see breach history.
7. **Resolve** → mark a breach as resolved on the check page → risk score drops.

## 4. Optional: add a real HIBP key

Get a key at https://haveibeenpwned.com/API/Key, then set it in `src/main/resources/application.properties`:

```properties
breachguard.hibp-api-key=your-key-here
```

The app automatically prefers HIBP and falls back to LeakCheck/demo data. Without a key, it uses LeakCheck + built-in demo data (emails ending `@example.com` always show demo breaches).

## 5. Project structure

```
breachguard/
├─ pom.xml                                   # Maven build
├─ mvnw / mvnw.cmd                           # Maven wrappers
├─ src/
│  ├─ main/
│  │  ├─ java/com/breachguard/
│  │  │  ├─ BreachGuardApplication.java     # @SpringBootApplication entry
│  │  │  ├─ config/SecurityConfig.java      # SecurityFilterChain, BCrypt, CSRF rules
│  │  │  ├─ controller/
│  │  │  │  ├─ AuthController.java          # register / login
│  │  │  │  ├─ MainController.java          # dashboard, check, password, family, india, legal
│  │  │  │  └─ HoneypotController.java      # canary CRUD + public trap /honeypot/t/{token}
│  │  │  ├─ service/
│  │  │  │  ├─ BreachProviderService.java   # LeakCheck + HIBP adapters + demo data
│  │  │  │  ├─ PasswordCheckService.java    # k-anonymity Pwned Passwords
│  │  │  │  ├─ RiskScoreService.java        # 0–100 scoring engine
│  │  │  │  ├─ HoneypotService.java         # haversine + impossible-travel + simulate
│  │  │  │  ├─ GeoIpService.java            # ipwho.is wrapper
│  │  │  │  ├─ RemediationService.java      # reset/2FA links per site
│  │  │  │  ├─ ThreatIntelService.java      # market value + attack chains
│  │  │  │  └─ LegalService.java            # DPDP letter generator
│  │  │  ├─ data/IndiaBreachData.java       # curated India breach feed + DPDP rights
│  │  │  ├─ dto/                            # BreachInfo, BreachResult, GeoInfo, AttackChain, PasswordResult
│  │  │  ├─ model/                          # JPA: User, Canary, HoneypotEvent, MonitoredAccount, Notification, ResolvedBreach
│  │  │  └─ repository/                     # Spring Data JPA repos
│  │  └─ resources/
│  │     ├─ application.properties
│  │     ├─ static/style.css               # dark neon theme
│  │     └─ templates/                      # Thymeleaf + Bootstrap 5 pages
│  │        ├─ base.html / index.html / dashboard.html
│  │        ├─ check.html / password.html / family.html
│  │        ├─ honeypot.html / canary_detail.html / trap.html
│  │        └─ india.html / legal.html / brokers.html / notifications.html
│  └─ test/java/com/breachguard/BreachGuardApplicationTests.java
└─ .github/workflows/maven.yml               # CI
```

## 6. Ethics & legal notes

- Uses only **official/public APIs** and synthetic demo data. It never accesses stolen-data markets or real third-party credentials.
- Honeypot canaries use **fake** credentials you create; the trap page is a clearly fictional demo. Do not deploy real credential-harvesting pages.
- Breach data and the DPDP letter are **educational**, not legal advice; verify findings with official sources (CERT-In advisories).

## 7. Ideas to extend

- Email/Telegram/WhatsApp alerts on new breaches (HIBP webhook + async worker).
- Chart breach trends over time (Chart.js) on the India feed.
- Swap H2 → PostgreSQL and deploy on Render/Railway.
- Add a browser extension that warns on breached sites.
- Username/handle search across breaches.
- ML-based phishing detection.

## 8. What makes this unique vs HIBP?

| Capability | HIBP | BreachGuard |
|---|---|---|
| Email breach lookup | ✅ | ✅ (multi-source: LeakCheck + HIBP + demo) |
| Personal risk scoring & prioritization | ❌ | ✅ 0–100 weighted by sensitivity + recency |
| One-click remediation (reset/2FA links) | ❌ | ✅ 25+ sites mapped, fallback to 2fa.directory |
| Post-breach honeypot attack detection | ❌ | ✅ canary tokens + IP/geo + impossible-travel |
| Impossible-travel anomaly flagging | ❌ | ✅ haversine >900 km/h over >1000 km |
| Family/household dashboard | partial (paid) | ✅ unlimited, free |
| India breach feed + DPDP letter | ❌ | ✅ 6 curated Indian breaches + DPDP rights + letter generator |
| Dark-web market value + attack chains | ❌ | ✅ $ value per breach + 5 attack-chain narratives |
| Password k-anonymity check | ✅ (separate page) | ✅ integrated, same privacy guarantee |
| Broker opt-out hub | ❌ | ✅ 5 data brokers with opt-out links |
| Resolved-breach tracking | ❌ | ✅ risk score recalculates live |
