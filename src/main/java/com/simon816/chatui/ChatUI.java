package com.simon816.chatui;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.simon816.chatui.group.ChatGroupFeature;
import com.simon816.chatui.pagination.TabbedPaginationService;
import com.simon816.chatui.privmsg.PrivateMessageFeature;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.bstats.MetricsLite;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.ClickAction.ExecuteCallback;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Plugin(id = "chatui", name = "Chat UI")
public class ChatUI {

    public static final String ADMIN_PERMISSON = "chatui.admin";

    private final Map<UUID, PlayerChatView> playerViewMap = Maps.newHashMap();
    private final List<AbstractFeature> features = Lists.newArrayList();

    private static ChatUI instance;

    private boolean enabledAsService = false;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> confLoader;

    @Inject
    private Logger logger;

    @Inject
    private Injector injector;

    public static ChatUI instance() {
        return instance;
    }

    public static PlayerChatView getView(CommandSource source) {
        checkArgument(source instanceof Player);
        return getView((Player) source);
    }

    public static PlayerChatView getView(Player player) {
        return getView(player.getUniqueId());
    }

    public static PlayerChatView getView(UUID uuid) {
        return instance.playerViewMap.get(uuid);
    }

    public static ActivePlayerChatView getActiveView(CommandSource source) {
        return (ActivePlayerChatView) getView(source);
    }

    public static ActivePlayerChatView getActiveView(UUID uuid) {
        return (ActivePlayerChatView) getView(uuid);
    }

    public static ClickAction<?> command(String subcommand) {
        return TextActions.runCommand("/chatui " + subcommand);
    }

    // Route all callbacks through here so it will be easy to make changes in
    // the future
    public static ExecuteCallback execClick(Consumer<CommandSource> handler) {
        return TextActions.executeCallback(handler);
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        instance = this;
        try {
            this.injector.getInstance(MetricsLite.class);
        } catch (ExceptionInInitializerError e) {
            // in development mode - metrics class is not relocated
        }
    }

    private Map<String, Supplier<AbstractFeature>> featuresToLoad;

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getGame().getCommandManager().register(this, new ChatUICommand(), "chatui");
        Config.init(this.confLoader, this.logger);

        this.enabledAsService = !Config.getRootNode().getNode("interface-enabled").getBoolean(true);

        if (this.enabledAsService) {
            return;
        }

        this.featuresToLoad = Maps.newHashMap();

        this.registerFeature(this, "privmsg", PrivateMessageFeature::new);
        this.registerFeature(this, "chatgroup", ChatGroupFeature::new);
    }

    public boolean isServiceOnlyMode() {
        return this.enabledAsService;
    }

    public void registerFeature(Object plugin, String id, Supplier<AbstractFeature> featureLoader) {
        checkState(this.featuresToLoad != null, "Not accepting new features to be registered");
        String featureId = Sponge.getPluginManager().fromInstance(plugin).get().getId() + ":" + id;
        this.featuresToLoad.putIfAbsent(featureId, featureLoader);
    }

    @Listener(order = Order.POST)
    public void onPostInit(GamePostInitializationEvent event) {
        if (this.enabledAsService) {
            return;
        }
        Optional<ProviderRegistration<PaginationService>> optService = Sponge.getGame().getServiceManager().getRegistration(PaginationService.class);
        if (!optService.isPresent()) {
            return;
        }
        PaginationService service = optService.get().getProvider();
        Sponge.getGame().getServiceManager().setProvider(this, PaginationService.class, new TabbedPaginationService(service));

        for (Entry<String, Supplier<AbstractFeature>> entry : this.featuresToLoad.entrySet()) {
            if (canLoad(entry.getKey())) {
                AbstractFeature feature = entry.getValue().get();
                feature.setConfigRoot(featureConfig(entry.getKey()));
                feature.onInit();
                this.features.add(feature);
            }
        }
        this.featuresToLoad = null;
    }

    private ConfigurationNode featureConfig(String featureId) {
        ConfigurationNode config = Config.getRootNode().getNode("features", featureId, "config");
        if (config.isVirtual()) {
            config.setValue(Collections.emptyMap());
        }
        return config;
    }

    private boolean canLoad(String featureId) {
        ConfigurationNode enabled = Config.getRootNode().getNode("features", featureId, "enabled");
        if (enabled.isVirtual()) {
            enabled.setValue(true);
        }
        return enabled.getBoolean(true);
    }

    @Listener
    public void onGameReload(GameReloadEvent event) {
        Config.loadConfig();
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        if (this.enabledAsService) {
            this.playerViewMap.put(event.getTargetEntity().getUniqueId(), new ExternalServiceView(event.getTargetEntity()));
            return;
        }
        initialize(event.getTargetEntity());
    }

    void initialize(Player player) {
        ConfigurationNode playerSettings = Config.playerConfig(player.getUniqueId());
        PlayerChatView view;
        if (!playerSettings.getNode("enabled").getBoolean()) {
            view = new DisabledChatView(player);
        } else {
            view = new ActivePlayerChatView(player, playerSettings);
        }
        PlayerChatView oldView = this.playerViewMap.put(player.getUniqueId(), view);
        if (oldView != null) {
            oldView.onRemove();
        }
        for (AbstractFeature feature : this.features) {
            if (oldView != null) {
                feature.onViewClose(oldView);
            }
            feature.onNewPlayerView(view);
        }
        view.update();
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        PlayerChatView view = this.playerViewMap.remove(event.getTargetEntity().getUniqueId());
        Config.saveConfig();
        if (!this.enabledAsService) {
            for (AbstractFeature feature : this.features) {
                feature.onViewClose(view);
            }
        }
        view.onRemove();
        // TODO Offline message buffering?
    }

    // Pretty aggressively captures incoming chat
    @Listener(order = Order.PRE, beforeModifications = true)
    @IsCancelled(Tristate.UNDEFINED)
    public void onIncomingMessage(MessageChannelEvent.Chat event, @Root Player player) {
        if (getView(player).handleIncoming(event.getRawMessage())) {
            // No plugins should interpret this as chat because the processing
            // is for tab-specific behaviour
            event.setCancelled(true);
            event.setChannel(MessageChannel.TO_NONE);
        }
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        Config.saveConfig();
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

}
