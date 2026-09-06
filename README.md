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
- Passwords are checked with **k-anonymity** — the password never leaves the
  device.

---

## 1. Tech stack

| Layer | Technology | Why |
|---|---|---|
| Language | **Python 3** | Beginner-friendly, great security libraries |
| Web framework | **Flask** | Lightweight, easy to learn; application-factory + blueprint structure |
| Database | **SQLite + SQLAlchemy** | Zero-setup database, real ORM, swappable to Postgres later |
| Frontend | **Jinja2 templates + Bootstrap 5 (CDN)** | No build step; responsive dark UI |
| External APIs | **LeakCheck** (free breach data), **Pwned Passwords** (free, k-anonymity), **ipwho.is** (free IP geolocation) | All free, no API key needed |
| Optional | **Have I Been Pwned API** (paid key) | Drop-in better data if you have a key |

## 2. System architecture

```
                         ┌──────────────────────── Browser ────────────────────────┐
                         │  Bootstrap 5 UI (Jinja2 templates)                       │
                         └───────────────▲─────────────────────────────────────────┘
                                         │ HTTP
                        ┌────────────────┴───────────────┐
                        │          Flask app              │
                        │  blueprints/                    │
                        │   ├─ auth.py   (register/login) │
                        │   ├─ main.py   (check, family,  │
                        │   │            india, legal…)   │
                        │   └─ honeypot.py (canaries +    │
                        │                 public trap)    │
                        └───┬───────────┬─────────────┬───┘
                            │           │             │
              ┌─────────────▼──┐  ┌─────▼────────┐  ┌─▼──────────────────┐
              │  services/     │  │  models.py   │  │  External APIs     │
              │ breach_providers│ │  (SQLAlchemy)│  │  LeakCheck (breach)│
              │ risk_score     │  │  User        │  │  Pwned Passwords   │
              │ password_check │  │  Canary      │  │  ipwho.is (geo)    │
              │ honeypot       │──│  HoneypotEvt │  │  HIBP (optional)   │
              │ remediation    │  │  MonitoredAcct│ └────────────────────┘
              │ geoip / legal  │  │  Notification │
              └────────────────┘  └──────┬───────┘
                                         │
                                  ┌──────▼──────┐
                                  │  SQLite DB  │  (instance/breachguard.db)
                                  └─────────────┘
```

**Request flow example (email check):**
`User submits email → main.check() → breach_providers.check_email()
(normalizes results from LeakCheck/HIBP/demo) → risk_score.score_breaches()
→ remediation.remediation_for() per breach → renders check.html`.

**Request flow example (honeypot hit):**
`Attacker opens /honeypot/t/<token> and submits the decoy login →
honeypot.trap() → record_attack() → geoip.lookup() + impossible-travel check
→ HoneypotEvent row + Notification → attacker sees a fake "invalid login"`.

## 3. Setup & run (Antigravity IDE)

1. **Open the project folder** `breachguard` in Antigravity (File → Open Folder).
2. Open a terminal in the IDE (Terminal → New Terminal) and run:

   ```bash
   # create a virtual environment
   python -m venv .venv

   # activate it
   #  • macOS/Linux:
   source .venv/bin/activate
   #  • Windows (PowerShell):
   .venv\Scripts\Activate.ps1

   # install dependencies
   pip install -r requirements.txt

   # (optional) create your env file
   cp .env.example .env      # Windows: copy .env.example .env
   ```

3. **Run the app:**

   ```bash
   python run.py
   ```

4. Open **http://127.0.0.1:5000** in your browser, register, and start exploring.

> The database file is created automatically on first run. No DB setup needed.

### Try the demo script (for viva/recording)

1. Register an account.
2. **Email check** → enter `test@example.com` → instant sample breaches + risk
   score + fix links (works offline, no API limits).
3. **Password check** → enter `password123` → shows it's been seen millions of
   times.
4. **Canaries** → create one → open it → click **⚡ Simulate attack** twice →
   watch the second event flag *impossible travel* between two countries.
5. **India feed** and **DPDP letter** → generate a ready-to-send legal letter.

## 4. Optional: add a real HIBP key

Get a key at https://haveibeenpwned.com/API/Key, then put it in `.env`:

```
HIBP_API_KEY=your-key-here
```

The app automatically prefers HIBP and falls back to LeakCheck/demo data.

## 5. Project structure

```
breachguard/
├─ run.py                     # entry point
├─ requirements.txt
├─ .env.example
└─ app/
   ├─ __init__.py             # app factory, login_required, current_user
   ├─ config.py               # configuration (API keys from .env)
   ├─ extensions.py           # SQLAlchemy instance
   ├─ models.py               # User, MonitoredAccount, Canary, HoneypotEvent, Notification
   ├─ blueprints/
   │   ├─ auth.py             # register/login/logout
   │   ├─ main.py             # dashboard, check, password, family, india, legal, notifications
   │   └─ honeypot.py         # canary dashboard + public attacker trap page
   ├─ services/
   │   ├─ breach_providers.py # LeakCheck + HIBP adapters + demo data (normalized)
   │   ├─ password_check.py   # k-anonymity Pwned Passwords
   │   ├─ risk_score.py       # 0–100 scoring engine
   │   ├─ honeypot.py         # attack recording, geo, impossible-travel, simulator
   │   ├─ geoip.py            # ipwho.is wrapper
   │   ├─ remediation.py      # reset/2FA links per site
   │   └─ legal.py            # DPDP letter generator
   ├─ data/
   │   └─ india_breaches.py   # curated India breach feed + DPDP rights
   ├─ static/style.css
   └─ templates/              # Jinja2 + Bootstrap 5 pages
```

## 6. Ethics & legal notes

- Uses only **official/public APIs** and synthetic demo data. It never accesses
  stolen-data markets or real third-party credentials.
- Honeypot canaries use **fake** credentials you create; the trap page is a
  clearly fictional demo. Do not deploy real credential-harvesting pages.
- Breach data and the DPDP letter are **educational**, not legal advice; verify
  findings with official sources (CERT-In advisories).

## 7. Ideas to extend

- Email/Telegram/WhatsApp alerts on new breaches (HIBP webhook + async worker).
- Chart breach trends over time (Chart.js) on the India feed.
- Swap SQLite → PostgreSQL and deploy on Render/Railway.
- Add a browser extension that warns on breached sites.
- Username/handle search across breaches.
