package com.k2so.watcher.model;

public enum DeviceType {
    COMPUTER("Computer", "🖥️"),
    LAPTOP("Laptop", "💻"),
    SMARTPHONE("Smartphone", "📱"),
    TABLET("Tablet", "📲"),
    SMART_TV("Smart TV", "📺"),
    GAMING_CONSOLE("Gaming Console", "🎮"),
    STREAMING_DEVICE("Streaming Device", "📡"),
    ROUTER("Router", "🌐"),
    SWITCH("Network Switch", "🔀"),
    ACCESS_POINT("Access Point", "📶"),
    SMART_HOME("Smart Home Device", "🏠"),
    PRINTER("Printer", "🖨️"),
    CAMERA("Camera", "📷"),
    SERVER("Server", "🖧"),
    NAS("NAS Storage", "💾"),
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
