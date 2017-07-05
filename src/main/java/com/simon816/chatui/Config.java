package com.simon816.chatui;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

public class Config {

    private static ConfigurationLoader<CommentedConfigurationNode> confLoader;

    private static Logger logger;
    private static CommentedConfigurationNode config;

    public static void init(ConfigurationLoader<CommentedConfigurationNode> confLoader, Logger logger) {
        Config.confLoader = confLoader;
        Config.logger = logger;
        loadConfig();
    }

    public static void loadConfig() {
        try {
            config = confLoader.load();
        } catch (IOException e) {
            logger.error("Error while loading config", e);
            config = confLoader.createEmptyNode();
        }
        CommentedConfigurationNode playerSettings = config.getNode("player-settings");
        if (playerSettings.isVirtual()) {
            playerSettings.setValue(Collections.emptyMap());
        }
    }

    public static void saveConfig() {
        try {
            confLoader.save(config);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    public static ConfigurationNode playerConfig(UUID uuid) {
        CommentedConfigurationNode settings = config.getNode("player-settings", uuid.toString());
        if (settings.isVirtual()) {
            settings.getNode("enabled").setValue(true);
        }
        return settings;
    }

    public static ConfigurationNode getRootNode() {
        return config;
    }
}
