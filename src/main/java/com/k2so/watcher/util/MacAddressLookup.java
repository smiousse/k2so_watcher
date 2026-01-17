package com.k2so.watcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MacAddressLookup {

    private static final Logger logger = LoggerFactory.getLogger(MacAddressLookup.class);

    // Common MAC address prefixes (OUI) to vendor mappings
    private static final Map<String, String> OUI_DATABASE = new HashMap<>();

    static {
        // Apple
        OUI_DATABASE.put("00:03:93", "Apple");
        OUI_DATABASE.put("00:0A:27", "Apple");
        OUI_DATABASE.put("00:0A:95", "Apple");
        OUI_DATABASE.put("00:0D:93", "Apple");
        OUI_DATABASE.put("00:11:24", "Apple");
        OUI_DATABASE.put("00:14:51", "Apple");
        OUI_DATABASE.put("00:16:CB", "Apple");
        OUI_DATABASE.put("00:17:F2", "Apple");
        OUI_DATABASE.put("00:19:E3", "Apple");
        OUI_DATABASE.put("00:1B:63", "Apple");
        OUI_DATABASE.put("00:1C:B3", "Apple");
        OUI_DATABASE.put("00:1D:4F", "Apple");
        OUI_DATABASE.put("00:1E:52", "Apple");
        OUI_DATABASE.put("00:1E:C2", "Apple");
        OUI_DATABASE.put("00:1F:5B", "Apple");
        OUI_DATABASE.put("00:1F:F3", "Apple");
        OUI_DATABASE.put("00:21:E9", "Apple");
        OUI_DATABASE.put("00:22:41", "Apple");
        OUI_DATABASE.put("00:23:12", "Apple");
        OUI_DATABASE.put("00:23:32", "Apple");
        OUI_DATABASE.put("00:23:6C", "Apple");
        OUI_DATABASE.put("00:23:DF", "Apple");
        OUI_DATABASE.put("00:24:36", "Apple");
        OUI_DATABASE.put("00:25:00", "Apple");
        OUI_DATABASE.put("00:25:4B", "Apple");
        OUI_DATABASE.put("00:25:BC", "Apple");
        OUI_DATABASE.put("00:26:08", "Apple");
        OUI_DATABASE.put("00:26:4A", "Apple");
        OUI_DATABASE.put("00:26:B0", "Apple");
        OUI_DATABASE.put("00:26:BB", "Apple");

        // Samsung
        OUI_DATABASE.put("00:00:F0", "Samsung");
        OUI_DATABASE.put("00:02:78", "Samsung");
        OUI_DATABASE.put("00:07:AB", "Samsung");
        OUI_DATABASE.put("00:09:18", "Samsung");
        OUI_DATABASE.put("00:0D:AE", "Samsung");
        OUI_DATABASE.put("00:0D:E5", "Samsung");
        OUI_DATABASE.put("00:12:47", "Samsung");
        OUI_DATABASE.put("00:12:FB", "Samsung");
        OUI_DATABASE.put("00:13:77", "Samsung");
        OUI_DATABASE.put("00:15:99", "Samsung");
        OUI_DATABASE.put("00:15:B9", "Samsung");
        OUI_DATABASE.put("00:16:32", "Samsung");
        OUI_DATABASE.put("00:16:6B", "Samsung");
        OUI_DATABASE.put("00:16:6C", "Samsung");
        OUI_DATABASE.put("00:16:DB", "Samsung");
        OUI_DATABASE.put("00:17:C9", "Samsung");
        OUI_DATABASE.put("00:17:D5", "Samsung");
        OUI_DATABASE.put("00:18:AF", "Samsung");

        // Intel
        OUI_DATABASE.put("00:02:B3", "Intel");
        OUI_DATABASE.put("00:03:47", "Intel");
        OUI_DATABASE.put("00:04:23", "Intel");
        OUI_DATABASE.put("00:07:E9", "Intel");
        OUI_DATABASE.put("00:0C:F1", "Intel");
        OUI_DATABASE.put("00:0E:0C", "Intel");
        OUI_DATABASE.put("00:0E:35", "Intel");
        OUI_DATABASE.put("00:11:11", "Intel");
        OUI_DATABASE.put("00:12:F0", "Intel");
        OUI_DATABASE.put("00:13:02", "Intel");
        OUI_DATABASE.put("00:13:20", "Intel");
        OUI_DATABASE.put("00:13:CE", "Intel");
        OUI_DATABASE.put("00:13:E8", "Intel");
        OUI_DATABASE.put("00:15:00", "Intel");
        OUI_DATABASE.put("00:15:17", "Intel");
        OUI_DATABASE.put("00:16:6F", "Intel");
        OUI_DATABASE.put("00:16:76", "Intel");
        OUI_DATABASE.put("00:16:EA", "Intel");
        OUI_DATABASE.put("00:16:EB", "Intel");
        OUI_DATABASE.put("00:17:35", "Intel");

        // Cisco
        OUI_DATABASE.put("00:00:0C", "Cisco");
        OUI_DATABASE.put("00:01:42", "Cisco");
        OUI_DATABASE.put("00:01:43", "Cisco");
        OUI_DATABASE.put("00:01:63", "Cisco");
        OUI_DATABASE.put("00:01:64", "Cisco");
        OUI_DATABASE.put("00:01:96", "Cisco");
        OUI_DATABASE.put("00:01:97", "Cisco");
        OUI_DATABASE.put("00:01:C7", "Cisco");
        OUI_DATABASE.put("00:01:C9", "Cisco");
        OUI_DATABASE.put("00:02:16", "Cisco");
        OUI_DATABASE.put("00:02:17", "Cisco");
        OUI_DATABASE.put("00:02:3D", "Cisco");
        OUI_DATABASE.put("00:02:4A", "Cisco");
        OUI_DATABASE.put("00:02:4B", "Cisco");
        OUI_DATABASE.put("00:02:7D", "Cisco");
        OUI_DATABASE.put("00:02:7E", "Cisco");
        OUI_DATABASE.put("00:02:B9", "Cisco");
        OUI_DATABASE.put("00:02:BA", "Cisco");
        OUI_DATABASE.put("00:02:FC", "Cisco");
        OUI_DATABASE.put("00:02:FD", "Cisco");

        // TP-Link
        OUI_DATABASE.put("00:27:19", "TP-Link");
        OUI_DATABASE.put("10:FE:ED", "TP-Link");
        OUI_DATABASE.put("14:CC:20", "TP-Link");
        OUI_DATABASE.put("14:CF:92", "TP-Link");
        OUI_DATABASE.put("18:A6:F7", "TP-Link");
        OUI_DATABASE.put("1C:3B:F3", "TP-Link");
        OUI_DATABASE.put("20:DC:E6", "TP-Link");
        OUI_DATABASE.put("24:69:68", "TP-Link");
        OUI_DATABASE.put("30:B5:C2", "TP-Link");
        OUI_DATABASE.put("50:3E:AA", "TP-Link");
        OUI_DATABASE.put("54:C8:0F", "TP-Link");
        OUI_DATABASE.put("5C:89:9A", "TP-Link");
        OUI_DATABASE.put("60:E3:27", "TP-Link");

        // Netgear
        OUI_DATABASE.put("00:09:5B", "Netgear");
        OUI_DATABASE.put("00:0F:B5", "Netgear");
        OUI_DATABASE.put("00:14:6C", "Netgear");
        OUI_DATABASE.put("00:18:4D", "Netgear");
        OUI_DATABASE.put("00:1B:2F", "Netgear");
        OUI_DATABASE.put("00:1E:2A", "Netgear");
        OUI_DATABASE.put("00:1F:33", "Netgear");
        OUI_DATABASE.put("00:22:3F", "Netgear");
        OUI_DATABASE.put("00:24:B2", "Netgear");
        OUI_DATABASE.put("00:26:F2", "Netgear");
        OUI_DATABASE.put("08:BD:43", "Netgear");
        OUI_DATABASE.put("10:0D:7F", "Netgear");
        OUI_DATABASE.put("10:DA:43", "Netgear");

        // Dell
        OUI_DATABASE.put("00:06:5B", "Dell");
        OUI_DATABASE.put("00:08:74", "Dell");
        OUI_DATABASE.put("00:0B:DB", "Dell");
        OUI_DATABASE.put("00:0D:56", "Dell");
        OUI_DATABASE.put("00:0F:1F", "Dell");
        OUI_DATABASE.put("00:11:43", "Dell");
        OUI_DATABASE.put("00:12:3F", "Dell");
        OUI_DATABASE.put("00:13:72", "Dell");
        OUI_DATABASE.put("00:14:22", "Dell");
        OUI_DATABASE.put("00:15:C5", "Dell");
        OUI_DATABASE.put("00:18:8B", "Dell");
        OUI_DATABASE.put("00:19:B9", "Dell");
        OUI_DATABASE.put("00:1A:A0", "Dell");
        OUI_DATABASE.put("00:1C:23", "Dell");
        OUI_DATABASE.put("00:1D:09", "Dell");
        OUI_DATABASE.put("00:1E:4F", "Dell");
        OUI_DATABASE.put("00:1E:C9", "Dell");
        OUI_DATABASE.put("00:21:70", "Dell");
        OUI_DATABASE.put("00:21:9B", "Dell");
        OUI_DATABASE.put("00:22:19", "Dell");
        OUI_DATABASE.put("00:23:AE", "Dell");
        OUI_DATABASE.put("00:24:E8", "Dell");
        OUI_DATABASE.put("00:25:64", "Dell");
        OUI_DATABASE.put("00:26:B9", "Dell");

        // HP
        OUI_DATABASE.put("00:01:E6", "HP");
        OUI_DATABASE.put("00:01:E7", "HP");
        OUI_DATABASE.put("00:02:A5", "HP");
        OUI_DATABASE.put("00:04:EA", "HP");
        OUI_DATABASE.put("00:08:02", "HP");
        OUI_DATABASE.put("00:08:83", "HP");
        OUI_DATABASE.put("00:0A:57", "HP");
        OUI_DATABASE.put("00:0B:CD", "HP");
        OUI_DATABASE.put("00:0D:9D", "HP");
        OUI_DATABASE.put("00:0E:7F", "HP");
        OUI_DATABASE.put("00:0F:20", "HP");
        OUI_DATABASE.put("00:0F:61", "HP");
        OUI_DATABASE.put("00:10:83", "HP");
        OUI_DATABASE.put("00:10:E3", "HP");
        OUI_DATABASE.put("00:11:0A", "HP");
        OUI_DATABASE.put("00:11:85", "HP");
        OUI_DATABASE.put("00:12:79", "HP");
        OUI_DATABASE.put("00:13:21", "HP");
        OUI_DATABASE.put("00:14:38", "HP");
        OUI_DATABASE.put("00:14:C2", "HP");
        OUI_DATABASE.put("00:15:60", "HP");
        OUI_DATABASE.put("00:16:35", "HP");
        OUI_DATABASE.put("00:17:08", "HP");
        OUI_DATABASE.put("00:17:A4", "HP");
        OUI_DATABASE.put("00:18:71", "HP");
        OUI_DATABASE.put("00:18:FE", "HP");
        OUI_DATABASE.put("00:19:BB", "HP");

        // Amazon
        OUI_DATABASE.put("00:FC:8B", "Amazon");
        OUI_DATABASE.put("0C:47:C9", "Amazon");
        OUI_DATABASE.put("10:AE:60", "Amazon");
        OUI_DATABASE.put("18:74:2E", "Amazon");
        OUI_DATABASE.put("1C:12:B0", "Amazon");
        OUI_DATABASE.put("34:D2:70", "Amazon");
        OUI_DATABASE.put("38:F7:3D", "Amazon");
        OUI_DATABASE.put("40:B4:CD", "Amazon");
        OUI_DATABASE.put("44:65:0D", "Amazon");
        OUI_DATABASE.put("4C:EF:C0", "Amazon");
        OUI_DATABASE.put("50:DC:E7", "Amazon");
        OUI_DATABASE.put("50:F5:DA", "Amazon");
        OUI_DATABASE.put("58:38:79", "Amazon");
        OUI_DATABASE.put("68:37:E9", "Amazon");
        OUI_DATABASE.put("68:54:FD", "Amazon");
        OUI_DATABASE.put("74:C2:46", "Amazon");
        OUI_DATABASE.put("78:E1:03", "Amazon");
        OUI_DATABASE.put("84:D6:D0", "Amazon");
        OUI_DATABASE.put("A0:02:DC", "Amazon");
        OUI_DATABASE.put("AC:63:BE", "Amazon");
        OUI_DATABASE.put("B0:FC:0D", "Amazon");
        OUI_DATABASE.put("F0:27:2D", "Amazon");
        OUI_DATABASE.put("F0:D2:F1", "Amazon");
        OUI_DATABASE.put("FC:65:DE", "Amazon");
        OUI_DATABASE.put("FE:FC:FE", "Amazon");

        // Google
        OUI_DATABASE.put("00:1A:11", "Google");
        OUI_DATABASE.put("08:9E:08", "Google");
        OUI_DATABASE.put("18:D6:C7", "Google");
        OUI_DATABASE.put("1C:F2:9A", "Google");
        OUI_DATABASE.put("20:DF:B9", "Google");
        OUI_DATABASE.put("3C:5A:B4", "Google");
        OUI_DATABASE.put("44:07:0B", "Google");
        OUI_DATABASE.put("48:D6:D5", "Google");
        OUI_DATABASE.put("54:60:09", "Google");
        OUI_DATABASE.put("58:CB:52", "Google");
        OUI_DATABASE.put("5C:E8:31", "Google");
        OUI_DATABASE.put("94:EB:2C", "Google");
        OUI_DATABASE.put("98:D2:93", "Google");
        OUI_DATABASE.put("A4:77:33", "Google");
        OUI_DATABASE.put("D8:6C:63", "Google");
        OUI_DATABASE.put("F4:F5:D8", "Google");
        OUI_DATABASE.put("F4:F5:E8", "Google");
        OUI_DATABASE.put("F8:0F:F9", "Google");

        // Sony
        OUI_DATABASE.put("00:01:4A", "Sony");
        OUI_DATABASE.put("00:04:1F", "Sony");
        OUI_DATABASE.put("00:0A:D9", "Sony");
        OUI_DATABASE.put("00:0E:07", "Sony");
        OUI_DATABASE.put("00:12:EE", "Sony");
        OUI_DATABASE.put("00:13:A9", "Sony");
        OUI_DATABASE.put("00:15:C1", "Sony");
        OUI_DATABASE.put("00:16:20", "Sony");
        OUI_DATABASE.put("00:18:13", "Sony");
        OUI_DATABASE.put("00:19:63", "Sony");
        OUI_DATABASE.put("00:19:C5", "Sony");
        OUI_DATABASE.put("00:1A:80", "Sony");
        OUI_DATABASE.put("00:1B:59", "Sony");
        OUI_DATABASE.put("00:1C:A4", "Sony");
        OUI_DATABASE.put("00:1D:0D", "Sony");
        OUI_DATABASE.put("00:1D:BA", "Sony");
        OUI_DATABASE.put("00:1E:A4", "Sony");
        OUI_DATABASE.put("00:1F:E4", "Sony");
        OUI_DATABASE.put("00:21:4F", "Sony");
        OUI_DATABASE.put("00:22:98", "Sony");
        OUI_DATABASE.put("00:23:45", "Sony");
        OUI_DATABASE.put("00:24:8D", "Sony");
        OUI_DATABASE.put("00:24:BE", "Sony");
        OUI_DATABASE.put("00:25:E7", "Sony");
        OUI_DATABASE.put("00:26:43", "Sony");

        // Microsoft
        OUI_DATABASE.put("00:03:FF", "Microsoft");
        OUI_DATABASE.put("00:0D:3A", "Microsoft");
        OUI_DATABASE.put("00:12:5A", "Microsoft");
        OUI_DATABASE.put("00:15:5D", "Microsoft");
        OUI_DATABASE.put("00:17:FA", "Microsoft");
        OUI_DATABASE.put("00:1D:D8", "Microsoft");
        OUI_DATABASE.put("00:22:48", "Microsoft");
        OUI_DATABASE.put("00:25:AE", "Microsoft");
        OUI_DATABASE.put("00:50:F2", "Microsoft");
        OUI_DATABASE.put("28:18:78", "Microsoft");
        OUI_DATABASE.put("30:59:B7", "Microsoft");
        OUI_DATABASE.put("50:1A:C5", "Microsoft");
        OUI_DATABASE.put("58:82:A8", "Microsoft");
        OUI_DATABASE.put("60:45:BD", "Microsoft");
        OUI_DATABASE.put("7C:1E:52", "Microsoft");
        OUI_DATABASE.put("7C:ED:8D", "Microsoft");
        OUI_DATABASE.put("98:5F:D3", "Microsoft");
        OUI_DATABASE.put("B4:0E:DE", "Microsoft");
        OUI_DATABASE.put("C8:3F:26", "Microsoft");
        OUI_DATABASE.put("D4:3D:7E", "Microsoft");

        // Raspberry Pi
        OUI_DATABASE.put("B8:27:EB", "Raspberry Pi");
        OUI_DATABASE.put("DC:A6:32", "Raspberry Pi");
        OUI_DATABASE.put("E4:5F:01", "Raspberry Pi");

        // ASUS
        OUI_DATABASE.put("00:0C:6E", "ASUS");
        OUI_DATABASE.put("00:0E:A6", "ASUS");
        OUI_DATABASE.put("00:11:2F", "ASUS");
        OUI_DATABASE.put("00:11:D8", "ASUS");
        OUI_DATABASE.put("00:13:D4", "ASUS");
        OUI_DATABASE.put("00:15:F2", "ASUS");
        OUI_DATABASE.put("00:17:31", "ASUS");
        OUI_DATABASE.put("00:18:F3", "ASUS");
        OUI_DATABASE.put("00:1A:92", "ASUS");
        OUI_DATABASE.put("00:1B:FC", "ASUS");
        OUI_DATABASE.put("00:1D:60", "ASUS");
        OUI_DATABASE.put("00:1E:8C", "ASUS");
        OUI_DATABASE.put("00:1F:C6", "ASUS");
        OUI_DATABASE.put("00:22:15", "ASUS");
        OUI_DATABASE.put("00:23:54", "ASUS");
        OUI_DATABASE.put("00:24:8C", "ASUS");
        OUI_DATABASE.put("00:25:22", "ASUS");
        OUI_DATABASE.put("00:26:18", "ASUS");

        // Linksys
        OUI_DATABASE.put("00:04:5A", "Linksys");
        OUI_DATABASE.put("00:06:25", "Linksys");
        OUI_DATABASE.put("00:0C:41", "Linksys");
        OUI_DATABASE.put("00:0F:66", "Linksys");
        OUI_DATABASE.put("00:12:17", "Linksys");
        OUI_DATABASE.put("00:13:10", "Linksys");
        OUI_DATABASE.put("00:14:BF", "Linksys");
        OUI_DATABASE.put("00:16:B6", "Linksys");
        OUI_DATABASE.put("00:18:39", "Linksys");
        OUI_DATABASE.put("00:18:F8", "Linksys");
        OUI_DATABASE.put("00:1A:70", "Linksys");
        OUI_DATABASE.put("00:1C:10", "Linksys");
        OUI_DATABASE.put("00:1D:7E", "Linksys");
        OUI_DATABASE.put("00:1E:E5", "Linksys");
        OUI_DATABASE.put("00:21:29", "Linksys");
        OUI_DATABASE.put("00:22:6B", "Linksys");
        OUI_DATABASE.put("00:23:69", "Linksys");
        OUI_DATABASE.put("00:25:9C", "Linksys");

        // Nintendo
        OUI_DATABASE.put("00:09:BF", "Nintendo");
        OUI_DATABASE.put("00:16:56", "Nintendo");
        OUI_DATABASE.put("00:17:AB", "Nintendo");
        OUI_DATABASE.put("00:19:1D", "Nintendo");
        OUI_DATABASE.put("00:19:FD", "Nintendo");
        OUI_DATABASE.put("00:1A:E9", "Nintendo");
        OUI_DATABASE.put("00:1B:7A", "Nintendo");
        OUI_DATABASE.put("00:1B:EA", "Nintendo");
        OUI_DATABASE.put("00:1C:BE", "Nintendo");
        OUI_DATABASE.put("00:1D:BC", "Nintendo");
        OUI_DATABASE.put("00:1E:35", "Nintendo");
        OUI_DATABASE.put("00:1E:A9", "Nintendo");
        OUI_DATABASE.put("00:1F:32", "Nintendo");
        OUI_DATABASE.put("00:1F:C5", "Nintendo");
        OUI_DATABASE.put("00:21:47", "Nintendo");
        OUI_DATABASE.put("00:21:BD", "Nintendo");
        OUI_DATABASE.put("00:22:4C", "Nintendo");
        OUI_DATABASE.put("00:22:AA", "Nintendo");
        OUI_DATABASE.put("00:23:31", "Nintendo");
        OUI_DATABASE.put("00:23:CC", "Nintendo");
        OUI_DATABASE.put("00:24:1E", "Nintendo");
        OUI_DATABASE.put("00:24:44", "Nintendo");
        OUI_DATABASE.put("00:24:F3", "Nintendo");
        OUI_DATABASE.put("00:25:A0", "Nintendo");
        OUI_DATABASE.put("00:26:59", "Nintendo");
        OUI_DATABASE.put("2C:10:C1", "Nintendo");
        OUI_DATABASE.put("34:AF:2C", "Nintendo");
        OUI_DATABASE.put("40:D2:8A", "Nintendo");
        OUI_DATABASE.put("58:BD:A3", "Nintendo");
        OUI_DATABASE.put("7C:BB:8A", "Nintendo");
        OUI_DATABASE.put("8C:CD:E8", "Nintendo");
        OUI_DATABASE.put("98:41:5C", "Nintendo");
        OUI_DATABASE.put("E0:0C:7F", "Nintendo");
        OUI_DATABASE.put("E8:4E:CE", "Nintendo");
    }

    public static String lookupVendor(String macAddress) {
        if (macAddress == null || macAddress.isEmpty()) {
            return "Unknown";
        }

        String normalizedMac = NetworkUtils.normalizeMacAddress(macAddress);
        String prefix = normalizedMac.substring(0, 8);

        String vendor = OUI_DATABASE.get(prefix);
        if (vendor != null) {
            return vendor;
        }

        return "Unknown";
    }

    public static String getMacPrefix(String macAddress) {
        if (macAddress == null || macAddress.length() < 8) {
            return "";
        }
        return NetworkUtils.normalizeMacAddress(macAddress).substring(0, 8);
    }
}
