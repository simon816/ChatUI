package com.simon816.chatui.lib.config;

import com.simon816.chatui.lib.event.PlayerChangeConfigEvent;
import com.simon816.chatui.util.FontData;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

public class LibConfig {

    private static ConfigurationLoader<CommentedConfigurationNode> confLoader;

    private static Logger logger;
    private static CommentedConfigurationNode config;
    private static boolean dirty;

    private static ObjectMapper<PlayerSettings> settingsMapper;

    private static boolean useLanguagePack;
    private static FontData defaultFontData;

    public static CommandCallable createCommand() {
        return CommandSpec.builder()
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.integer(Text.of("width")))
                        .executor((src, args) -> {
                            updatePlayer(config(src).withWidth(args.<Integer>getOne("width").get()), src);
                            saveConfig();
                            src.sendMessage(Text.of("Width setting changed"));
                            return CommandResult.success();
                        })
                        .build(), "width")
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.integer(Text.of("height")))
                        .executor((src, args) -> {
                            updatePlayer(config(src).withHeight(args.<Integer>getOne("height").get()), src);
                            saveConfig();
                            src.sendMessage(Text.of("Height setting changed"));
                            return CommandResult.success();
                        })
                        .build(), "height")
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.bool(Text.of("unicode")))
                        .executor((src, args) -> {
                            updatePlayer(config(src).withUnicode(args.<Boolean>getOne("unicode").get()), src);
                            saveConfig();
                            src.sendMessage(Text.of("Force Unicode setting changed"));
                            return CommandResult.success();
                        })
                        .build(), "unicode")
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.optional(GenericArguments.string(Text.of("font data"))))
                        .executor((src, args) -> {
                            updatePlayer(config(src).withFontData(args.<String>getOne("font data").orElse(null)), src);
                            saveConfig();
                            src.sendMessage(Text.of("Font Data setting changed"));
                            return CommandResult.success();
                        }).build(), "font")
                .executor((src, args) -> {
                    PlayerSettings settings = config(src);
                    src.sendMessages(Text.of("Settings:"),
                            Text.of("Width: " + settings.getWidth()),
                            Text.of("Height: " + settings.getHeight()),
                            Text.of("Force Unicode: " + settings.getForceUnicode()),
                            Text.of("Font data: " + (settings.getFontData() == null ? "vanilla" : "custom")));
                    return CommandResult.success();
                })
                .build();
    }

    private static PlayerSettings config(CommandSource src) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("source must be a player"));
        }
        return playerConfig(((Player) src).getUniqueId());
    }

    public static void init(ConfigurationLoader<CommentedConfigurationNode> confLoader, Logger logger) {
        LibConfig.confLoader = confLoader;
        LibConfig.logger = logger;
        loadConfig();
    }

    public static void loadConfig() {
        try {
            settingsMapper = ObjectMapper.forClass(PlayerSettings.class);
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
        try {
            config = confLoader.load();
        } catch (IOException e) {
            logger.error("Error while loading config. Resetting to default config.", e);
            config = confLoader.createEmptyNode();
        }
        dirty = false;

        CommentedConfigurationNode playerSettings = config.getNode("player-settings");
        if (playerSettings.isVirtual()) {
            playerSettings.setValue(Collections.emptyMap());
            dirty = true;
        }

        CommentedConfigurationNode useLanguagePack = config.getNode("use-language-pack");
        if (useLanguagePack.isVirtual()) {
            useLanguagePack.setValue(false);
            dirty = true;
        }
        LibConfig.useLanguagePack = useLanguagePack.getBoolean();

        CommentedConfigurationNode defaultFontNode = config.getNode("default-font");
        if (defaultFontNode.isVirtual()) {
            defaultFontNode.setValue("");
            dirty = true;
        }

        String defaultFontString = defaultFontNode.getString();
        try {
            FontData.checkValid(defaultFontString);
        } catch (IllegalArgumentException e) {
            logger.warn("Default font is invalid, falling back to vanilla font", e);
            defaultFontString = null;
        }
        defaultFontData = FontData.fromString(defaultFontString, FontData.VANILLA);

        saveConfig();
    }

    public static boolean useLanguagePack() {
        return useLanguagePack;
    }

    public static FontData defaultFontData() {
        return defaultFontData;
    }

    public static void saveConfig() {
        if (!dirty) {
            return;
        }
        try {
            confLoader.save(config);
            dirty = false;
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    public static PlayerSettings playerConfig(UUID uuid) {
        CommentedConfigurationNode settings = config.getNode("player-settings", uuid.toString());
        try {
            // Save defaults to config file
            dirty |= settings.isVirtual();
            return settingsMapper.bindToNew().populate(settings);
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updatePlayer(PlayerSettings settings, CommandSource src) {
        updatePlayer(settings, (Player) src);
    }

    public static void updatePlayer(PlayerSettings settings, Player player) {
        try {
            UUID uuid = player.getUniqueId();
            PlayerSettings oldSettings = playerConfig(uuid);
            if (settings.equals(oldSettings)) {
                return;
            }
            settingsMapper.bind(settings).serialize(config.getNode("player-settings", uuid.toString()));
            dirty = true;
            Sponge.getEventManager()
                    .post(new PlayerChangeConfigEvent(player, oldSettings, settings, Sponge.getCauseStackManager().getCurrentCause()));
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

}
