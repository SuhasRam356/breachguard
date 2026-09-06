package com.breachguard.controller;

import com.breachguard.dto.PasswordResult;
import com.breachguard.service.PasswordCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final PasswordCheckService passwordCheckService;

    @Autowired
    public ApiController(PasswordCheckService passwordCheckService) {
        this.passwordCheckService = passwordCheckService;
    }

    @GetMapping("/check-password")
    public ResponseEntity<?> checkPassword(@RequestParam String prefix, @RequestParam String hash) {
        try {
            PasswordResult result = passwordCheckService.checkPasswordHash(prefix, hash);
            return ResponseEntity.ok(Map.of(
                    "isBreached", result.isPwned(),
                    "count", result.getTimesSeen()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
