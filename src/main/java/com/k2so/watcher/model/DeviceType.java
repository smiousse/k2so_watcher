package com.k2so.watcher.model;

public enum DeviceType {
    ACCESS_POINT("Access Point", "📶"),
    CAMERA("Camera", "📷"),
    COMPUTER("Computer", "🖥️"),
    DOOR_LOCK("Door Lock", "🔒"),
    GAMING_CONSOLE("Gaming Console", "🎮"),
    GARAGE_DOOR("Garage Door", "🚪"),
    LAPTOP("Laptop", "💻"),
    MINER("Miner", "⛏️"),
    NAS("NAS Storage", "💾"),
    PRINTER("Printer", "🖨️"),
    ROUTER("Router", "🌐"),
    SERVER("Server", "🖧"),
    SMART_HOME("Smart Home Device", "🏠"),
    SMART_PLUG("Smart Plug", "🔌"),
    SMART_SWITCH("Smart Switch", "💡"),
    SMART_TV("Smart TV", "📺"),
    SMARTPHONE("Smartphone", "📱"),
    STREAMING_DEVICE("Streaming Device", "📡"),
    SWITCH("Network Switch", "🔀"),
    TABLET("Tablet", "📲"),
    TEMPERATURE_SENSOR("Temperature Sensor", "🌡️"),
    UNKNOWN("Unknown", "❓");

    private final String displayName;
    private final String icon;

    DeviceType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
