package com.k2so.watcher.dto;

import com.k2so.watcher.model.DeviceType;

public class DeviceAnalysisResult {

    private DeviceType deviceType;
    private String suggestedName;
    private String notes;

    public DeviceAnalysisResult() {
    }

    public DeviceAnalysisResult(DeviceType deviceType, String suggestedName, String notes) {
        this.deviceType = deviceType;
        this.suggestedName = suggestedName;
        this.notes = notes;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getSuggestedName() {
        return suggestedName;
    }

    public void setSuggestedName(String suggestedName) {
        this.suggestedName = suggestedName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
