package com.simon816.chatui;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
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

    public static ConfigurationNode playerConfig(UUID uuid) {
        CommentedConfigurationNode settings = config.getNode("playerSettings", uuid);
        if (settings.isVirtual()) {
            Map<String, Object> defaults = Maps.newHashMap();
            defaults.put("enabled", true);
            defaults.put("displayWidth", PlayerChatView.DEFAULT_BUFFER_WIDTH);
            defaults.put("displayHeight", PlayerChatView.DEFAULT_BUFFER_HEIGHT);
            settings.setValue(defaults);
        }
        return settings;
    }

    public static void loadConfig() {
        try {
            config = confLoader.load();
        } catch (IOException e) {
            logger.error("Error while loading config", e);
            config = confLoader.createEmptyNode();
        }
        CommentedConfigurationNode playerSettings = config.getNode("playerSettings");
        if (playerSettings.isVirtual()) {
            playerSettings.setValue(Collections.EMPTY_MAP);
        }
    }

    public static void saveConfig() {
        try {
            confLoader.save(config);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

}
