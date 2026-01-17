package com.k2so.watcher.controller;

import com.k2so.watcher.model.NetworkScan;
import com.k2so.watcher.model.ScanResult;
import com.k2so.watcher.service.NetworkScannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/scan")
public class ScanController {

    private final NetworkScannerService networkScannerService;

    public ScanController(NetworkScannerService networkScannerService) {
        this.networkScannerService = networkScannerService;
    }

    @GetMapping
    public String scanPage(Model model) {
        List<NetworkScan> recentScans = networkScannerService.getRecentScans(20);
        model.addAttribute("recentScans", recentScans);
        model.addAttribute("scanInProgress", networkScannerService.isScanInProgress());

        return "scan-results";
    }

    @PostMapping("/start")
    public String startScan(RedirectAttributes redirectAttributes) {
        try {
            if (networkScannerService.isScanInProgress()) {
                redirectAttributes.addFlashAttribute("error", "A scan is already in progress");
            } else {
                networkScannerService.startScan("MANUAL");
                redirectAttributes.addFlashAttribute("success", "Network scan started");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error starting scan: " + e.getMessage());
        }

        return "redirect:/scan";
    }

    @GetMapping("/{id}")
    public String scanDetail(@PathVariable Long id, Model model) {
        NetworkScan scan = networkScannerService.getScanById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scan not found"));

        List<ScanResult> results = networkScannerService.getScanResults(id);

        model.addAttribute("scan", scan);
        model.addAttribute("results", results);

        return "scan-detail";
    }
}
