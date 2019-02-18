package com.simon816.chatui;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.simon816.chatui.group.ChatGroupFeature;
import com.simon816.chatui.lib.ChatUILib;
import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.config.LibConfig;
import com.simon816.chatui.lib.config.PlayerSettings;
import com.simon816.chatui.lib.event.CreatePlayerViewEvent;
import com.simon816.chatui.lib.event.PlayerChangeConfigEvent;
import com.simon816.chatui.pagination.TabbedPaginationService;
import com.simon816.chatui.privmsg.PrivateMessageFeature;
import com.simon816.chatui.tabs.Tab;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.pagination.PaginationService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Plugin(id = "chatui", name = "Chat UI", dependencies = @Dependency(id = "chatuilib"))
public class ChatUI {

    public static final String ADMIN_PERMISSON = "chatui.admin";

    private static ChatUI instance;

    private FeatureManager features;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> confLoader;

    @Inject
    private Logger logger;

    public static ChatUI instance() {
        return instance;
    }

    public static ActivePlayerChatView getActiveView(UUID playerUuid) {
        return getActiveView(ChatUILib.getView(playerUuid));
    }

    public static ActivePlayerChatView getActiveView(PlayerChatView view) {
        view = unwrapView(view);
        if (view instanceof ActivePlayerChatView) {
            return (ActivePlayerChatView) view;
        }
        throw new IllegalStateException("Not an active view");
    }

    public static PlayerChatView unwrapView(PlayerChatView view) {
        if (view instanceof ChatUIView) {
            view = ((ChatUIView) view).getActualView();
        }
        return view;
    }

    public static boolean isTabActive(PlayerChatView view, Tab tab) {
        return view instanceof ActivePlayerChatView && ((ActivePlayerChatView) view).getWindow().getActiveTab() == tab;
    }

    public static boolean isTabActive(PlayerChatView view, Predicate<Tab> test) {
        return view instanceof ActivePlayerChatView && test.test(((ActivePlayerChatView) view).getWindow().getActiveTab());
    }

    public FeatureManager getFeatureManager() {
        checkState(this.features != null, "Features can only be added in the INIT phase");
        return this.features;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        instance = this;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        Config.init(this.confLoader, this.logger);

        this.features = new FeatureManager();
        this.features.registerFeature(this, "privmsg", PrivateMessageFeature::new);
        this.features.registerFeature(this, "chatgroup", ChatGroupFeature::new);
    }

    @Listener(order = Order.POST)
    public void onPostInit(GamePostInitializationEvent event) {
        Optional<ProviderRegistration<PaginationService>> optService = Sponge.getGame().getServiceManager().getRegistration(PaginationService.class);
        if (optService.isPresent()) {
            PaginationService service = optService.get().getProvider();
            Sponge.getGame().getServiceManager().setProvider(this, PaginationService.class, new TabbedPaginationService(service));
        }
        this.features.load();
    }

    @Listener
    public void onCreateView(CreatePlayerViewEvent event) {
        Player player = event.getPlayer();
        event.setView(new ChatUIView(createView(player)));
    }

    private PlayerChatView createView(Player player) {
        ConfigurationNode chatUISettings = Config.playerConfig(player.getUniqueId());
        PlayerSettings playerSettings = LibConfig.playerConfig(player.getUniqueId());
        PlayerChatView view;
        if (!chatUISettings.getNode("enabled").getBoolean()) {
            view = new DisabledChatView(player);
        } else {
            view = new ActivePlayerChatView(player, playerSettings);
        }
        return view;
    }

    void reInit(Player player) {
        PlayerChatView view = createView(player);
        ((ChatUIView) ChatUILib.getView(player)).setView(view);
        view.update();
    }

    @Listener
    public void onConfigChange(PlayerChangeConfigEvent event) {
        PlayerChatView view = unwrapView(ChatUILib.getView(event.getPlayer()));
        if (view instanceof ActivePlayerChatView) {
            ((ActivePlayerChatView) view).setContextFromSettings(event.getPlayer(), event.getNewSettings());
            view.update();
        }
    }

    @Listener
    public void onGameReload(GameReloadEvent event) {
        Config.loadConfig();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        Config.saveConfig();
    }

}
