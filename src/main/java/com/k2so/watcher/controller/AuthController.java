package com.k2so.watcher.controller;

import com.k2so.watcher.model.User;
import com.k2so.watcher.service.TotpService;
import com.k2so.watcher.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final TotpService totpService;

    public AuthController(UserService userService, TotpService totpService) {
        this.userService = userService;
        this.totpService = totpService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "expired", required = false) String expired,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        if (expired != null) {
            model.addAttribute("error", "Your session has expired. Please log in again");
        }
        return "auth/login";
    }

    @GetMapping("/totp-setup")
    public String totpSetup(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        // Generate new TOTP secret if not already set
        String secret = user.getTotpSecret();
        if (secret == null || secret.isEmpty()) {
            secret = totpService.generateSecret();
            userService.updateTotpSecret(username, secret);
        }

        String qrCodeDataUri = totpService.generateQrCodeDataUri(secret, username);
        String manualKey = totpService.getManualEntryKey(secret);

        model.addAttribute("qrCode", qrCodeDataUri);
        model.addAttribute("manualKey", manualKey);
        model.addAttribute("username", username);

        return "auth/totp-setup";
    }

    @PostMapping("/totp-setup")
    public String totpSetupVerify(@RequestParam("code") String code,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user == null || user.getTotpSecret() == null) {
            return "redirect:/login";
        }

        if (totpService.verifyCode(user.getTotpSecret(), code)) {
            userService.enableTotp(username);
            session.removeAttribute("TOTP_SETUP_REQUIRED");
            userService.recordSuccessfulLogin(username);
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid verification code. Please try again.");
            return "redirect:/totp-setup";
        }
    }

    @GetMapping("/totp-verify")
    public String totpVerify(HttpSession session, Model model) {
        Boolean totpPending = (Boolean) session.getAttribute("TOTP_PENDING");
        if (totpPending == null || !totpPending) {
            return "redirect:/login";
        }

        String username = (String) session.getAttribute("TOTP_USERNAME");
        model.addAttribute("username", username);

        return "auth/totp-verify";
    }

    @PostMapping("/totp-verify")
    public String totpVerifyCode(@RequestParam("code") String code,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Boolean totpPending = (Boolean) session.getAttribute("TOTP_PENDING");
        String username = (String) session.getAttribute("TOTP_USERNAME");

        if (totpPending == null || !totpPending || username == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(username).orElse(null);
        if (user == null || user.getTotpSecret() == null) {
            return "redirect:/login";
        }

        if (totpService.verifyCode(user.getTotpSecret(), code)) {
            session.removeAttribute("TOTP_PENDING");
            session.removeAttribute("TOTP_USERNAME");
            userService.recordSuccessfulLogin(username);
            return "redirect:/dashboard";
        } else {
            userService.recordFailedLogin(username);
            redirectAttributes.addFlashAttribute("error", "Invalid verification code");
            return "redirect:/totp-verify";
        }
    }
}
