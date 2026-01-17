package com.k2so.watcher.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mac_address", unique = true, nullable = false)
    private String macAddress;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "custom_name")
    private String customName;

    @Column(name = "vendor")
    private String vendor;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private DeviceType deviceType = DeviceType.UNKNOWN;

    @Column(name = "connection_type")
    private String connectionType; // WIRED, WIFI

    @Column(name = "is_known")
    private boolean known = false;

    @Column(name = "is_trusted")
    private boolean trusted = false;

    @Column(name = "is_online")
    private boolean online = false;

    @Column(name = "first_seen")
    private LocalDateTime firstSeen;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "open_ports", length = 2000)
    private String openPorts;

    @Column(name = "detected_os")
    private String detectedOs;

    @Column(name = "deep_scan_log", columnDefinition = "TEXT")
    private String deepScanLog;

    @Column(name = "last_deep_scan")
    private LocalDateTime lastDeepScan;

    @Column(name = "ai_identification")
    private String aiIdentification;

    @Column(name = "service_url")
    private String serviceUrl;

    @Column(name = "is_pinned")
    private boolean pinned = false;

    @PrePersist
    protected void onCreate() {
        firstSeen = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public LocalDateTime getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(LocalDateTime firstSeen) {
        this.firstSeen = firstSeen;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOpenPorts() {
        return openPorts;
    }

    public void setOpenPorts(String openPorts) {
        this.openPorts = openPorts;
    }

    public String getDetectedOs() {
        return detectedOs;
    }

    public void setDetectedOs(String detectedOs) {
        this.detectedOs = detectedOs;
    }

    public String getDeepScanLog() {
        return deepScanLog;
    }

    public void setDeepScanLog(String deepScanLog) {
        this.deepScanLog = deepScanLog;
    }

    public LocalDateTime getLastDeepScan() {
        return lastDeepScan;
    }

    public void setLastDeepScan(LocalDateTime lastDeepScan) {
        this.lastDeepScan = lastDeepScan;
    }

    public String getAiIdentification() {
        return aiIdentification;
    }

    public void setAiIdentification(String aiIdentification) {
        this.aiIdentification = aiIdentification;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    /**
     * Returns the full service URL, resolving relative URLs against the device's IP
     * Supports formats:
     * - Full URL: http://192.168.1.100:8080/admin
     * - Port only: :8080 or :9000/admin
     * - Path only: /admin or admin
     */
    public String getFullServiceUrl() {
        if (serviceUrl == null || serviceUrl.isEmpty()) {
            return null;
        }
        // If it's already a full URL, return as-is
        if (serviceUrl.startsWith("http://") || serviceUrl.startsWith("https://")) {
            return serviceUrl;
        }
        // Build URL from device IP
        if (ipAddress != null && !ipAddress.isEmpty()) {
            // Handle port-only format like :8080 or :9000/path
            if (serviceUrl.startsWith(":")) {
                return "http://" + ipAddress + serviceUrl;
            }
            // Handle path-only format
            String path = serviceUrl.startsWith("/") ? serviceUrl : "/" + serviceUrl;
            return "http://" + ipAddress + path;
        }
        return serviceUrl;
    }

    public String getDisplayName() {
        if (customName != null && !customName.isEmpty()) {
            return customName;
        }
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }
        return macAddress;
    }
}
