package com.potcounter;

import com.potcounter.combat.CombatManager;
import com.potcounter.config.ConfigManager;
import com.potcounter.config.CounterConfig;
import com.potcounter.debug.DiagnosticsTracker;
import com.potcounter.detection.PotDetectionManager;
import com.potcounter.gui.MainConfigScreen;
import com.potcounter.sound.SoundManager;
import com.potcounter.tracker.PotTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PotCounterMod implements ClientModInitializer {
    public static final String MOD_ID = "potcounter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static PotCounterMod instance;

    private ConfigManager configManager;
    private SoundManager soundManager;
    private CombatManager combatManager;
    private PotTracker potTracker;
    private PotDetectionManager potDetectionManager;

    private KeyBinding configKeyBinding;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing Pot Counter Mod for Minecraft 1.21.11...");

        this.configManager = new ConfigManager();
        this.soundManager = new SoundManager(configManager.getConfig());
        this.combatManager = new CombatManager();
        this.potTracker = new PotTracker(combatManager, configManager.getConfig());
        this.potDetectionManager = new PotDetectionManager(potTracker, combatManager, soundManager);

        configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.potcounter.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.create(Identifier.of("potcounter", "category"))
        ));

        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (!isModActive()) return;
            if (configManager != null && potTracker != null) {
                com.potcounter.render.CounterHudRenderer.render(context, potTracker, configManager.getConfig());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKeyBinding != null) {
                while (configKeyBinding.wasPressed()) {
                    if (client.currentScreen == null) {
                        client.setScreen(new MainConfigScreen(null));
                    }
                }
            }

            if (!isModActive()) {
                return;
            }

            if (combatManager != null) {
                combatManager.tick();
            }

            if (potDetectionManager != null) {
                potDetectionManager.tick();
            }

            if (potTracker != null && client.world != null) {
                UUID localUuid = (client.player != null) ? client.player.getUuid() : null;
                potTracker.tick(localUuid, client.world);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info("Disconnected from server, resetting combat and pot tracking state.");
            combatManager.clear();
            potTracker.resetAll();
            potDetectionManager.clear();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("potcounter")
                    .executes(context -> {
                        context.getSource().getClient().send(() -> {
                            context.getSource().getClient().setScreen(new MainConfigScreen(null));
                        });
                        return 1;
                    })
                    .then(ClientCommandManager.literal("debug")
                            .executes(context -> {
                                MinecraftClient client = context.getSource().getClient();
                                DiagnosticsTracker.runDiagnostics(client, msg -> {
                                    if (client.inGameHud != null && client.inGameHud.getChatHud() != null) {
                                        client.inGameHud.getChatHud().addMessage(msg);
                                    }
                                });
                                return 1;
                            })
                    )
            );
        });

        LOGGER.info("Pot Counter Mod initialized successfully!");
    }

    public static PotCounterMod getInstance() {
        return instance;
    }

    public static boolean isM4LibLoaded() {
        try {
            net.fabricmc.loader.api.FabricLoader loader = net.fabricmc.loader.api.FabricLoader.getInstance();
            if (loader != null) {
                return loader.isModLoaded("m4lib");
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean isModActive() {
        if (!isM4LibLoaded()) {
            return false;
        }
        PotCounterMod mod = getInstance();
        if (mod == null) return false;
        CounterConfig config = mod.getConfig();
        return config != null && config.isActive();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CounterConfig getConfig() {
        return configManager.getConfig();
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public PotTracker getPotTracker() {
        return potTracker;
    }

    public PotDetectionManager getPotDetectionManager() {
        return potDetectionManager;
    }
}
