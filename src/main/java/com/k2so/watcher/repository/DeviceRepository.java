package com.k2so.watcher.repository;

import com.k2so.watcher.model.Device;
import com.k2so.watcher.model.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByMacAddress(String macAddress);

    Optional<Device> findByIpAddress(String ipAddress);

    boolean existsByMacAddress(String macAddress);

    List<Device> findByOnlineTrue();

    List<Device> findByKnownFalse();

    List<Device> findByDeviceType(DeviceType deviceType);

    List<Device> findByTrustedTrue();

    @Query("SELECT d FROM Device d ORDER BY d.lastSeen DESC")
    List<Device> findAllOrderByLastSeenDesc();

    @Query("SELECT COUNT(d) FROM Device d WHERE d.online = true")
    long countOnlineDevices();

    @Query("SELECT COUNT(d) FROM Device d WHERE d.known = false")
    long countUnknownDevices();

    List<Device> findByPinnedTrueOrderByCustomNameAsc();

    @Query("SELECT d FROM Device d WHERE d.ipAddress IN (SELECT d2.ipAddress FROM Device d2 WHERE d2.ipAddress IS NOT NULL GROUP BY d2.ipAddress HAVING COUNT(d2) > 1) ORDER BY d.ipAddress")
    List<Device> findDevicesWithDuplicateIpAddresses();
}
