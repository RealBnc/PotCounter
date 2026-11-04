package com.potcounter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("PotCounter/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "potcounter.json";

    private final File configFile;
    private CounterConfig config;

    public ConfigManager() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.configFile = configDir.resolve(FILE_NAME).toFile();
        load();
    }

    public void load() {
        if (!configFile.exists()) {
            LOGGER.info("Config file not found, creating default config.");
            this.config = new CounterConfig();
            save();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            this.config = GSON.fromJson(reader, CounterConfig.class);
            if (this.config == null) {
                LOGGER.warn("Config file was empty or corrupted, resetting to defaults.");
                this.config = new CounterConfig();
                save();
            } else {
                LOGGER.info("Loaded PotCounter config successfully.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read PotCounter config, using default settings.", e);
            this.config = new CounterConfig();
        }
    }

    public void save() {
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this.config, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save PotCounter config.", e);
        }
    }

    public CounterConfig getConfig() {
        if (config == null) {
            config = new CounterConfig();
        }
        return config;
    }
}
