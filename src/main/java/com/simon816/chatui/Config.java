package com.simon816.chatui;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class Config {

    public static final int DEFAULT_BUFFER_HEIGHT = 20;
    public static final int DEFAULT_BUFFER_WIDTH = 320;

    private static ConfigurationLoader<CommentedConfigurationNode> confLoader;

    private static Logger logger;
    private static CommentedConfigurationNode config;

    private static final Map<String, Object> DEFAULTS;
    static {
        DEFAULTS = Maps.newHashMap();
        DEFAULTS.put("enabled", true);
        DEFAULTS.put("display-width", DEFAULT_BUFFER_WIDTH);
        DEFAULTS.put("display-height", DEFAULT_BUFFER_HEIGHT);
        DEFAULTS.put("force-unicode", false);
    }

    public static void init(ConfigurationLoader<CommentedConfigurationNode> confLoader, Logger logger) {
        Config.confLoader = confLoader;
        Config.logger = logger;
        loadConfig();
    }

    public static ConfigurationNode playerConfig(UUID uuid) {
        CommentedConfigurationNode settings = config.getNode("player-settings", uuid);
        if (settings.isVirtual()) {
            settings.setValue(DEFAULTS);
        } else {
            for (Entry<String, Object> e : DEFAULTS.entrySet()) {
                CommentedConfigurationNode n = settings.getNode(e.getKey());
                if (n.isVirtual()) {
                    n.setValue(e.getValue());
                }
            }
        }
        return settings;
    }

    public static ConfigurationNode getRootNode() {
        return config;
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
        CommentedConfigurationNode enabled = config.getNode("interface-enabled");
        if (enabled.isVirtual()) {
            enabled.setValue(true);
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
