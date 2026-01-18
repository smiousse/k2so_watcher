package com.k2so.watcher.model;

import jakarta.persistence.*;

@Entity
@Table(name = "device_service_urls")
public class DeviceServiceUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "alias", nullable = false)
    private String alias;

    @Column(name = "url", nullable = false)
    private String url;

    public DeviceServiceUrl() {
    }

    public DeviceServiceUrl(Device device, String alias, String url) {
        this.device = device;
        this.alias = alias;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the full service URL, resolving relative URLs against the device's IP.
     * Supports formats:
     * - Full URL: http://192.168.1.100:8080/admin
     * - Port only: :8080 or :9000/admin
     * - Path only: /admin or admin
     */
    public String getFullUrl() {
        if (url == null || url.isEmpty()) {
            return null;
        }
        // If it's already a full URL, return as-is
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        // Build URL from device IP
        if (device != null && device.getIpAddress() != null && !device.getIpAddress().isEmpty()) {
            // Handle port-only format like :8080 or :9000/path
            if (url.startsWith(":")) {
                return "http://" + device.getIpAddress() + url;
            }
            // Handle path-only format
            String path = url.startsWith("/") ? url : "/" + url;
            return "http://" + device.getIpAddress() + path;
        }
        return url;
    }
}
