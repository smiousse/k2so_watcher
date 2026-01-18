package com.k2so.watcher.repository;

import com.k2so.watcher.model.DeviceServiceUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceServiceUrlRepository extends JpaRepository<DeviceServiceUrl, Long> {

    List<DeviceServiceUrl> findByDeviceId(Long deviceId);

    List<DeviceServiceUrl> findByDeviceIdOrderByAliasAsc(Long deviceId);

    void deleteByDeviceId(Long deviceId);
}
