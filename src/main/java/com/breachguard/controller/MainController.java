package com.breachguard.controller;

import com.breachguard.data.IndiaBreachData;
import com.breachguard.dto.*;
import com.breachguard.model.*;
import com.breachguard.repository.*;
import com.breachguard.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MainController {

    private final UserRepository userRepo;
    private final MonitoredAccountRepository monitoredRepo;
    private final NotificationRepository notifRepo;
    private final ResolvedBreachRepository resolvedRepo;
    private final HoneypotEventRepository eventRepo;
    private final BreachProviderService breachService;
    private final RiskScoreService riskService;
    private final PasswordCheckService passwordService;
    private final RemediationService remediationService;
    private final ThreatIntelService threatService;
    private final LegalService legalService;

    public MainController(UserRepository userRepo,
                          MonitoredAccountRepository monitoredRepo,
                          NotificationRepository notifRepo,
                          ResolvedBreachRepository resolvedRepo,
                          HoneypotEventRepository eventRepo,
                          BreachProviderService breachService,
                          RiskScoreService riskService,
                          PasswordCheckService passwordService,
                          RemediationService remediationService,
                          ThreatIntelService threatService,
                          LegalService legalService) {
        this.userRepo = userRepo;
        this.monitoredRepo = monitoredRepo;
        this.notifRepo = notifRepo;
        this.resolvedRepo = resolvedRepo;
        this.eventRepo = eventRepo;
        this.breachService = breachService;
        this.riskService = riskService;
        this.passwordService = passwordService;
        this.remediationService = remediationService;
        this.threatService = threatService;
        this.legalService = legalService;
    }

    private User getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElse(null);
    }

    // Inject current_user & unread count into every template
    @ModelAttribute
    public void addCommonAttributes(Model model,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = getUser(userDetails);
            model.addAttribute("currentUser", user);
            model.addAttribute("unreadNotifications",
                    notifRepo.countByUserIdAndIsReadFalse(user.getId()));
        }
    }

    // ── Landing ──

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ── Dashboard ──

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = getUser(ud);
        List<HoneypotEvent> events = eventRepo.findByUserId(user.getId());
        Map<String, Object> stats = new HashMap<>();
        stats.put("monitored", monitoredRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).size());
        stats.put("canaries", user.getCanaries().size());
        stats.put("events", events.size());
        stats.put("anomalies", events.stream().filter(HoneypotEvent::isAnomaly).count());
        stats.put("resolved", resolvedRepo.countByUserId(user.getId()));
        model.addAttribute("stats", stats);
        model.addAttribute("recentEvents", events.stream().limit(5).toList());
        return "dashboard";
    }

    // ── Email Breach Check ──

    @GetMapping("/check")
    public String checkForm() {
        return "check";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/check")
    public String check(@RequestParam String email,
                        @AuthenticationPrincipal UserDetails ud,
                        Model model) {
        User user = getUser(ud);
        email = email.trim().toLowerCase();

        if (email.isEmpty() || !email.contains("@")) {
            model.addAttribute("error", "Please enter a valid email address.");
            model.addAttribute("email", email);
            return "check";
        }

        Map<String, Object> raw = breachService.checkEmail(email);
        List<BreachInfo> breaches = (List<BreachInfo>) raw.get("breaches");

        // Get resolved breach names
        List<String> resolvedNames = resolvedRepo.findByUserIdAndAccount(user.getId(), email)
                .stream().map(ResolvedBreach::getBreachName).toList();

        // Score
        Object[] scoreResult = riskService.scoreBreaches(breaches);
        int score = (int) scoreResult[0];
        String level = (String) scoreResult[1];
        List<BreachInfo> scored = (List<BreachInfo>) scoreResult[2];

        double totalMarketValue = 0;
        Set<String> allDataClasses = new HashSet<>();
        List<BreachInfo> activeRaw = new ArrayList<>();

        for (BreachInfo b : scored) {
            b.setRemediation(remediationService.remediationFor(b));
            b.setResolved(resolvedNames.contains(b.getName()));
            totalMarketValue += threatService.estimateMarketValue(
                    b.getDataClasses() != null ? b.getDataClasses() : List.of());
            if (b.getDataClasses() != null) allDataClasses.addAll(b.getDataClasses());
            if (!b.isResolved()) activeRaw.add(b);
        }

        if (!resolvedNames.isEmpty()) {
            Object[] recalc = riskService.scoreBreaches(activeRaw);
            score = (int) recalc[0];
            level = (String) recalc[1];
        }

        List<AttackChain> attackChains = threatService.evaluateAttackChains(new ArrayList<>(allDataClasses));

        BreachResult result = new BreachResult();
        result.setBreaches(scored);
        result.setCount(scored.size());
        result.setProvider((String) raw.get("provider"));
        result.setProviderLabel(breachService.providerLabel((String) raw.get("provider")));
        result.setDemo((Boolean) raw.get("demo"));
        result.setError((String) raw.get("error"));
        result.setScore(score);
        result.setLevel(level);
        result.setLevelLabel(RiskScoreService.LEVEL_LABELS.get(level));
        result.setLevelColor(RiskScoreService.LEVEL_COLORS.get(level));
        result.setGenericAdvice(RemediationService.GENERIC_ADVICE);
        result.setTwofaDirectory(RemediationService.TWOFA_DIRECTORY);
        result.setMarketValue(totalMarketValue);
        result.setAttackChains(attackChains);
        result.setResolvedCount((int) scored.stream().filter(BreachInfo::isResolved).count());

        // Notification for high risk
        if (score >= 45) {
            notifRepo.save(new Notification(user, "breach",
                    "High exposure risk for " + email,
                    scored.size() + " breaches found, risk score " + score + "/100."));
        }

        model.addAttribute("result", result);
        model.addAttribute("email", email);
        return "check";
    }

    // ── Password Check ──

    @GetMapping("/password")
    public String passwordForm() {
        return "password";
    }

    @PostMapping("/password")
    public String passwordCheck(@RequestParam String password, Model model) {
        if (password.isEmpty()) {
            model.addAttribute("error", "Please enter a password to check.");
            return "password";
        }
        model.addAttribute("result", passwordService.checkPassword(password));
        return "password";
    }

    // ── Family / Monitored Accounts ──

    @GetMapping("/family")
    public String family(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = getUser(ud);
        model.addAttribute("accounts", monitoredRepo.findByUserIdOrderByCreatedAtDesc(user.getId()));
        return "family";
    }

    @PostMapping("/family")
    public String familyAdd(@RequestParam String account,
                            @RequestParam(defaultValue = "") String label,
                            @AuthenticationPrincipal UserDetails ud,
                            RedirectAttributes flash) {
        User user = getUser(ud);
        account = account.trim().toLowerCase();
        if (account.isEmpty() || !account.contains("@")) {
            flash.addFlashAttribute("error", "Enter a valid email address.");
            return "redirect:/family";
        }
        if (monitoredRepo.findByUserIdAndAccount(user.getId(), account).isPresent()) {
            flash.addFlashAttribute("info", "That account is already being monitored.");
            return "redirect:/family";
        }
        monitoredRepo.save(new MonitoredAccount(user, account, label.trim()));
        flash.addFlashAttribute("success", "Now monitoring " + account + ".");
        return "redirect:/family";
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/family/check/{id}")
    public String familyCheck(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails ud,
                              RedirectAttributes flash) {
        User user = getUser(ud);
        MonitoredAccount acc = monitoredRepo.findById(id).orElse(null);
        if (acc == null || !acc.getUser().getId().equals(user.getId())) {
            flash.addFlashAttribute("error", "Account not found.");
            return "redirect:/family";
        }
        Map<String, Object> raw = breachService.checkEmail(acc.getAccount());
        List<BreachInfo> breaches = (List<BreachInfo>) raw.get("breaches");
        acc.setLastChecked(Instant.now());
        acc.setLastBreachCount(breaches.size());
        monitoredRepo.save(acc);
        flash.addFlashAttribute(breaches.isEmpty() ? "success" : "warning",
                acc.getAccount() + ": " + breaches.size() + " breach(es) found via "
                        + breachService.providerLabel((String) raw.get("provider")) + ".");
        return "redirect:/family";
    }

    @PostMapping("/family/delete/{id}")
    public String familyDelete(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes flash) {
        User user = getUser(ud);
        MonitoredAccount acc = monitoredRepo.findById(id).orElse(null);
        if (acc != null && acc.getUser().getId().equals(user.getId())) {
            monitoredRepo.delete(acc);
            flash.addFlashAttribute("info", "Removed from monitoring.");
        }
        return "redirect:/family";
    }

    // ── India Feed ──

    @GetMapping("/india")
    public String india(Model model) {
        model.addAttribute("breaches", IndiaBreachData.INDIA_BREACHES);
        model.addAttribute("rights", IndiaBreachData.DPDP_RIGHTS);
        return "india";
    }

    // ── Legal Letter ──

    @GetMapping("/legal")
    public String legalForm(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = getUser(ud);
        model.addAttribute("userName", user.getName());
        model.addAttribute("userEmail", user.getEmail());
        return "legal";
    }

    @PostMapping("/legal")
    public String legalGenerate(@RequestParam String userName,
                                @RequestParam String userEmail,
                                @RequestParam(defaultValue = "") String userPhone,
                                @RequestParam(defaultValue = "") String companyName,
                                @RequestParam(defaultValue = "") String companyEmail,
                                @RequestParam(defaultValue = "") String breachName,
                                @RequestParam(defaultValue = "") String details,
                                Model model) {
        String letter = legalService.buildErasureLetter(userName, userEmail, userPhone,
                companyName, companyEmail, breachName, details);
        model.addAttribute("letter", letter);
        model.addAttribute("userName", userName);
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userPhone", userPhone);
        model.addAttribute("companyName", companyName);
        model.addAttribute("companyEmail", companyEmail);
        model.addAttribute("breachName", breachName);
        model.addAttribute("details", details);
        return "legal";
    }

    // ── Notifications ──

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = getUser(ud);
        List<Notification> notes = notifRepo.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (Notification n : notes) { n.setRead(true); }
        notifRepo.saveAll(notes);
        model.addAttribute("notifications", notes);
        return "notifications";
    }

    // ── Resolve Breach ──

    @PostMapping("/resolve_breach")
    public String resolveBreach(@RequestParam String account,
                                @RequestParam String breachName,
                                @RequestParam String email,
                                @AuthenticationPrincipal UserDetails ud,
                                RedirectAttributes flash) {
        User user = getUser(ud);
        if (account != null && breachName != null) {
            if (resolvedRepo.findByUserIdAndAccountAndBreachName(user.getId(), account, breachName).isEmpty()) {
                resolvedRepo.save(new ResolvedBreach(user, account, breachName));
                flash.addFlashAttribute("success",
                        "Awesome! You secured your account against the " + breachName + " breach.");
            }
        }
        // Re-post back to /check with the email
        flash.addFlashAttribute("repostEmail", email);
        return "redirect:/check";
    }

    // ── Data Broker Hub ──

    @GetMapping("/brokers")
    public String brokers(Model model) {
        List<Map<String, String>> brokerList = List.of(
                Map.of("name","Whitepages","url","https://www.whitepages.com/suppression-requests","difficulty","Easy","time","2 mins"),
                Map.of("name","Spokeo","url","https://www.spokeo.com/optout","difficulty","Easy","time","3 mins"),
                Map.of("name","BeenVerified","url","https://www.beenverified.com/app/optout/search","difficulty","Medium","time","5 mins"),
                Map.of("name","Intelius","url","https://suppression.peopleconnect.us/login","difficulty","Hard","time","10 mins"),
                Map.of("name","Acxiom","url","https://isapps.acxiom.com/optout/optout.aspx","difficulty","Medium","time","5 mins")
        );
        model.addAttribute("brokers", brokerList);
        return "brokers";
    }
}
