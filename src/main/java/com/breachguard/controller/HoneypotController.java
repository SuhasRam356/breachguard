package com.breachguard.controller;

import com.breachguard.model.Canary;
import com.breachguard.model.HoneypotEvent;
import com.breachguard.model.User;
import com.breachguard.repository.CanaryRepository;
import com.breachguard.repository.NotificationRepository;
import com.breachguard.repository.UserRepository;
import com.breachguard.service.HoneypotService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/honeypot")
public class HoneypotController {

    private final UserRepository userRepo;
    private final CanaryRepository canaryRepo;
    private final NotificationRepository notifRepo;
    private final HoneypotService honeypotService;

    public HoneypotController(UserRepository userRepo, CanaryRepository canaryRepo,
                              NotificationRepository notifRepo, HoneypotService honeypotService) {
        this.userRepo = userRepo;
        this.canaryRepo = canaryRepo;
        this.notifRepo = notifRepo;
        this.honeypotService = honeypotService;
    }

    private User getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElse(null);
    }

    @ModelAttribute
    public void addCommon(Model model, @AuthenticationPrincipal UserDetails ud) {
        if (ud != null) {
            User user = getUser(ud);
            model.addAttribute("currentUser", user);
            model.addAttribute("unreadNotifications",
                    notifRepo.countByUserIdAndIsReadFalse(user.getId()));
        }
    }

    @GetMapping("")
    public String canaries(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = getUser(ud);
        model.addAttribute("canaries", user.getCanaries());
        return "honeypot";
    }

    @PostMapping("")
    public String createCanary(@RequestParam String label,
                               @RequestParam String decoyEmail,
                               @RequestParam String decoyPassword,
                               @RequestParam(defaultValue = "") String decoyNote,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes flash) {
        User user = getUser(ud);
        if (label.isBlank() || decoyEmail.isBlank() || decoyPassword.isBlank()) {
            flash.addFlashAttribute("error", "Label, decoy email and decoy password are required.");
            return "redirect:/honeypot";
        }
        Canary c = new Canary(user, label.trim(), decoyEmail.trim().toLowerCase(),
                decoyPassword.trim(), decoyNote.trim(), honeypotService.newTrapToken());
        canaryRepo.save(c);
        flash.addFlashAttribute("success",
                "Canary created. Deploy the decoy credential and watch for hits!");
        return "redirect:/honeypot";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails ud,
                         Model model) {
        User user = getUser(ud);
        Canary c = canaryRepo.findById(id).orElse(null);
        if (c == null || !c.getUser().getId().equals(user.getId())) {
            return "redirect:/honeypot";
        }
        model.addAttribute("canary", c);
        model.addAttribute("events", c.getEvents());
        model.addAttribute("trapUrl", "/honeypot/t/" + c.getTrapToken());
        return "canary_detail";
    }

    @PostMapping("/{id}/simulate")
    public String simulate(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails ud,
                           RedirectAttributes flash) {
        User user = getUser(ud);
        Canary c = canaryRepo.findById(id).orElse(null);
        if (c == null || !c.getUser().getId().equals(user.getId())) {
            return "redirect:/honeypot";
        }
        HoneypotEvent event = honeypotService.simulateAttack(c);
        flash.addFlashAttribute("warning",
                "Simulated attack captured from " + event.getCity() + ", " + event.getCountry() + ".");
        return "redirect:/honeypot/" + c.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails ud,
                         RedirectAttributes flash) {
        User user = getUser(ud);
        Canary c = canaryRepo.findById(id).orElse(null);
        if (c != null && c.getUser().getId().equals(user.getId())) {
            canaryRepo.delete(c);
            flash.addFlashAttribute("info", "Canary deleted.");
        }
        return "redirect:/honeypot";
    }

    // ── Public trap page (what attackers see) ──

    @GetMapping("/t/{token}")
    public String trap(@PathVariable String token, Model model) {
        model.addAttribute("failed", false);
        return "trap";
    }

    @PostMapping("/t/{token}")
    public String trapPost(@PathVariable String token,
                           @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                           @RequestHeader(value = "User-Agent", defaultValue = "") String ua,
                           jakarta.servlet.http.HttpServletRequest request,
                           Model model) {
        canaryRepo.findByTrapToken(token).ifPresent(canary -> {
            String ip = xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
            honeypotService.recordAttack(canary, ip, ua, null);
        });
        model.addAttribute("failed", true);
        return "trap";
    }
}
