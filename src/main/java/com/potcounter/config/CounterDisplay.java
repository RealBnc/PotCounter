package com.potcounter.config;

public enum CounterDisplay {
    NAMETAG("Nametag"),
    MENU("Menu HUD");

    private final String displayName;

    CounterDisplay(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CounterDisplay next() {
        CounterDisplay[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
