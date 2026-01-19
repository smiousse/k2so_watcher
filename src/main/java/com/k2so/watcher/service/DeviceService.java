package com.k2so.watcher.service;

import com.k2so.watcher.model.Device;
import com.k2so.watcher.model.DeviceServiceUrl;
import com.k2so.watcher.model.DeviceType;
import com.k2so.watcher.repository.DeviceRepository;
import com.k2so.watcher.repository.DeviceServiceUrlRepository;
import com.k2so.watcher.repository.ScanResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceServiceUrlRepository deviceServiceUrlRepository;
    private final ScanResultRepository scanResultRepository;
    private final AIIdentificationService aiIdentificationService;

    public DeviceService(DeviceRepository deviceRepository,
                        DeviceServiceUrlRepository deviceServiceUrlRepository,
                        ScanResultRepository scanResultRepository,
                        AIIdentificationService aiIdentificationService) {
        this.deviceRepository = deviceRepository;
        this.deviceServiceUrlRepository = deviceServiceUrlRepository;
        this.scanResultRepository = scanResultRepository;
        this.aiIdentificationService = aiIdentificationService;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAllOrderByLastSeenDesc();
    }

    public List<Device> getOnlineDevices() {
        return deviceRepository.findByOnlineTrue();
    }

    public List<Device> getUnknownDevices() {
        return deviceRepository.findByKnownFalse();
    }

    public List<Device> getDevicesWithDuplicateIps() {
        return deviceRepository.findDevicesWithDuplicateIpAddresses();
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public Optional<Device> getDeviceByMac(String macAddress) {
        return deviceRepository.findByMacAddress(macAddress);
    }

    @Transactional
    public Device updateDevice(Long id, String customName, DeviceType deviceType,
                               boolean known, boolean trusted, String notes,
                               String serviceUrl, boolean pinned) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setCustomName(customName);
        device.setDeviceType(deviceType);
        device.setKnown(known);
        device.setTrusted(trusted);
        device.setNotes(notes);
        device.setServiceUrl(serviceUrl);
        device.setPinned(pinned);

        return deviceRepository.save(device);
    }

    @Transactional
    public void markAsKnown(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        device.setKnown(true);
        deviceRepository.save(device);
    }

    @Transactional
    public void markAsTrusted(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        device.setTrusted(true);
        device.setKnown(true);
        deviceRepository.save(device);
    }

    @Transactional
    public void deleteDevice(Long id) {
        scanResultRepository.deleteByDeviceId(id);
        deviceRepository.deleteById(id);
    }

    @Transactional
    public String identifyWithAI(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        String identification = aiIdentificationService.identifyDevice(device);
        if (identification != null) {
            device.setAiIdentification(identification);

            // Optionally update device type based on AI suggestion
            DeviceType suggestedType = aiIdentificationService.suggestDeviceType(identification);
            if (suggestedType != DeviceType.UNKNOWN && device.getDeviceType() == DeviceType.UNKNOWN) {
                device.setDeviceType(suggestedType);
            }

            deviceRepository.save(device);
        }

        return identification;
    }

    public long countOnlineDevices() {
        return deviceRepository.countOnlineDevices();
    }

    public long countUnknownDevices() {
        return deviceRepository.countUnknownDevices();
    }

    public long countTotalDevices() {
        return deviceRepository.count();
    }

    public List<Device> getPinnedDevices() {
        return deviceRepository.findByPinnedTrueOrderByCustomNameAsc();
    }

    @Transactional
    public void togglePin(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        device.setPinned(!device.isPinned());
        deviceRepository.save(device);
    }

    @Transactional
    public void updateServiceUrl(Long id, String serviceUrl) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        device.setServiceUrl(serviceUrl);
        deviceRepository.save(device);
    }

    @Transactional
    public Device saveDevice(Device device) {
        return deviceRepository.save(device);
    }

    // Service URL management methods

    @Transactional
    public DeviceServiceUrl addServiceUrl(Long deviceId, String alias, String url) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        DeviceServiceUrl serviceUrl = new DeviceServiceUrl(device, alias, url);
        device.addServiceUrl(serviceUrl);
        deviceRepository.save(device);

        return serviceUrl;
    }

    @Transactional
    public void updateServiceUrlEntry(Long serviceUrlId, String alias, String url) {
        DeviceServiceUrl serviceUrl = deviceServiceUrlRepository.findById(serviceUrlId)
                .orElseThrow(() -> new IllegalArgumentException("Service URL not found"));

        serviceUrl.setAlias(alias);
        serviceUrl.setUrl(url);
        deviceServiceUrlRepository.save(serviceUrl);
    }

    @Transactional
    public void deleteServiceUrl(Long serviceUrlId) {
        deviceServiceUrlRepository.deleteById(serviceUrlId);
    }

    public List<DeviceServiceUrl> getServiceUrls(Long deviceId) {
        return deviceServiceUrlRepository.findByDeviceIdOrderByAliasAsc(deviceId);
    }

    public Optional<DeviceServiceUrl> getServiceUrlById(Long serviceUrlId) {
        return deviceServiceUrlRepository.findById(serviceUrlId);
    }
}
