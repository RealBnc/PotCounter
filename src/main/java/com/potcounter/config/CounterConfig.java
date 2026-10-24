package com.potcounter.config;

public class CounterConfig {
    private boolean active = true;
    private CounterMode counterMode = CounterMode.SINGLE;
    private CounterDisplay counterDisplay = CounterDisplay.NAMETAG;
    private boolean debugMode = false;

    private boolean soundEnabled = true;
    private String soundId = "minecraft:entity.experience_orb.pickup";
    private float soundVolume = 1.0f;

    private boolean autoColor = true;
    private int customColor = 0xFFD54F;

    private float nameFontScale = 1.0f;
    private int nameOffsetX = 0;
    private int nameOffsetY = 0;
    private int nameGap = 4;
    private boolean nameBackgroundEnabled = true;
    private int nameBackgroundColor = 0x111116;
    private float nameBackgroundOpacity = 0.5f;
    private boolean nameBorderEnabled = false;
    private int nameBorderColor = 0xFFD54F;
    private int namePadding = 1;

    private int menuX = -1;
    private int menuY = 10;
    private int menuWidth = 140;
    private int menuHeight = 100;
    private float menuScale = 1.0f;
    private boolean menuShowTitle = true;
    private int menuThemeIndex = 0;
    private boolean menuBackgroundEnabled = true;
    private int menuBackgroundColor = 0x121216;
    private float menuBackgroundOpacity = 0.65f;
    private boolean menuBorderEnabled = true;
    private int menuBorderColor = 0xFFFFD54F;
    private int menuPadding = 6;
    private int menuLineSpacing = 2;
    private boolean menuAutoColor = true;
    private int menuCustomColor = 0xFFFFD54F;

    public CounterConfig() {}

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public CounterMode getCounterMode() { return counterMode != null ? counterMode : CounterMode.SINGLE; }
    public void setCounterMode(CounterMode counterMode) { this.counterMode = counterMode; }

