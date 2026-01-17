package com.k2so.watcher.service;

import com.k2so.watcher.model.Device;
import com.k2so.watcher.model.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DeviceIdentificationService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceIdentificationService.class);

    // Vendor to likely device type mappings
    private static final Map<String, DeviceType> VENDOR_TYPE_MAP = new HashMap<>();

    static {
        // Routers and networking
        VENDOR_TYPE_MAP.put("Cisco", DeviceType.ROUTER);
        VENDOR_TYPE_MAP.put("Netgear", DeviceType.ROUTER);
        VENDOR_TYPE_MAP.put("TP-Link", DeviceType.ROUTER);
        VENDOR_TYPE_MAP.put("Linksys", DeviceType.ROUTER);
        VENDOR_TYPE_MAP.put("ASUS", DeviceType.ROUTER);

        // Gaming consoles
        VENDOR_TYPE_MAP.put("Nintendo", DeviceType.GAMING_CONSOLE);
        VENDOR_TYPE_MAP.put("Sony", DeviceType.GAMING_CONSOLE);
        VENDOR_TYPE_MAP.put("Microsoft", DeviceType.GAMING_CONSOLE);

        // Smart home / IoT
        VENDOR_TYPE_MAP.put("Amazon", DeviceType.SMART_HOME);
        VENDOR_TYPE_MAP.put("Google", DeviceType.SMART_HOME);

        // Computers
        VENDOR_TYPE_MAP.put("Dell", DeviceType.COMPUTER);
        VENDOR_TYPE_MAP.put("HP", DeviceType.COMPUTER);
        VENDOR_TYPE_MAP.put("Intel", DeviceType.COMPUTER);

        // Mobile devices
        VENDOR_TYPE_MAP.put("Apple", DeviceType.SMARTPHONE);
        VENDOR_TYPE_MAP.put("Samsung", DeviceType.SMARTPHONE);

        // IoT / Embedded
        VENDOR_TYPE_MAP.put("Raspberry Pi", DeviceType.SERVER);
    }

    public DeviceType identifyDeviceType(Device device) {
        String vendor = device.getVendor();
        String hostname = device.getHostname();

        // First, try to identify by vendor
        if (vendor != null && !vendor.equals("Unknown")) {
            DeviceType vendorType = VENDOR_TYPE_MAP.get(vendor);
            if (vendorType != null) {
                return vendorType;
            }
        }

        // Try to identify by hostname patterns
        if (hostname != null && !hostname.isEmpty()) {
            String lowerHostname = hostname.toLowerCase();

            // Smartphones
            if (lowerHostname.contains("iphone") || lowerHostname.contains("android") ||
                lowerHostname.contains("galaxy") || lowerHostname.contains("pixel")) {
                return DeviceType.SMARTPHONE;
            }

            // Tablets
            if (lowerHostname.contains("ipad") || lowerHostname.contains("tablet")) {
                return DeviceType.TABLET;
            }

            // Laptops
            if (lowerHostname.contains("macbook") || lowerHostname.contains("laptop") ||
                lowerHostname.contains("notebook")) {
                return DeviceType.LAPTOP;
            }

            // Desktops
            if (lowerHostname.contains("desktop") || lowerHostname.contains("pc") ||
                lowerHostname.contains("imac") || lowerHostname.contains("mac-mini")) {
                return DeviceType.COMPUTER;
            }

            // Printers
            if (lowerHostname.contains("printer") || lowerHostname.contains("epson") ||
                lowerHostname.contains("canon") || lowerHostname.contains("hp-") ||
                lowerHostname.contains("brother")) {
                return DeviceType.PRINTER;
            }

            // Smart TVs
            if (lowerHostname.contains("-tv") || lowerHostname.contains("smarttv") ||
                lowerHostname.contains("roku") || lowerHostname.contains("firetv") ||
                lowerHostname.contains("chromecast") || lowerHostname.contains("appletv")) {
                return DeviceType.SMART_TV;
            }

            // Gaming consoles
            if (lowerHostname.contains("playstation") || lowerHostname.contains("xbox") ||
                lowerHostname.contains("nintendo") || lowerHostname.contains("switch")) {
                return DeviceType.GAMING_CONSOLE;
            }

            // NAS devices
            if (lowerHostname.contains("nas") || lowerHostname.contains("synology") ||
                lowerHostname.contains("qnap") || lowerHostname.contains("drobo")) {
                return DeviceType.NAS;
            }

            // Cameras
            if (lowerHostname.contains("camera") || lowerHostname.contains("cam-") ||
                lowerHostname.contains("ipcam") || lowerHostname.contains("nest")) {
                return DeviceType.CAMERA;
            }

            // Smart home
            if (lowerHostname.contains("echo") || lowerHostname.contains("alexa") ||
                lowerHostname.contains("home-mini") || lowerHostname.contains("homepod") ||
                lowerHostname.contains("hue") || lowerHostname.contains("smartthings")) {
                return DeviceType.SMART_HOME;
            }

            // Servers
            if (lowerHostname.contains("server") || lowerHostname.contains("srv") ||
                lowerHostname.contains("pi") || lowerHostname.contains("raspberry")) {
                return DeviceType.SERVER;
            }

            // Routers / Access Points
            if (lowerHostname.contains("router") || lowerHostname.contains("gateway") ||
                lowerHostname.contains("ap-") || lowerHostname.contains("accesspoint")) {
                return DeviceType.ROUTER;
            }
        }

        return DeviceType.UNKNOWN;
    }

    public String suggestConnectionType(String interfaceName) {
        if (interfaceName == null) {
            return "UNKNOWN";
        }

        String lower = interfaceName.toLowerCase();

        // Wireless indicators
        if (lower.contains("wlan") || lower.contains("wifi") ||
            lower.contains("wl") || lower.contains("ath") ||
            lower.contains("wireless")) {
            return "WIFI";
        }

        // Wired indicators
        if (lower.contains("eth") || lower.contains("en") ||
            lower.contains("lan")) {
            return "WIRED";
        }

        return "UNKNOWN";
    }
}
