<div align="center">
  <img src="https://img.shields.io/badge/Java-17-blue.svg" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Gradle-8.9-02303A.svg" alt="Gradle">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</div>

<br>

<h1 align="center">BreachGuard: Enterprise-Grade Data Breach & Risk Mitigation Platform</h1>

<p align="center">
  <strong>Proactive data breach monitoring, privacy remediation, and threat intelligence hub.</strong>
</p>

---

## 📑 Table of Contents

1. [Introduction](#-introduction)
2. [Key Features](#-key-features)
    - [Account & Password Security](#account--password-security)
    - [Active Threat Intelligence (Honeypots)](#active-threat-intelligence-honeypots)
    - [Remediation & Legal Framework](#remediation--legal-framework)
    - [Transparency & Insights](#transparency--insights)
3. [System Architecture](#-system-architecture)
4. [Technology Stack](#-technology-stack)
5. [New V2 Updates](#-new-v2-updates)
6. [Getting Started (Installation)](#-getting-started)
    - [Prerequisites](#prerequisites)
    - [Local Development Setup](#local-development-setup)
7. [Usage Guide](#-usage-guide)
8. [Browser Extension (Real-Time Protection)](#-browser-extension)
9. [Database Schema](#-database-schema)
10. [API Documentation](#-api-documentation)
11. [PDF Reporting Module](#-pdf-reporting-module)
12. [Privacy & Security Notice](#-privacy--security-notice)
13. [Contributing](#-contributing)
14. [Future Scope](#-future-scope)
15. [License](#-license)
16. [Acknowledgments](#-acknowledgments)

---

## 🚀 Introduction

In an era where data breaches are ubiquitous, reactive measures are no longer sufficient. **BreachGuard** is a robust, full-stack Java application designed to empower users to take back control of their digital footprint. 

Unlike traditional breach checkers that simply tell you if your data was exposed, BreachGuard provides an end-to-end ecosystem: it checks for breaches, computes a personalized risk score, helps you draft legal data-erasure requests (DPDP Act compliant), and allows you to deploy active honeypots (Canary Tokens) to catch attackers probing your leaked infrastructure.

---

## ✨ Key Features

### Account & Password Security
* **Email & Phone Breach Monitoring:** Seamlessly integrates with the HaveIBeenPwned (HIBP) ecosystem and custom localized datasets (e.g., `IndiaBreachData`) to detect if your email or phone number was compromised in a known breach.
* **k-Anonymity Password Checking:** Implements the mathematically secure k-anonymity model. When checking if a password is breached, the application hashes it via SHA-1 locally, and only sends the first 5 characters (the prefix) to the public API. It then compares the remaining 35 characters locally, ensuring your raw password and even its full hash never leaves your machine.
* **Family Monitoring:** A unified dashboard to monitor accounts of family members, tracking breach history, and resolving active threats systematically.

### Active Threat Intelligence (Honeypots)
* **Canary Tokens (Honeytokens):** Deploy unique, trackable URLs specifically designed to be hidden in your password managers, private databases, or confidential documents. 
* **Real-time Alerting:** The moment an attacker discovers and clicks a Canary Token, BreachGuard logs the event (including IP address, geolocation, User-Agent, and exact timestamp) and triggers an immediate alert.
* **Global Threat Mapping:** Understand where attacks are originating from across the globe based on interactions with your deployed honeypots.

### Remediation & Legal Framework
* **Data Broker Opt-Out Hub:** A curated, easy-to-use directory containing direct links, difficulty ratings, and time estimates for opting out of major data brokers (Whitepages, Spokeo, Acxiom, etc.).
* **Automated DPDP Erasure Letters:** Generates legally sound, professionally drafted "Right to Erasure" letters under the Digital Personal Data Protection (DPDP) Act, 2023. This empowers users to force non-compliant companies to delete their data.
* **Actionable Risk Scoring:** Dynamically computes a holistic risk score (0-100) based on the severity of the breaches, the types of data exposed (passwords, SSNs, credit cards), and the number of unmitigated accounts.

### Transparency & Insights
* **Public Transparency Page:** An open, anonymized dashboard displaying aggregate statistics of the BreachGuard network. View the total number of canaries deployed, the total number of attacks caught, and a breakdown of the top origin countries for these attacks.
* **Comprehensive PDF Reports:** Generate and download a polished, fully formatted PDF report containing your risk score, monitored accounts, breach history, and customized legal letters for offline filing.

---

## 🏗 System Architecture

The application follows a standard Model-View-Controller (MVC) architectural pattern, heavily leveraging the Spring Boot ecosystem for dependency injection, security, and persistence.

```mermaid
graph TD
    subgraph Client Tier
        UI[Web Browser / UI]
        EXT[Chrome Extension]
    end

    subgraph Presentation Tier (Spring MVC)
        MC[MainController]
        AC[ApiController]
    end

    subgraph Business Logic Tier (Spring Services)
        BS[BreachProviderService]
        PS[PasswordCheckService]
        RS[RiskScoreService]
        LS[LegalService]
        TS[ThreatIntelService]
        PE[PdfExportService]
    end

    subgraph Data Access Tier (Spring Data JPA)
        UR[UserRepository]
        MAR[MonitoredAccountRepository]
        HR[HoneypotEventRepository]
        CR[CanaryRepository]
    end

    subgraph Database Tier
        H2[(H2 File Database)]
    end
    
    subgraph External APIs
        HIBP[HaveIBeenPwned API]
        IP[IP-API Geolocation]
    end

    UI -->|HTTP GET/POST| MC
    EXT -->|REST/CORS| AC
    MC --> BS & PS & RS & LS & TS & PE
    AC --> PS
    
    BS --> HIBP
    TS --> IP
    
    BS & PS & RS & LS & TS & PE --> UR & MAR & HR & CR
    UR & MAR & HR & CR --> H2
```

---

## 💻 Technology Stack

* **Backend Framework:** Java 17, Spring Boot 3.4.x
* **Build System:** Gradle (Wrapper included)
* **Templating Engine:** Thymeleaf 
* **Security:** Spring Security 6 (BCrypt Hashing, Session Management)
* **Database:** H2 Database (Persistent File Mode - `./data/breachguard`)
* **ORM:** Spring Data JPA (Hibernate)
* **PDF Generation:** Flying Saucer (OpenPDF / iText)
* **Frontend:** HTML5, CSS3 (Custom Glassmorphism UI), Bootstrap 5.3
* **Browser Extension:** JavaScript (Manifest V3)

---

## 🚀 New V2 Updates

BreachGuard has recently been upgraded from a basic Python Flask prototype to a fully-fledged Java Enterprise application.

**V2 Additions Include:**
1. **Gradle Migration:** Shifted from Maven to Gradle for faster builds and better dependency management.
2. **Chrome Extension MVP:** Added a real-time browser extension that hooks into our backend API to instantly verify passwords as they are typed.
3. **Transparency Dashboard:** A public `/transparency` endpoint showcasing anonymized attack metrics caught by our honeypot network.
4. **PDF Reporting Engine:** Integrated Flying Saucer to render our Thymeleaf templates into professional PDF documents for user download.

---

## 🛠 Getting Started

### Prerequisites

Ensure you have the following installed on your local machine:
*   **Java Development Kit (JDK) 17** (or higher, preferably LTS)
*   **Git** (for cloning the repository)
*   **A Modern Web Browser** (Chrome/Edge/Firefox/Safari)

*Note: You do not need to install Gradle manually; the project includes a Gradle Wrapper (`gradlew`).*

### Local Development Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/SuhasRam356/breachguard.git
   cd breachguard
   ```

2. **Verify Java Version:**
   ```bash
   java -version
   # Should output java version "17.x.x" or higher.
   ```

3. **Build the Application:**
   On Windows:
   ```bash
   .\gradlew.bat build
   ```
   On Linux/macOS:
   ```bash
   ./gradlew build
   ```

4. **Run the Application:**
   On Windows:
   ```bash
   .\gradlew.bat bootRun
   ```
   On Linux/macOS:
   ```bash
   ./gradlew bootRun
   ```

5. **Access the Application:**
   Open your browser and navigate to: `http://localhost:8080`

---

## 📖 Usage Guide

### 1. Registration & Login
Start by creating an account. Your passwords are encrypted using `BCryptPasswordEncoder`. Once logged in, you will be redirected to the main **Dashboard**.

### 2. Monitoring Accounts
Navigate to **Family Monitoring** or use the quick actions on the dashboard to add emails or phone numbers. The system will immediately cross-reference these against known breach databases.

### 3. Reviewing Breaches & Risk Score
If breaches are found, your Risk Score will dynamically update. High-risk data classes (like SSNs or Passwords) weigh heavily on this score. You can mark breaches as "Resolved" once you have taken remediation steps (like changing your password).

### 4. Deploying Canary Tokens
Navigate to the **Honeypot** section. Generate a new token and place the generated URL somewhere secure (e.g., a note in your LastPass vault titled "AWS Root Credentials"). If anyone accesses that URL, you will receive an immediate alert detailing their IP and location.

### 5. Legal Remediation
Use the **Legal Letters** section to select a specific breach. The system will auto-populate a Right to Erasure letter citing the specific laws of the DPDP Act. You can copy this and email it to the offending company's Grievance Officer.

---

## 🧩 Browser Extension

BreachGuard includes a Chrome Extension stub designed to provide real-time protection by pinging the backend API when users type passwords into potentially vulnerable sites.

### Installation Instructions
1. Open Google Chrome or any Chromium-based browser.
2. Navigate to `chrome://extensions/`.
3. Enable **"Developer mode"** in the top right corner.
4. Click **"Load unpacked"**.
5. Select the `extension/` directory located within the cloned `breachguard` repository.
6. The extension is now active and will communicate with your local server running on `http://localhost:8080`.

*Note: The extension uses the `/api/check-password` endpoint. This endpoint is configured in Spring Security to allow CORS and public access.*

---

## 📊 Database Schema

BreachGuard uses a relational database model mapped via Hibernate. Below is a high-level overview of the core entities:

*   **`User`**: Primary entity storing account details, encrypted passwords, and user preferences.
*   **`MonitoredAccount`**: Represents an email or phone number being tracked for a specific user.
*   **`ResolvedBreach`**: Tracks which breaches a user has acknowledged and mitigated, preventing them from affecting the active risk score.
*   **`Canary`**: Represents a deployed honeypot token, mapped many-to-one to a `User`.
*   **`HoneypotEvent`**: Represents a trigger event when a `Canary` is accessed. Stores `ipAddress`, `userAgent`, `country`, `city`, and timestamp.
*   **`Notification`**: System alerts (e.g., "Canary Triggered!") sent to the user.

---

## 🔌 API Documentation

### `GET /api/check-password`
Checks if a password has been compromised using the k-anonymity model.

**Parameters:**
*   `prefix` (String): The first 5 characters of the SHA-1 hash of the password.
*   `hash` (String): The remaining 35 characters of the SHA-1 hash.

**Response (JSON):**
```json
{
  "isBreached": true,
  "count": 45123
}
```

### `GET /transparency`
Publicly accessible web view displaying aggregate system statistics. Does not require authentication.

### `GET /honeypot/t/{token}`
The public endpoint hit by attackers when they trigger a Canary Token. Captures request metadata and redirects to a blank page to avoid suspicion.

---

## 📄 PDF Reporting Module

The system integrates **Flying Saucer** (an XML/CSS to PDF renderer) to generate downloadable reports. 

When a user navigates to `/export-report`, the `PdfExportService` processes a strict XHTML Thymeleaf template (`pdf-report.html`), injects the user's data, risk score, breach history, and their personalized DPDP legal letter, and streams it back to the browser as a downloadable `.pdf` file with the `application/pdf` MIME type.

---

## 🔒 Privacy & Security Notice

*   **Zero-Knowledge Passwords:** We do NOT store your passwords in plain text.
*   **Local H2 Database:** By default, data is stored locally on your machine in the `./data/` directory.
*   **API Security:** The password checker utilizes k-anonymity. We never send your actual password over the network, only the first 5 characters of its SHA-1 hash.

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 🔮 Future Scope

*   **Dockerization:** Provide a `Dockerfile` and `docker-compose.yml` for effortless deployment alongside a robust database like PostgreSQL.
*   **Email Notifications:** Integrate JavaMailSender to alert users via email when a Canary is triggered.
*   **Automated Takedowns:** Integrate with external legal APIs to automatically dispatch DPDP letters to offending companies.
*   **Enhanced Threat Intel:** Feed honeypot IP data into an active blocklist API for firewall consumption.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 🙏 Acknowledgments

*   [HaveIBeenPwned API](https://haveibeenpwned.com/API/v3) by Troy Hunt.
*   [IP-API](https://ip-api.com/) for Geolocation services.
*   [Spring Boot](https://spring.io/projects/spring-boot) framework.
*   [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) for PDF generation.
*   UI Inspiration from modern Glassmorphism design trends.

<br>
<p align="center">
  <i>Built with ❤️ for privacy and security.</i>
</p>
