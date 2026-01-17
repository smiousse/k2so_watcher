package com.k2so.watcher.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "network_scans")
public class NetworkScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scan_type")
    private String scanType; // MANUAL, SCHEDULED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status")
    private String status; // RUNNING, COMPLETED, FAILED

    @Column(name = "network_range")
    private String networkRange;

    @Column(name = "devices_found")
    private int devicesFound = 0;

    @Column(name = "new_devices")
    private int newDevices = 0;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "scan_log", columnDefinition = "TEXT")
    private String scanLog;

    @Column(name = "scanner_tool")
    private String scannerTool;

    @OneToMany(mappedBy = "networkScan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScanResult> results = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        status = "RUNNING";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNetworkRange() {
        return networkRange;
    }

    public void setNetworkRange(String networkRange) {
        this.networkRange = networkRange;
    }

    public int getDevicesFound() {
        return devicesFound;
    }

    public void setDevicesFound(int devicesFound) {
        this.devicesFound = devicesFound;
    }

    public int getNewDevices() {
        return newDevices;
    }

    public void setNewDevices(int newDevices) {
        this.newDevices = newDevices;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<ScanResult> getResults() {
        return results;
    }

    public void setResults(List<ScanResult> results) {
        this.results = results;
    }

    public String getScanLog() {
        return scanLog;
    }

    public void setScanLog(String scanLog) {
        this.scanLog = scanLog;
    }

    public String getScannerTool() {
        return scannerTool;
    }

    public void setScannerTool(String scannerTool) {
        this.scannerTool = scannerTool;
    }

    public long getDurationSeconds() {
        if (startedAt == null || completedAt == null) {
            return 0;
        }
        return java.time.Duration.between(startedAt, completedAt).getSeconds();
    }
}
