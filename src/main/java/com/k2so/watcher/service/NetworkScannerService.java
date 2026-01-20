package com.k2so.watcher.service;

import com.k2so.watcher.model.*;
import com.k2so.watcher.repository.DeviceRepository;
import com.k2so.watcher.repository.NetworkScanRepository;
import com.k2so.watcher.repository.ScanResultRepository;
import com.k2so.watcher.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NetworkScannerService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkScannerService.class);

    private final DeviceRepository deviceRepository;
    private final NetworkScanRepository networkScanRepository;
    private final ScanResultRepository scanResultRepository;
    private final MacVendorService macVendorService;
    private final DeviceIdentificationService deviceIdentificationService;

    @Value("${k2so.network.scan-range:192.168.1.0/24}")
    private String defaultScanRange;

    @Value("${k2so.network.scanner-tool:arp-scan}")
    private String scannerTool;

    @Value("${k2so.network.scan-timeout:120}")
    private int scanTimeout;

    private volatile boolean scanInProgress = false;

    // Check if running as root (UID 0) - if so, don't need sudo
    private final boolean isRoot = System.getProperty("user.name").equals("root") ||
                                   "0".equals(System.getenv("EUID")) ||
                                   checkIfRoot();

    public NetworkScannerService(DeviceRepository deviceRepository,
                                  NetworkScanRepository networkScanRepository,
                                  ScanResultRepository scanResultRepository,
                                  MacVendorService macVendorService,
                                  DeviceIdentificationService deviceIdentificationService) {
        this.deviceRepository = deviceRepository;
        this.networkScanRepository = networkScanRepository;
        this.scanResultRepository = scanResultRepository;
        this.macVendorService = macVendorService;
        this.deviceIdentificationService = deviceIdentificationService;
    }

    public boolean isScanInProgress() {
        return scanInProgress;
    }

    @Transactional
    public NetworkScan startScan(String scanType) {
        if (scanInProgress) {
            throw new IllegalStateException("A scan is already in progress");
        }

        scanInProgress = true;
        NetworkScan scan = new NetworkScan();
        scan.setScanType(scanType);
        scan.setNetworkRange(defaultScanRange);
        scan.setStatus("RUNNING");
        scan = networkScanRepository.save(scan);

        final Long scanId = scan.getId();

        // Run scan in separate thread
        new Thread(() -> performScan(scanId)).start();

        return scan;
    }

    private void performScan(Long scanId) {
        try {
            NetworkScan scan = networkScanRepository.findById(scanId).orElse(null);
            if (scan == null) {
                logger.error("Scan not found: {}", scanId);
                return;
            }

            logger.info("Starting network scan: {} on range {}", scanId, scan.getNetworkRange());

            // Mark all devices as offline before scan
            List<Device> allDevices = deviceRepository.findAll();
            for (Device device : allDevices) {
                device.setOnline(false);
                deviceRepository.save(device);
            }

            List<Map<String, String>> discoveredHosts;
            StringBuilder scanLogBuilder = new StringBuilder();

            // Try different scanning methods
            if (NetworkUtils.isToolAvailable("arp-scan") && "arp-scan".equals(scannerTool)) {
                scan.setScannerTool("arp-scan");
                discoveredHosts = scanWithArpScan(scan.getNetworkRange(), scanLogBuilder);
            } else if (NetworkUtils.isToolAvailable("nmap")) {
                scan.setScannerTool("nmap");
                discoveredHosts = scanWithNmap(scan.getNetworkRange(), scanLogBuilder);
            } else {
                scan.setScannerTool("ping");
                discoveredHosts = scanWithPing(scan.getNetworkRange(), scanLogBuilder);
            }

            scan.setScanLog(scanLogBuilder.toString());

            int newDevices = 0;
            int skippedDevices = 0;
            Set<String> processedMacs = new HashSet<>();
            Set<String> processedIps = new HashSet<>();
            StringBuilder skippedLog = new StringBuilder();

            for (Map<String, String> host : discoveredHosts) {
                String macAddress = host.get("mac");
                String ipAddress = host.get("ip");
                String hostname = host.get("hostname");
                String scannedVendor = host.get("vendor");

                if (ipAddress == null || ipAddress.isEmpty()) {
                    continue;
                }

                // Skip if we've already processed this IP in this scan
                if (processedIps.contains(ipAddress)) {
                    logger.debug("Skipping duplicate IP address in scan results: {}", ipAddress);
                    continue;
                }

                boolean hasMac = macAddress != null && !macAddress.isEmpty();
                if (hasMac) {
                    macAddress = NetworkUtils.normalizeMacAddress(macAddress);
                    // Skip if we've already processed this MAC in this scan (duplicate scan result)
                    if (processedMacs.contains(macAddress)) {
                        logger.debug("Skipping duplicate MAC address in scan results: {}", macAddress);
                        continue;
                    }
                    processedMacs.add(macAddress);
                }

                // Find or create device - MAC is the primary identifier
                Device device = null;
                Device deviceByIp = null;

                if (hasMac) {
                    device = deviceRepository.findByMacAddress(macAddress).orElse(null);
                }

                // Check if IP already exists in database
                deviceByIp = deviceRepository.findByIpAddress(ipAddress).orElse(null);

                if (device == null && deviceByIp != null) {
                    // A device with this IP already exists
                    if (!hasMac) {
                        // No new MAC to set, use the existing device
                        device = deviceByIp;
                    } else if (deviceByIp.getMacAddress() == null ||
                               deviceByIp.getMacAddress().startsWith("fe:00:") ||
                               deviceByIp.getMacAddress().equals(macAddress)) {
                        // Device has no MAC, has a placeholder MAC, or same MAC - safe to update
                        device = deviceByIp;
                    } else {
                        // A device exists at this IP with a different real MAC
                        // Skip this device to prevent duplicates
                        skippedDevices++;
                        String skipMsg = String.format("Skipped: IP %s (MAC: %s) - IP already assigned to device '%s' (MAC: %s)",
                                ipAddress, macAddress, deviceByIp.getDisplayName(), deviceByIp.getMacAddress());
                        skippedLog.append(skipMsg).append("\n");
                        logger.info(skipMsg);
                        processedIps.add(ipAddress);
                        continue;
                    }
                }

                boolean isNew = (device == null);

                if (isNew) {
                    device = new Device();
                    device.setKnown(false);
                    newDevices++;
                }

                // Update MAC if we have one (might get MAC later for cross-VLAN device)
                if (hasMac) {
                    device.setMacAddress(macAddress);
                } else if (device.getMacAddress() == null) {
                    // No MAC available - generate a unique placeholder that won't collide with real MACs
                    // Using fe:xx format which is in the locally administered range
                    device.setMacAddress("fe:00:" + String.format("%02x", (ipAddress.hashCode() >> 24) & 0xff) + ":" +
                            String.format("%02x", (ipAddress.hashCode() >> 16) & 0xff) + ":" +
                            String.format("%02x", (ipAddress.hashCode() >> 8) & 0xff) + ":" +
                            String.format("%02x", ipAddress.hashCode() & 0xff));
                }

                processedIps.add(ipAddress);

                device.setIpAddress(ipAddress);
                if (hostname != null && !hostname.isEmpty()) {
                    device.setHostname(hostname);
                }

                // Use vendor from scan output if available, otherwise lookup by MAC
                String vendor = (scannedVendor != null && !scannedVendor.isEmpty())
                    ? scannedVendor
                    : (hasMac ? macVendorService.lookupVendor(macAddress) : "Unknown (Cross-VLAN)");
                device.setVendor(vendor);

                // Identify device type if unknown
                if (device.getDeviceType() == null || device.getDeviceType() == DeviceType.UNKNOWN) {
                    device.setDeviceType(deviceIdentificationService.identifyDeviceType(device));
                }

                device.setOnline(true);
                device.setLastSeen(LocalDateTime.now());

                device = deviceRepository.save(device);

                // Create scan result
                ScanResult result = new ScanResult();
                result.setNetworkScan(scan);
                result.setDevice(device);
                result.setMacAddress(device.getMacAddress());
                result.setIpAddress(ipAddress);
                result.setHostname(hostname);
                result.setVendor(vendor);
                result.setNewDevice(isNew);
                scanResultRepository.save(result);

                // Auto-trigger deep scan for new devices
                if (isNew && ipAddress != null && !ipAddress.isEmpty()) {
                    final Long deviceId = device.getId();
                    logger.info("Scheduling deep scan for new device {} ({})", deviceId, ipAddress);
                    performDeepScanAsync(deviceId);
                }
            }

            // Append skipped devices log to scan log
            if (skippedDevices > 0) {
                String currentLog = scan.getScanLog() != null ? scan.getScanLog() : "";
                scan.setScanLog(currentLog + "\n--- Skipped Devices (Duplicate IPs) ---\n" + skippedLog.toString());
            }

            // Update scan status
            scan.setStatus("COMPLETED");
            scan.setCompletedAt(LocalDateTime.now());
            scan.setDevicesFound(discoveredHosts.size() - skippedDevices);
            scan.setNewDevices(newDevices);
            networkScanRepository.save(scan);

            logger.info("Scan completed: {} devices found, {} new, {} skipped (duplicate IPs)",
                    discoveredHosts.size() - skippedDevices, newDevices, skippedDevices);

        } catch (Exception e) {
            logger.error("Error during network scan", e);
            NetworkScan scan = networkScanRepository.findById(scanId).orElse(null);
            if (scan != null) {
                scan.setStatus("FAILED");
                // Truncate error message to fit in the column (max 1000 chars)
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 1000) {
                    errorMsg = errorMsg.substring(0, 997) + "...";
                }
                scan.setErrorMessage(errorMsg);
                scan.setCompletedAt(LocalDateTime.now());
                networkScanRepository.save(scan);
            }
        } finally {
            scanInProgress = false;
        }
    }

    private List<Map<String, String>> scanWithArpScan(String networkRange, StringBuilder scanLog) {
        List<Map<String, String>> hosts = new ArrayList<>();

        try {
            // Use the configured network range instead of --localnet to ensure full range scan
            List<String> command = buildCommand("arp-scan", networkRange);
            scanLog.append("Command: ").append(String.join(" ", command)).append("\n");
            scanLog.append("Network range: ").append(networkRange).append("\n");
            scanLog.append("---\n");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+([0-9a-fA-F:]+)\\s+(.*)");

                while ((line = reader.readLine()) != null) {
                    scanLog.append(line).append("\n");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        Map<String, String> host = new HashMap<>();
                        host.put("ip", matcher.group(1));
                        host.put("mac", matcher.group(2));
                        host.put("vendor", matcher.group(3).trim());
                        hosts.add(host);
                    }
                }
            }

            process.waitFor(scanTimeout, TimeUnit.SECONDS);

        } catch (Exception e) {
            logger.error("Error running arp-scan", e);
            scanLog.append("ERROR: ").append(e.getMessage()).append("\n");
        }

        // Try to resolve hostnames
        for (Map<String, String> host : hosts) {
            String hostname = resolveHostname(host.get("ip"));
            host.put("hostname", hostname);
        }

        return hosts;
    }

    private List<Map<String, String>> scanWithNmap(String networkRange, StringBuilder scanLog) {
        List<Map<String, String>> hosts = new ArrayList<>();

        try {
            List<String> command = buildCommand("nmap", "-sn", networkRange);
            scanLog.append("Command: ").append(String.join(" ", command)).append("\n");
            scanLog.append("Network range: ").append(networkRange).append("\n");
            scanLog.append("---\n");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    scanLog.append(line).append("\n");
                }
            }

            process.waitFor(scanTimeout, TimeUnit.SECONDS);

            // Parse nmap output
            String[] blocks = output.toString().split("Nmap scan report for ");
            for (String block : blocks) {
                if (block.trim().isEmpty()) continue;

                String ip = NetworkUtils.extractIpFromLine(block);
                String mac = NetworkUtils.extractMacFromArpOutput(block);

                if (ip != null) {
                    Map<String, String> host = new HashMap<>();
                    host.put("ip", ip);
                    host.put("mac", mac != null ? mac : "");

                    // Extract hostname from block
                    String[] lines = block.split("\n");
                    if (lines.length > 0) {
                        String firstLine = lines[0].trim();
                        if (firstLine.contains("(")) {
                            String hostname = firstLine.substring(0, firstLine.indexOf("(")).trim();
                            host.put("hostname", hostname);
                        }
                    }

                    // Extract vendor from MAC Address line (e.g., "MAC Address: AA:BB:CC:DD:EE:FF (Vendor Name)")
                    for (String line : lines) {
                        if (line.contains("MAC Address:") && line.contains("(")) {
                            int start = line.indexOf("(") + 1;
                            int end = line.indexOf(")");
                            if (start > 0 && end > start) {
                                host.put("vendor", line.substring(start, end));
                            }
                            break;
                        }
                    }

                    // Add all discovered hosts, even without MAC (for cross-VLAN devices)
                    hosts.add(host);
                }
            }

        } catch (Exception e) {
            logger.error("Error running nmap", e);
            scanLog.append("ERROR: ").append(e.getMessage()).append("\n");
        }

        return hosts;
    }

    private List<Map<String, String>> scanWithPing(String networkRange, StringBuilder scanLog) {
        List<Map<String, String>> hosts = new ArrayList<>();
        List<String> ips = NetworkUtils.generateIpRange(networkRange);

        scanLog.append("Method: Ping sweep\n");
        scanLog.append("Network range: ").append(networkRange).append("\n");
        scanLog.append("Total addresses to scan: ").append(ips.size()).append("\n");
        scanLog.append("---\n");

        logger.info("Performing ping sweep on {} addresses", ips.size());

        for (String ip : ips) {
            if (NetworkUtils.isHostReachable(ip, 500)) {
                Map<String, String> host = new HashMap<>();
                host.put("ip", ip);
                host.put("hostname", resolveHostname(ip));

                // Try to get MAC from ARP cache
                String mac = getMacFromArpCache(ip);
                host.put("mac", mac != null ? mac : "");

                if (mac != null && !mac.isEmpty()) {
                    hosts.add(host);
                    scanLog.append("Found: ").append(ip).append(" - ").append(mac).append("\n");
                }
            }
        }

        return hosts;
    }

    private String getMacFromArpCache(String ip) {
        try {
            String output = NetworkUtils.executeCommand("arp", "-n", ip);
            return NetworkUtils.extractMacFromArpOutput(output);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveHostname(String ip) {
        try {
            java.net.InetAddress addr = java.net.InetAddress.getByName(ip);
            String hostname = addr.getHostName();
            if (!hostname.equals(ip)) {
                return hostname;
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    public List<NetworkScan> getRecentScans(int limit) {
        List<NetworkScan> allScans = networkScanRepository.findAllOrderByStartedAtDesc();
        if (allScans.size() > limit) {
            return allScans.subList(0, limit);
        }
        return allScans;
    }

    public Optional<NetworkScan> getScanById(Long id) {
        return networkScanRepository.findById(id);
    }

    public List<ScanResult> getScanResults(Long scanId) {
        return scanResultRepository.findByNetworkScanId(scanId);
    }

    public Optional<NetworkScan> getLatestScan() {
        return networkScanRepository.findTopByOrderByStartedAtDesc();
    }

    /**
     * Performs an aggressive nmap scan on a specific device to discover open ports, OS, and services.
     * This is a blocking operation that can take several minutes.
     */
    @Transactional
    public void performDeepScan(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null || device.getIpAddress() == null) {
            logger.error("Cannot perform deep scan: device not found or no IP address");
            return;
        }

        String ip = device.getIpAddress();
        logger.info("Starting deep scan on device {} ({})", deviceId, ip);

        StringBuilder scanLog = new StringBuilder();
        scanLog.append("Deep Scan started at: ").append(LocalDateTime.now()).append("\n");
        scanLog.append("Target IP: ").append(ip).append("\n");
        scanLog.append("---\n");

        try {
            List<String> command = buildCommand("nmap", "-A", "-T4", ip);
            scanLog.append("Command: ").append(String.join(" ", command)).append("\n\n");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    scanLog.append(line).append("\n");
                }
            }

            // Wait up to 5 minutes for aggressive scan
            boolean finished = process.waitFor(300, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                scanLog.append("\nWARNING: Scan timed out after 5 minutes\n");
            }

            String nmapOutput = output.toString();

            // Parse open ports
            StringBuilder ports = new StringBuilder();
            Pattern portPattern = Pattern.compile("(\\d+)/(tcp|udp)\\s+open\\s+(\\S+)\\s*(.*)");
            for (String line : nmapOutput.split("\n")) {
                Matcher matcher = portPattern.matcher(line);
                if (matcher.find()) {
                    if (ports.length() > 0) ports.append(", ");
                    ports.append(matcher.group(1))
                         .append("/").append(matcher.group(2))
                         .append(" (").append(matcher.group(3));
                    if (!matcher.group(4).isEmpty()) {
                        ports.append(" - ").append(matcher.group(4).trim());
                    }
                    ports.append(")");
                }
            }
            device.setOpenPorts(ports.toString());

            // Parse OS detection
            Pattern osPattern = Pattern.compile("OS details:\\s*(.+)");
            Matcher osMatcher = osPattern.matcher(nmapOutput);
            if (osMatcher.find()) {
                device.setDetectedOs(osMatcher.group(1).trim());
            } else {
                // Try alternative OS pattern
                Pattern osPattern2 = Pattern.compile("Running:\\s*(.+)");
                Matcher osMatcher2 = osPattern2.matcher(nmapOutput);
                if (osMatcher2.find()) {
                    device.setDetectedOs(osMatcher2.group(1).trim());
                }
            }

            // Update hostname if found and not already set
            Pattern hostnamePattern = Pattern.compile("Nmap scan report for ([^\\s]+)\\s*\\(");
            Matcher hostnameMatcher = hostnamePattern.matcher(nmapOutput);
            if (hostnameMatcher.find()) {
                String discoveredHostname = hostnameMatcher.group(1);
                if (device.getHostname() == null || device.getHostname().isEmpty()) {
                    device.setHostname(discoveredHostname);
                }
            }

            device.setDeepScanLog(scanLog.toString());
            device.setLastDeepScan(LocalDateTime.now());
            deviceRepository.save(device);

            logger.info("Deep scan completed for device {} ({})", deviceId, ip);

        } catch (Exception e) {
            logger.error("Error during deep scan of device {}", deviceId, e);
            scanLog.append("\nERROR: ").append(e.getMessage()).append("\n");
            device.setDeepScanLog(scanLog.toString());
            device.setLastDeepScan(LocalDateTime.now());
            deviceRepository.save(device);
        }
    }

    /**
     * Performs a deep scan asynchronously in a new thread.
     */
    public void performDeepScanAsync(Long deviceId) {
        new Thread(() -> performDeepScan(deviceId)).start();
    }

    private static boolean checkIfRoot() {
        try {
            Process process = new ProcessBuilder("id", "-u").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String uid = reader.readLine();
                return "0".equals(uid);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> buildCommand(String... args) {
        List<String> command = new ArrayList<>();
        if (!isRoot) {
            command.add("sudo");
        }
        command.addAll(Arrays.asList(args));
        return command;
    }
}
