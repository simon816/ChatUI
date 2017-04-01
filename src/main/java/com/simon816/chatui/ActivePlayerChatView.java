package com.simon816.chatui;

import com.google.common.base.Objects;
import com.simon816.chatui.impl.ImplementationConfig;
import com.simon816.chatui.tabs.GlobalTab;
import com.simon816.chatui.tabs.NewTab;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.tabs.TextBufferTab;
import com.simon816.chatui.tabs.config.ConfigEditTab;
import com.simon816.chatui.tabs.perm.PermissionsTab;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Optional;
import java.util.UUID;

public class ActivePlayerChatView implements PlayerChatView {

    private static final String PERM_CONFIG = ChatUI.ADMIN_PERMISSON + ".config";
    private static final String PERM_PERMISSIONS = ChatUI.ADMIN_PERMISSON + ".permissions";

    private PlayerContext playerContext;
    private final Window window;
    private final TextBufferTab globalTab;
    private final NewTab newTab;
    boolean isUpdating;

    private final PlayerList playerList;

    ActivePlayerChatView(Player player, ConfigurationNode settings) {
        this.playerContext = new PlayerContext(player,
                settings.getNode("display-width").getInt(),
                settings.getNode("display-height").getInt(),
                settings.getNode("force-unicode").getBoolean());
        this.window = new Window();
        this.window.addTab(this.globalTab = new GlobalTab(), true);
        this.newTab = new NewTab();
        this.playerList = new PlayerList(player);
        initNewTab(player);
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    private void initNewTab(Player player) {
        this.newTab.addButton("Player List", new NewTab.LaunchTabAction(() -> new Tab(Text.of("Player List"), this.playerList.getRoot())));
        if (ImplementationConfig.isSupported()) {
            if (player.hasPermission(PERM_CONFIG)) {
                ConfigEditTab.Options opts = new ConfigEditTab.Options(
                        player.hasPermission(PERM_CONFIG + ".add"),
                        player.hasPermission(PERM_CONFIG + ".edit"),
                        player.hasPermission(PERM_CONFIG + ".delete"), null);
                this.newTab.addButton("Edit Config", new NewTab.LaunchTabAction(() -> new ConfigEditTab(ImplementationConfig.getRootNode(),
                        ImplementationConfig.getTitle(), opts, ImplementationConfig.getHandler())));
            }
        }
        if (player.hasPermission(PERM_PERMISSIONS)) {
            Optional<PermissionService> optService = Sponge.getServiceManager().provide(PermissionService.class);
            if (optService.isPresent()) {
                this.newTab.addButton("Permissions", new NewTab.LaunchTabAction(() -> new PermissionsTab(optService.get())));
            }
        }
        UUID uuid = player.getUniqueId();
        this.newTab.addButton("Settings", new NewTab.LaunchTabAction(() -> createSettingsTab(uuid)));
    }

    private Tab createSettingsTab(UUID uuid) {
        ConfigurationNode config = Config.playerConfig(uuid);
        ConfigEditTab.Options opts = new ConfigEditTab.Options(false, true, false, "Settings");
        ConfigEditTab.ActionHandler handler = new ConfigEditTab.ActionHandler() {

            @Override
            public void onNodeChanged(ConfigEditTab tab, ConfigurationNode node) {
                onConfigChange(node);
                Config.saveConfig();
            }
        };
        return new ConfigEditTab(config, Text.of("Settings"), opts, handler);
    }

    @Override
    public Player getPlayer() {
        return this.playerContext.getPlayer();
    }

    public PlayerContext getContext() {
        return this.playerContext;
    }

    @Override
    public Window getWindow() {
        return this.window;
    }

    public NewTab getNewTab() {
        return this.newTab;
    }

    @Override
    public void update() {
        this.isUpdating = true;
        this.playerContext.getPlayer().sendMessage(this.window.draw(this.playerContext));
        this.isUpdating = false;
    }

    @Override
    public boolean handleIncoming(Text message) {
        try {
            this.window.onTextInput(this, message);
            // Only allow normal chat to get processed if on the global tab
            return this.window.getActiveTab() != this.globalTab;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Just ignore the input
        }
    }

    @Override
    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type) {
        if (this.isUpdating) {
            // Send it straight out
            return Optional.of(originalOutgoing);
        }
        this.globalTab.appendMessage(originalOutgoing);
        return Optional.of(this.window.draw(this.playerContext));
    }

    @Override
    public boolean handleCommand(String[] args) {
        String cmd = args[0];
        if (cmd.equals("disable")) {
            Config.playerConfig(getPlayer().getUniqueId()).getNode("enabled").setValue(false);
            disable();
        } else if (cmd.equals("newtab")) {
            this.window.addTab(this.newTab, true);
        } else if (this.window.onCommand(this, args)) {
        } else {
            return false;
        }
        update();
        return true;
    }

    public void onConfigChange(ConfigurationNode node) {
        if (node.getKey().equals("display-width")) {
            this.playerContext = this.playerContext.withWidth(node.getInt());
        } else if (node.getKey().equals("display-height")) {
            this.playerContext = this.playerContext.withHeight(node.getInt());
        } else if (node.getKey().equals("force-unicode")) {
            this.playerContext = this.playerContext.withUnicode(node.getBoolean());
        } else if (node.getKey().equals("enabled")) {
            if (!node.getBoolean()) {
                disable();
            }
        }
    }

    private void disable() {
        // Force clear the screen
        this.isUpdating = true;
        for (int i = 0; i < this.playerContext.height; i++) {
            this.playerContext.getPlayer().sendMessage(Text.NEW_LINE);
        }
        ChatUI.instance().initialize(getPlayer());
        this.isUpdating = false;
    }

    @Override
    public void onRemove() {
        this.window.onClose();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("player", this.playerContext)
                .add("window", this.window)
                .toString();
    }

}
