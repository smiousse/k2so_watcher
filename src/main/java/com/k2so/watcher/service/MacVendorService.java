package com.k2so.watcher.service;

import com.k2so.watcher.util.MacAddressLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MacVendorService {

    private static final Logger logger = LoggerFactory.getLogger(MacVendorService.class);

    public String lookupVendor(String macAddress) {
        if (macAddress == null || macAddress.isEmpty()) {
            return "Unknown";
        }

        try {
            String vendor = MacAddressLookup.lookupVendor(macAddress);
            logger.debug("MAC {} resolved to vendor: {}", macAddress, vendor);
            return vendor;
        } catch (Exception e) {
            logger.error("Error looking up vendor for MAC: {}", macAddress, e);
            return "Unknown";
        }
    }
}
