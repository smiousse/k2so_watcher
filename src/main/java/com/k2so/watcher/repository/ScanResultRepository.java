package com.k2so.watcher.repository;

import com.k2so.watcher.model.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanResultRepository extends JpaRepository<ScanResult, Long> {

    List<ScanResult> findByNetworkScanId(Long networkScanId);

    List<ScanResult> findByDeviceId(Long deviceId);

    List<ScanResult> findByNewDeviceTrue();
}
