package com.k2so.watcher.controller;

import com.k2so.watcher.model.Device;
import com.k2so.watcher.model.NetworkScan;
import com.k2so.watcher.service.DeviceService;
import com.k2so.watcher.service.NetworkScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final DeviceService deviceService;
    private final NetworkScannerService networkScannerService;

    public ApiController(DeviceService deviceService, NetworkScannerService networkScannerService) {
        this.deviceService = deviceService;
        this.networkScannerService = networkScannerService;
    }

    @GetMapping("/devices")
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/devices/online")
    public ResponseEntity<List<Device>> getOnlineDevices() {
        return ResponseEntity.ok(deviceService.getOnlineDevices());
    }

    @GetMapping("/devices/unknown")
    public ResponseEntity<List<Device>> getUnknownDevices() {
        return ResponseEntity.ok(deviceService.getUnknownDevices());
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<Device> getDevice(@PathVariable Long id) {
        return deviceService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDevices", deviceService.countTotalDevices());
        stats.put("onlineDevices", deviceService.countOnlineDevices());
        stats.put("unknownDevices", deviceService.countUnknownDevices());
        stats.put("scanInProgress", networkScannerService.isScanInProgress());

        networkScannerService.getLatestScan().ifPresent(scan -> {
            stats.put("lastScanTime", scan.getStartedAt());
            stats.put("lastScanStatus", scan.getStatus());
        });

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/scan/start")
    public ResponseEntity<Map<String, Object>> startScan() {
        Map<String, Object> response = new HashMap<>();

        if (networkScannerService.isScanInProgress()) {
            response.put("success", false);
            response.put("message", "A scan is already in progress");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            NetworkScan scan = networkScannerService.startScan("MANUAL");
            response.put("success", true);
            response.put("message", "Scan started");
            response.put("scanId", scan.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error starting scan: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/scan/status")
    public ResponseEntity<Map<String, Object>> getScanStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("scanInProgress", networkScannerService.isScanInProgress());

        networkScannerService.getLatestScan().ifPresent(scan -> {
            status.put("lastScanId", scan.getId());
            status.put("lastScanStatus", scan.getStatus());
            status.put("lastScanTime", scan.getStartedAt());
            status.put("devicesFound", scan.getDevicesFound());
            status.put("newDevices", scan.getNewDevices());
        });

        return ResponseEntity.ok(status);
    }

    @GetMapping("/scan/{id}")
    public ResponseEntity<NetworkScan> getScan(@PathVariable Long id) {
        return networkScannerService.getScanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/scans/recent")
    public ResponseEntity<List<NetworkScan>> getRecentScans(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(networkScannerService.getRecentScans(limit));
    }
}
