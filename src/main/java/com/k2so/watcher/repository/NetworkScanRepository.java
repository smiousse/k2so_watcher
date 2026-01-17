package com.k2so.watcher.repository;

import com.k2so.watcher.model.NetworkScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NetworkScanRepository extends JpaRepository<NetworkScan, Long> {

    @Query("SELECT n FROM NetworkScan n ORDER BY n.startedAt DESC")
    List<NetworkScan> findAllOrderByStartedAtDesc();

    Optional<NetworkScan> findTopByOrderByStartedAtDesc();

    List<NetworkScan> findByStatus(String status);

    @Query("SELECT n FROM NetworkScan n WHERE n.status = 'RUNNING'")
    Optional<NetworkScan> findRunningScan();
}
