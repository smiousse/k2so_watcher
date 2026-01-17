package com.k2so.watcher.controller;

import com.k2so.watcher.model.AppSettings;
import com.k2so.watcher.model.User;
import com.k2so.watcher.service.AppSettingsService;
import com.k2so.watcher.service.BackupService;
import com.k2so.watcher.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AppSettingsService appSettingsService;
    private final BackupService backupService;

    public AdminController(UserService userService, AppSettingsService appSettingsService, BackupService backupService) {
        this.userService = userService;
        this.appSettingsService = appSettingsService;
        this.backupService = backupService;
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            @RequestParam("role") String role,
                            RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(username, password, role);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                            @RequestParam("username") String username,
                            @RequestParam("role") String role,
                            @RequestParam(value = "enabled", defaultValue = "false") boolean enabled,
                            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, username, role, enabled);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/password")
    public String changePassword(@PathVariable Long id,
                                @RequestParam("newPassword") String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing password: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-totp")
    public String resetTotp(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.disableTotp(id);
            redirectAttributes.addFlashAttribute("success", "2FA reset successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error resetting 2FA: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        List<AppSettings> settings = appSettingsService.getAllSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("backups", backupService.listBackups());
        model.addAttribute("javaVersion", System.getProperty("java.version"));
        model.addAttribute("osInfo", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        return "admin/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam("key") String key,
                                @RequestParam("value") String value,
                                RedirectAttributes redirectAttributes) {
        try {
            appSettingsService.updateSetting(key, value);
            redirectAttributes.addFlashAttribute("success", "Setting updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating setting: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    // Backup endpoints

    @PostMapping("/backup/create")
    public ResponseEntity<byte[]> createBackup() {
        try {
            byte[] backupData = backupService.createBackupForDownload();
            String filename = "k2so_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".zip";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(backupData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(backupData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/backup/download/{filename}")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable String filename) {
        try {
            byte[] backupData = backupService.downloadBackup(filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(backupData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(backupData);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/backup/restore")
    public String restoreBackup(@RequestParam("backupFile") MultipartFile backupFile,
                               RedirectAttributes redirectAttributes) {
        if (backupFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a backup file to restore");
            return "redirect:/admin/settings";
        }

        if (!backupFile.getOriginalFilename().endsWith(".zip")) {
            redirectAttributes.addFlashAttribute("error", "Invalid backup file format. Please upload a .zip file");
            return "redirect:/admin/settings";
        }

        try {
            backupService.restoreFromBackup(backupFile.getInputStream());
            redirectAttributes.addFlashAttribute("success", "Database restored successfully. Please log in again.");
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error restoring backup: " + e.getMessage());
            return "redirect:/admin/settings";
        }
    }

    @PostMapping("/backup/delete/{filename}")
    public String deleteBackup(@PathVariable String filename, RedirectAttributes redirectAttributes) {
        try {
            if (backupService.deleteBackup(filename)) {
                redirectAttributes.addFlashAttribute("success", "Backup deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error deleting backup");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting backup: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
