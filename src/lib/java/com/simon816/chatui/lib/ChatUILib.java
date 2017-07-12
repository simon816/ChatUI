package com.simon816.chatui.lib;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.simon816.chatui.lib.config.LibConfig;
import com.simon816.chatui.lib.event.CreatePlayerViewEvent;
import com.simon816.chatui.lib.event.PlayerChangeConfigEvent;
import com.simon816.chatui.lib.internal.ClickCallback;
import com.simon816.chatui.lib.internal.WrapOutputChannel;
import com.simon816.chatui.lib.lang.LanguagePackManager;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandNotFoundException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Plugin(id = "chatuilib", name = "Chat UI Library")
public class ChatUILib {

    private static ChatUILib instance;

    private final Map<UUID, PlayerChatView> playerViewMap = Maps.newHashMap();

    private LanguagePackManager languageManager;

    @Inject
    private Logger logger;

    /* Public API utility functions */

    public static PlayerChatView getView(UUID uuid) {
        return instance.playerViewMap.get(uuid);
    }

    public static PlayerChatView getView(Player player) {
        return getView(player.getUniqueId());
    }

    public static PlayerChatView getView(CommandSource source) {
        checkArgument(source instanceof Player);
        return getView((Player) source);
    }

    public static ClickAction<?> command(String subcommand) {
        return TextActions.runCommand("/chatui " + subcommand);
    }

    public static ChatUILib getInstance() {
        return instance;
    }
    /* Plugin event listeners (internal) */

    public LanguagePackManager getLanguageManager() {
        return this.languageManager;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        instance = this;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        Path confDir = Sponge.getGame().getConfigManager().getSharedConfig(this).getDirectory().resolve("chatui");
        try {
            Files.createDirectories(confDir);
        } catch (IOException e) {
            this.logger.error("Failed to create configuration directory at {}", confDir, e);
        }
        Path confFile = confDir.resolve("preferences.conf");

        HoconConfigurationLoader confLoader = HoconConfigurationLoader.builder().setPath(confFile).build();
        LibConfig.init(confLoader, this.logger);
        this.languageManager = new LanguagePackManager(confDir);
        if (LibConfig.useLanguagePack()) {
            this.languageManager.fetch(this.logger);
        }

        Text argParam = Text.of("args");
        CommandSpec cmd = CommandSpec.builder()
                .child(LibConfig.createCommand(), "config")
                .child(ClickCallback.createCommand(), "exec")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!(src instanceof Player)) {
                                throw new CommandException(Text.of("source must be a player"));
                            }
                            getView(src).handleIncoming(Text.EMPTY);
                            return CommandResult.success();
                        }).build(), "empty")
                .arguments(GenericArguments.remainingRawJoinedStrings(argParam))
                .executor((src, ctx) -> {
                    if (!(src instanceof Player)) {
                        throw new CommandException(Text.of("Source must be player"));
                    }
                    String arguments = ctx.<String>getOne(argParam).get();
                    String[] args = arguments.split("\\s+");
                    if (ChatUILib.getView(src).handleCommand(args)) {
                        return CommandResult.success();
                    }
                    throw new CommandNotFoundException(arguments);
                })
                .description(Text.of("Internal Chat UI commands"))
                .build();
        Sponge.getGame().getCommandManager().register(this, cmd, "chatui");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        CreatePlayerViewEvent createEvent = new CreatePlayerViewEvent(new DefaultChatView(player), player, Cause.source(this).build());
        Sponge.getEventManager().post(createEvent);
        this.playerViewMap.put(player.getUniqueId(), createEvent.getView());
        createEvent.getView().initialize();
        this.languageManager.incrementLocale(player.getLocale());
    }

    @Listener
    public void onConfigChange(PlayerChangeConfigEvent event) {
        PlayerChatView view = getView(event.getPlayer());
        if (view instanceof DefaultChatView) {
            ((DefaultChatView) view).updateContext(event.getNewSettings().createContext(event.getPlayer()));
        }
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        PlayerChatView view = this.playerViewMap.remove(event.getTargetEntity().getUniqueId());
        view.onRemove();
        LibConfig.saveConfig();
        this.languageManager.decrementLocale(event.getTargetEntity().getLocale());
    }

    @Listener
    public void onPlayerChangeClientSettings(PlayerChangeClientSettingsEvent event) {
        Locale oldLocale = event.getTargetEntity().getLocale();
        Locale newLocale = event.getLocale();
        if (oldLocale.equals(newLocale)) {
            return;
        }
        this.languageManager.decrementLocale(oldLocale);
        this.languageManager.incrementLocale(newLocale);
    }

    // Pretty aggressively captures incoming chat
    @Listener(order = Order.PRE, beforeModifications = true)
    @IsCancelled(Tristate.UNDEFINED)
    public void onIncomingMessage(MessageChannelEvent.Chat event, @Root Player player) {
        if (getView(player).handleIncoming(event.getRawMessage())) {
            // No plugins should interpret this as chat
            event.setCancelled(true);
            event.setChannel(MessageChannel.TO_NONE);
        }
    }

    // Try to be last so we can wrap around any messages sent
    @Listener(order = Order.POST)
    public void onOutgoingMessage(MessageChannelEvent event) {
        if (!event.getChannel().isPresent() || event.isMessageCancelled()) {
            return;
        }
        CommandSource source = null;
        Object rootCause = event.getCause().root();
        if (rootCause instanceof CommandSource) {
            source = (CommandSource) rootCause;
        }
        event.setChannel(new WrapOutputChannel(event.getChannel().get(), source));
    }

    @Listener
    public void onGameReload(GameReloadEvent event) {
        LibConfig.loadConfig();
        if (LibConfig.useLanguagePack()) {
            this.languageManager.fetch(this.logger);
        }
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        LibConfig.saveConfig();
    }

}
