package com.k2so.watcher.controller;

import com.k2so.watcher.model.Device;
import com.k2so.watcher.model.NetworkScan;
import com.k2so.watcher.service.DeviceService;
import com.k2so.watcher.service.NetworkScannerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final DeviceService deviceService;
    private final NetworkScannerService networkScannerService;

    public DashboardController(DeviceService deviceService, NetworkScannerService networkScannerService) {
        this.deviceService = deviceService;
        this.networkScannerService = networkScannerService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, HttpSession session, Model model) {
        // Check if TOTP verification is pending
        Boolean totpPending = (Boolean) session.getAttribute("TOTP_PENDING");
        if (totpPending != null && totpPending) {
            return "redirect:/totp-verify";
        }

        Boolean totpSetupRequired = (Boolean) session.getAttribute("TOTP_SETUP_REQUIRED");
        if (totpSetupRequired != null && totpSetupRequired) {
            return "redirect:/totp-setup";
        }

        // Dashboard stats
        long totalDevices = deviceService.countTotalDevices();
        long onlineDevices = deviceService.countOnlineDevices();
        long unknownDevices = deviceService.countUnknownDevices();

        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("onlineDevices", onlineDevices);
        model.addAttribute("unknownDevices", unknownDevices);

        // Recent devices
        List<Device> recentDevices = deviceService.getAllDevices();
        if (recentDevices.size() > 10) {
            recentDevices = recentDevices.subList(0, 10);
        }
        model.addAttribute("recentDevices", recentDevices);

        // Online devices for display
        List<Device> onlineDeviceList = deviceService.getOnlineDevices();
        model.addAttribute("onlineDeviceList", onlineDeviceList);

        // Unknown/potential intruders
        List<Device> unknownDeviceList = deviceService.getUnknownDevices();
        model.addAttribute("unknownDeviceList", unknownDeviceList);

        // Pinned devices for quick access
        List<Device> pinnedDevices = deviceService.getPinnedDevices();
        model.addAttribute("pinnedDevices", pinnedDevices);

        // Last scan info
        Optional<NetworkScan> lastScan = networkScannerService.getLatestScan();
        lastScan.ifPresent(scan -> model.addAttribute("lastScan", scan));

        // Scan status
        model.addAttribute("scanInProgress", networkScannerService.isScanInProgress());

        // User info
        model.addAttribute("username", authentication.getName());

        return "dashboard";
    }
}
