package com.k2so.watcher.controller;

import com.k2so.watcher.dto.DeviceAnalysisResult;
import com.k2so.watcher.model.Device;
import com.k2so.watcher.model.DeviceType;
import com.k2so.watcher.service.AIIdentificationService;
import com.k2so.watcher.service.DeviceService;
import com.k2so.watcher.service.LangChain4jService;
import com.k2so.watcher.service.NetworkScannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final AIIdentificationService aiIdentificationService;
    private final NetworkScannerService networkScannerService;
    private final LangChain4jService langChain4jService;

    public DeviceController(DeviceService deviceService,
                           AIIdentificationService aiIdentificationService,
                           NetworkScannerService networkScannerService,
                           LangChain4jService langChain4jService) {
        this.deviceService = deviceService;
        this.aiIdentificationService = aiIdentificationService;
        this.networkScannerService = networkScannerService;
        this.langChain4jService = langChain4jService;
    }

    @GetMapping
    public String listDevices(@RequestParam(value = "filter", required = false) String filter, Model model) {
        List<Device> devices;

        if ("online".equals(filter)) {
            devices = deviceService.getOnlineDevices();
            model.addAttribute("currentFilter", "online");
        } else if ("unknown".equals(filter)) {
            devices = deviceService.getUnknownDevices();
            model.addAttribute("currentFilter", "unknown");
        } else {
            devices = deviceService.getAllDevices();
            model.addAttribute("currentFilter", "all");
        }

        model.addAttribute("devices", devices);
        model.addAttribute("deviceTypes", DeviceType.values());

        return "devices";
    }

    @GetMapping("/{id}")
    public String deviceDetail(@PathVariable Long id, Model model) {
        Device device = deviceService.getDeviceById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        model.addAttribute("device", device);
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("aiEnabled", aiIdentificationService.isEnabled());
        model.addAttribute("langChain4jConfigured", langChain4jService.isConfigured());

        return "device-detail";
    }

    @PostMapping("/{id}/update")
    public String updateDevice(@PathVariable Long id,
                               @RequestParam("customName") String customName,
                               @RequestParam("deviceType") DeviceType deviceType,
                               @RequestParam(value = "known", defaultValue = "false") boolean known,
                               @RequestParam(value = "trusted", defaultValue = "false") boolean trusted,
                               @RequestParam(value = "notes", required = false) String notes,
                               @RequestParam(value = "serviceUrl", required = false) String serviceUrl,
                               @RequestParam(value = "pinned", defaultValue = "false") boolean pinned,
                               RedirectAttributes redirectAttributes) {
        try {
            deviceService.updateDevice(id, customName, deviceType, known, trusted, notes, serviceUrl, pinned);
            redirectAttributes.addFlashAttribute("success", "Device updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating device: " + e.getMessage());
        }

        return "redirect:/devices/" + id;
    }

    @PostMapping("/{id}/toggle-pin")
    public String togglePin(@PathVariable Long id,
                           @RequestParam(value = "returnTo", defaultValue = "devices") String returnTo,
                           RedirectAttributes redirectAttributes) {
        try {
            deviceService.togglePin(id);
            redirectAttributes.addFlashAttribute("success", "Device pin status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        if ("dashboard".equals(returnTo)) {
            return "redirect:/dashboard";
        }
        return "redirect:/devices";
    }

    @PostMapping("/{id}/mark-known")
    public String markAsKnown(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            deviceService.markAsKnown(id);
            redirectAttributes.addFlashAttribute("success", "Device marked as known");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/devices";
    }

    @PostMapping("/{id}/mark-trusted")
    public String markAsTrusted(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            deviceService.markAsTrusted(id);
            redirectAttributes.addFlashAttribute("success", "Device marked as trusted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/devices";
    }

    @PostMapping("/{id}/identify-ai")
    public String identifyWithAI(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String result = deviceService.identifyWithAI(id);
            if (result != null) {
                redirectAttributes.addFlashAttribute("success", "AI identification complete");
            } else {
                redirectAttributes.addFlashAttribute("error", "AI identification failed or is not configured");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/devices/" + id;
    }

    @PostMapping("/{id}/deep-scan")
    public String deepScan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Device device = deviceService.getDeviceById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found"));

            if (device.getIpAddress() == null || device.getIpAddress().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cannot perform deep scan: device has no IP address");
                return "redirect:/devices/" + id;
            }

            // Start deep scan asynchronously
            networkScannerService.performDeepScanAsync(id);
            redirectAttributes.addFlashAttribute("success", "Deep scan started. This may take a few minutes. Refresh the page to see results.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error starting deep scan: " + e.getMessage());
        }

        return "redirect:/devices/" + id;
    }

    @PostMapping("/{id}/analyze-deepscan")
    public String analyzeDeepScan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Device device = deviceService.getDeviceById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found"));

            if (device.getDeepScanLog() == null || device.getDeepScanLog().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cannot analyze: device has no deep scan log. Run a deep scan first.");
                return "redirect:/devices/" + id;
            }

            DeviceAnalysisResult result = langChain4jService.analyzeDeepScanLog(device);

            // Update device with AI analysis results
            if (result.getDeviceType() != null) {
                device.setDeviceType(result.getDeviceType());
            }
            if (result.getSuggestedName() != null && !result.getSuggestedName().isEmpty()) {
                device.setCustomName(result.getSuggestedName());
            }
            if (result.getNotes() != null && !result.getNotes().isEmpty()) {
                device.setNotes(result.getNotes());
            }

            deviceService.saveDevice(device);
            redirectAttributes.addFlashAttribute("success", "AI analysis complete. Device information updated.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "AI not configured: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error analyzing device: " + e.getMessage());
        }

        return "redirect:/devices/" + id;
    }

    // Service URL management endpoints

    @PostMapping("/{id}/services/add")
    public String addServiceUrl(@PathVariable Long id,
                               @RequestParam("alias") String alias,
                               @RequestParam("url") String url,
                               RedirectAttributes redirectAttributes) {
        try {
            if (alias == null || alias.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Service alias is required");
                return "redirect:/devices/" + id;
            }
            if (url == null || url.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Service URL is required");
                return "redirect:/devices/" + id;
            }

            deviceService.addServiceUrl(id, alias.trim(), url.trim());
            redirectAttributes.addFlashAttribute("success", "Service URL added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding service URL: " + e.getMessage());
        }

        return "redirect:/devices/" + id;
    }

    @PostMapping("/{id}/services/{serviceId}/update")
    public String updateServiceUrl(@PathVariable Long id,
                                  @PathVariable Long serviceId,
                                  @RequestParam("alias") String alias,
                                  @RequestParam("url") String url,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (alias == null || alias.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Service alias is required");
                return "redirect:/devices/" + id;
            }
            if (url == null || url.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Service URL is required");
                return "redirect:/devices/" + id;
            }

            deviceService.updateServiceUrlEntry(serviceId, alias.trim(), url.trim());
            redirectAttributes.addFlashAttribute("success", "Service URL updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating service URL: " + e.getMessage());
        }

        return "redirect:/devices/" + id;
    }

    @PostMapping("/{id}/services/{serviceId}/delete")
    public String deleteServiceUrl(@PathVariable Long id,
                                  @PathVariable Long serviceId,
                                  RedirectAttributes redirectAttributes) {
        try {
            deviceService.deleteServiceUrl(serviceId);
            redirectAttributes.addFlashAttribute("success", "Service URL deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting service URL: " + e.getMessage());
        }

        return "redirect:/devices/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteDevice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            deviceService.deleteDevice(id);
            redirectAttributes.addFlashAttribute("success", "Device deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting device: " + e.getMessage());
        }

        return "redirect:/devices";
    }
}
