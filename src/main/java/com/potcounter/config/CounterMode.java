package com.potcounter.config;

public enum CounterMode {
    SINGLE("Single"),
    ALL_PLAYERS("All Players");

    private final String displayName;

    CounterMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CounterMode next() {
        CounterMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
