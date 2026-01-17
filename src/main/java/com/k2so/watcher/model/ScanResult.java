package com.k2so.watcher.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_results")
public class ScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_scan_id")
    private NetworkScan networkScan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "is_new_device")
    private boolean newDevice = false;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NetworkScan getNetworkScan() {
        return networkScan;
    }

    public void setNetworkScan(NetworkScan networkScan) {
        this.networkScan = networkScan;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public boolean isNewDevice() {
        return newDevice;
    }

    public void setNewDevice(boolean newDevice) {
        this.newDevice = newDevice;
    }

    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}