    public CounterDisplay getCounterDisplay() { return counterDisplay != null ? counterDisplay : CounterDisplay.NAMETAG; }
    public void setCounterDisplay(CounterDisplay counterDisplay) { this.counterDisplay = counterDisplay; }

    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }

    public String getSoundId() { return soundId != null ? soundId : "minecraft:entity.experience_orb.pickup"; }
    public void setSoundId(String soundId) { this.soundId = soundId; }

    public float getSoundVolume() { return Math.max(0.0f, Math.min(1.0f, soundVolume)); }
    public void setSoundVolume(float soundVolume) { this.soundVolume = Math.max(0.0f, Math.min(1.0f, soundVolume)); }

    public boolean isAutoColor() { return autoColor; }
    public void setAutoColor(boolean autoColor) { this.autoColor = autoColor; }

    public int getCustomColor() { return customColor; }
    public void setCustomColor(int customColor) { this.customColor = customColor; }

    public float getNameFontScale() { return Math.max(0.5f, Math.min(2.0f, nameFontScale)); }
    public void setNameFontScale(float nameFontScale) { this.nameFontScale = Math.max(0.5f, Math.min(2.0f, nameFontScale)); }

    public int getNameOffsetX() { return nameOffsetX; }
    public void setNameOffsetX(int nameOffsetX) { this.nameOffsetX = nameOffsetX; }

    public int getNameOffsetY() { return nameOffsetY; }
    public void setNameOffsetY(int nameOffsetY) { this.nameOffsetY = nameOffsetY; }

    public int getNameGap() { return Math.max(0, Math.min(30, nameGap)); }
    public void setNameGap(int nameGap) { this.nameGap = Math.max(0, Math.min(30, nameGap)); }

    public boolean isNameBackgroundEnabled() { return nameBackgroundEnabled; }
    public void setNameBackgroundEnabled(boolean nameBackgroundEnabled) { this.nameBackgroundEnabled = nameBackgroundEnabled; }

    public int getNameBackgroundColor() { return nameBackgroundColor; }
    public void setNameBackgroundColor(int nameBackgroundColor) { this.nameBackgroundColor = nameBackgroundColor; }

    public float getNameBackgroundOpacity() { return Math.max(0.0f, Math.min(1.0f, nameBackgroundOpacity)); }
    public void setNameBackgroundOpacity(float nameBackgroundOpacity) { this.nameBackgroundOpacity = Math.max(0.0f, Math.min(1.0f, nameBackgroundOpacity)); }

    public boolean isNameBorderEnabled() { return nameBorderEnabled; }
    public void setNameBorderEnabled(boolean nameBorderEnabled) { this.nameBorderEnabled = nameBorderEnabled; }

    public int getNameBorderColor() { return nameBorderColor; }
    public void setNameBorderColor(int nameBorderColor) { this.nameBorderColor = nameBorderColor; }

    public int getNamePadding() { return Math.max(0, Math.min(10, namePadding)); }
    public void setNamePadding(int namePadding) { this.namePadding = Math.max(0, Math.min(10, namePadding)); }

    public int getMenuX() { return menuX; }
    public void setMenuX(int menuX) { this.menuX = menuX; }

    public int getMenuY() { return menuY; }
    public void setMenuY(int menuY) { this.menuY = menuY; }

    public int getMenuWidth() { return Math.max(80, Math.min(400, menuWidth)); }
    public void setMenuWidth(int menuWidth) { this.menuWidth = Math.max(80, Math.min(400, menuWidth)); }

    public int getMenuHeight() { return Math.max(40, Math.min(600, menuHeight)); }
    public void setMenuHeight(int menuHeight) { this.menuHeight = Math.max(40, Math.min(600, menuHeight)); }

    public float getMenuScale() { return Math.max(0.6f, Math.min(1.8f, menuScale)); }
    public void setMenuScale(float menuScale) { this.menuScale = Math.max(0.6f, Math.min(1.8f, menuScale)); }

    public boolean isMenuShowTitle() { return menuShowTitle; }
    public void setMenuShowTitle(boolean menuShowTitle) { this.menuShowTitle = menuShowTitle; }

    public int getMenuThemeIndex() { return menuThemeIndex; }
    public void setMenuThemeIndex(int menuThemeIndex) { this.menuThemeIndex = menuThemeIndex; }

    public boolean isMenuBackgroundEnabled() { return menuBackgroundEnabled; }
    public void setMenuBackgroundEnabled(boolean menuBackgroundEnabled) { this.menuBackgroundEnabled = menuBackgroundEnabled; }

    public int getMenuBackgroundColor() { return menuBackgroundColor; }
    public void setMenuBackgroundColor(int menuBackgroundColor) { this.menuBackgroundColor = menuBackgroundColor; }

    public float getMenuBackgroundOpacity() { return Math.max(0.0f, Math.min(1.0f, menuBackgroundOpacity)); }
    public void setMenuBackgroundOpacity(float menuBackgroundOpacity) { this.menuBackgroundOpacity = Math.max(0.0f, Math.min(1.0f, menuBackgroundOpacity)); }

    public boolean isMenuBorderEnabled() { return menuBorderEnabled; }
    public void setMenuBorderEnabled(boolean menuBorderEnabled) { this.menuBorderEnabled = menuBorderEnabled; }

    public int getMenuBorderColor() { return menuBorderColor; }
    public void setMenuBorderColor(int menuBorderColor) { this.menuBorderColor = menuBorderColor; }

    public int getMenuPadding() { return Math.max(0, Math.min(20, menuPadding)); }
    public void setMenuPadding(int menuPadding) { this.menuPadding = Math.max(0, Math.min(20, menuPadding)); }

    public int getMenuLineSpacing() { return Math.max(0, Math.min(15, menuLineSpacing)); }
    public void setMenuLineSpacing(int menuLineSpacing) { this.menuLineSpacing = Math.max(0, Math.min(15, menuLineSpacing)); }

    public boolean isMenuAutoColor() { return menuAutoColor; }
    public void setMenuAutoColor(boolean menuAutoColor) { this.menuAutoColor = menuAutoColor; }

    public int getMenuCustomColor() { return menuCustomColor; }
    public void setMenuCustomColor(int menuCustomColor) { this.menuCustomColor = menuCustomColor; }

    public void applyTheme(int themeIdx) {
        this.menuThemeIndex = themeIdx;
        switch (themeIdx) {
            case 0 -> {
                this.menuBackgroundColor = 0x111116;
                this.menuBorderColor = 0xFFFFD54F;
                this.menuCustomColor = 0xFFFFD54F;
                this.menuBackgroundOpacity = 0.70f;
            }
            case 1 -> {
                this.menuBackgroundColor = 0x0A0A0E;
                this.menuBorderColor = 0xFFFFA000;
                this.menuCustomColor = 0xFFFFC107;
                this.menuBackgroundOpacity = 0.85f;
            }
            case 2 -> {
                this.menuBackgroundColor = 0x1A1A1E;
                this.menuBorderColor = 0xFFFFFFFF;
                this.menuCustomColor = 0xFFFFFFFF;
                this.menuBackgroundOpacity = 0.60f;
            }
            case 3 -> {
                this.menuBackgroundColor = 0x0E1422;
                this.menuBorderColor = 0xFFFFE082;
                this.menuCustomColor = 0xFFFFD54F;
                this.menuBackgroundOpacity = 0.75f;
            }
        }
    }

    public static String getThemeName(int idx) {
        return switch (idx) {
            case 0 -> "Black & Gold";
            case 1 -> "Cyber Amber";
            case 2 -> "Carbon White";
            case 3 -> "Night & Gold";
            default -> "Custom";
        };
    }

    public void resetMenuToDefaults() {
        this.menuX = -1;
        this.menuY = 10;
        this.menuWidth = 140;
        this.menuHeight = 100;
        this.menuScale = 1.0f;
        this.menuShowTitle = true;
        this.menuBackgroundEnabled = true;
        this.menuBackgroundColor = 0x111116;
        this.menuBackgroundOpacity = 0.70f;
        this.menuBorderEnabled = true;
        this.menuBorderColor = 0xFFFFD54F;
        this.menuPadding = 6;
        this.menuLineSpacing = 2;
        this.menuAutoColor = true;
        this.menuCustomColor = 0xFFFFD54F;
        this.menuThemeIndex = 0;
    }
}
