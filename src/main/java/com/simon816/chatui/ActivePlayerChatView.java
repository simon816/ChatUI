package com.simon816.chatui;

import com.google.common.base.Objects;
import com.simon816.chatui.impl.ImplementationConfig;
import com.simon816.chatui.tabs.GlobalTab;
import com.simon816.chatui.tabs.NewTab;
import com.simon816.chatui.tabs.PermissionsTab;
import com.simon816.chatui.tabs.SceneTab;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.tabs.TextBufferTab;
import com.simon816.chatui.tabs.TextFileTab;
import com.simon816.chatui.tabs.config.ConfigEditTab;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Optional;
import java.util.UUID;

public class ActivePlayerChatView implements PlayerChatView {

    private PlayerContext playerContext;
    private final Window window;
    private final TextBufferTab globalTab;
    private final NewTab newTab;
    boolean isUpdating;

    private final PlayerList playerList;

    ActivePlayerChatView(Player player, ConfigurationNode settings) {
        this.playerContext = new PlayerContext(player,
                settings.getNode("displayWidth").getInt(),
                settings.getNode("displayHeight").getInt(),
                settings.getNode("forceUnicode").getBoolean());
        this.window = new Window();
        this.window.addTab(this.globalTab = new GlobalTab(), true);
        this.newTab = new NewTab();
        this.playerList = new PlayerList(player);
        initNewTab(player);
        ChatUI.instance().loadFeatures(this);
    }

    @Override
    public PlayerList getPlayerList() {
        return this.playerList;
    }

    private void initNewTab(Player player) {
        this.newTab.addButton("Player List", new NewTab.LaunchTabAction(() -> new SceneTab(Text.of("Player List"), this.playerList.getRoot())));
        UUID uuid = player.getUniqueId();
        this.newTab.addButton("Settings", new NewTab.LaunchTabAction(() -> createSettingsTab(uuid)));
        if (player.hasPermission(ChatUI.ADMIN_PERMISSON)) {
            showNewTabAdminButtons();
        }
        if (DemoContent.ENABLE_DEMO) {
            this.newTab.addButton("Demo Content", new NewTab.LaunchTabAction(() -> DemoContent.TAB));
        }
    }

    private Tab createSettingsTab(UUID uuid) {
        ConfigurationNode config = Config.playerConfig(uuid);
        ConfigEditTab.Options opts = new ConfigEditTab.Options(false, true, false, "Settings");
        ConfigEditTab.ActionHandler handler = new ConfigEditTab.ActionHandler() {

            @Override
            public void onNodeChanged(ConfigurationNode node) {
                Config.saveConfig();
                onConfigChange(node);
            }
        };
        return new ConfigEditTab(config, Text.of("Settings"), opts, handler);
    }

    private void showNewTabAdminButtons() {
        if (ImplementationConfig.isSupported()) {
            this.newTab.addButton("Edit Config", new NewTab.LaunchTabAction(() -> new ConfigEditTab(ImplementationConfig.getRootNode(),
                    Text.of("Sponge Config"), ConfigEditTab.Options.DEFAULTS, ImplementationConfig.getHandler())));
        }
        this.newTab.addButton("Permissions", new NewTab.LaunchTabAction(() -> new PermissionsTab()));
    }

    @Override
    public Player getPlayer() {
        return this.playerContext.player;
    }

    public PlayerContext getContext() {
        return this.playerContext;
    }

    @Override
    public Window getWindow() {
        return this.window;
    }

    @Override
    public NewTab getNewTab() {
        return this.newTab;
    }

    @Override
    public void update() {
        this.isUpdating = true;
        this.playerContext.player.sendMessage(this.window.draw(this.playerContext));
        this.isUpdating = false;
    }

    @Override
    public boolean handleIncoming(Text message) {
        try {
            this.window.getActiveTab().onTextEntered(this, message);
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
        if (cmd.equals("settab")) {
            this.window.setTab(Integer.parseInt(args[1]));
        } else if (cmd.equals("closetab")) {
            this.window.removeTab(Integer.parseInt(args[1]));
        } else if (cmd.equals("newtab")) {
            this.window.addTab(this.newTab, true);
        } else if (cmd.equals("tf") && this.window.getActiveTab() instanceof TextFileTab) {
            // TextFileTab specific behaviour, see TextFileTab#clickAction
            ((TextFileTab) this.window.getActiveTab()).onCommand(this, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else {
            return false;
        }
        update();
        return true;
    }

    public void onConfigChange(ConfigurationNode node) {
        if (node.getKey().equals("displayWidth")) {
            this.playerContext = this.playerContext.withWidth(node.getInt());
        } else if (node.getKey().equals("displayHeight")) {
            this.playerContext = this.playerContext.withHeight(node.getInt());
        } else if (node.getKey().equals("forceUnicode")) {
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
            this.playerContext.player.sendMessage(Text.NEW_LINE);
        }
        this.isUpdating = false;
        ChatUI.instance().initialize(getPlayer());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("player", this.playerContext)
                .add("window", this.window)
                .toString();
    }

}
