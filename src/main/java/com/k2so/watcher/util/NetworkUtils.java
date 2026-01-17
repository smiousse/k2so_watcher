package com.k2so.watcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
    private static final Pattern MAC_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})");
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.getHostAddress().contains(":")) continue; // Skip IPv6
                    if (!addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error getting local IP address", e);
        }
        return "127.0.0.1";
    }

    public static String getLocalNetworkRange() {
        String localIp = getLocalIpAddress();
        String[] parts = localIp.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + ".0/24";
        }
        return "192.168.1.0/24";
    }

    public static List<String> generateIpRange(String cidr) {
        List<String> ips = new ArrayList<>();
        try {
            String[] parts = cidr.split("/");
            String baseIp = parts[0];
            int prefix = Integer.parseInt(parts[1]);

            String[] ipParts = baseIp.split("\\.");
            int baseAddr = (Integer.parseInt(ipParts[0]) << 24) |
                          (Integer.parseInt(ipParts[1]) << 16) |
                          (Integer.parseInt(ipParts[2]) << 8) |
                          Integer.parseInt(ipParts[3]);

            int mask = ~((1 << (32 - prefix)) - 1);
            int network = baseAddr & mask;
            int broadcast = network | ~mask;

            for (int addr = network + 1; addr < broadcast; addr++) {
                String ip = ((addr >> 24) & 0xFF) + "." +
                           ((addr >> 16) & 0xFF) + "." +
                           ((addr >> 8) & 0xFF) + "." +
                           (addr & 0xFF);
                ips.add(ip);
            }
        } catch (Exception e) {
            logger.error("Error generating IP range from CIDR: {}", cidr, e);
        }
        return ips;
    }

    public static boolean isValidMacAddress(String mac) {
        if (mac == null) return false;
        return MAC_PATTERN.matcher(mac).matches();
    }

    public static String normalizeMacAddress(String mac) {
        if (mac == null) return null;
        return mac.toUpperCase().replace("-", ":");
    }

    public static String extractMacFromArpOutput(String line) {
        Matcher matcher = MAC_PATTERN.matcher(line);
        if (matcher.find()) {
            return normalizeMacAddress(matcher.group());
        }
        return null;
    }

    public static String extractIpFromLine(String line) {
        Matcher matcher = IP_PATTERN.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static boolean isHostReachable(String host, int timeout) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeout);
        } catch (Exception e) {
            return false;
        }
    }

    public static String executeCommand(String... command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();
        } catch (Exception e) {
            logger.error("Error executing command: {}", String.join(" ", command), e);
        }
        return output.toString();
    }

    public static boolean isToolAvailable(String tool) {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", tool);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
