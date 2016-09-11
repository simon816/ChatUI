package com.simon816.chatui;

import com.google.common.base.Objects;
import com.simon816.chatui.tabs.GlobalTab;
import com.simon816.chatui.tabs.NewTab;
import com.simon816.chatui.tabs.PermissionsTab;
import com.simon816.chatui.tabs.PlayerListTab;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.tabs.TextBufferTab;
import com.simon816.chatui.tabs.config.ConfigEditTab;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;
import java.util.UUID;

public class PlayerChatView {

    public static final int DEFAULT_BUFFER_HEIGHT = 20;
    public static final int DEFAULT_BUFFER_WIDTH = 320;

    private PlayerContext playerContext;
    private final Window window;
    private final TextBufferTab globalTab;
    private final NewTab newTab;
    boolean isUpdating;

    private final MessagePipeline incomingPipeline = new MessagePipeline();
    private final MessagePipeline outgoingPipeline = new MessagePipeline();
    private final PlayerListTab playerListTab;

    PlayerChatView(Player player, ConfigurationNode settings) {
        this.playerContext = new PlayerContext(player, settings.getNode("displayWidth").getInt(), settings.getNode("displayHeight").getInt());
        this.window = new Window();
        this.window.addTab(this.globalTab = new GlobalTab(), true);
        this.newTab = new NewTab();
        this.playerListTab = new PlayerListTab(player);
        initNewTab(player);

        this.outgoingPipeline.addHandler((message, sender) -> {
            this.globalTab.appendMessage(message);
            return true;
        });
        this.incomingPipeline.addHandler((message, sender) -> {
            this.window.getActiveTab().onTextEntered(this, message);
            // Only allow normal chat to get processed if on the global tab
            return this.window.getActiveTab() != this.globalTab;
        });
        ChatUI.instance().loadFeatures(this);
    }

    public PlayerListTab getPlayerListTab() {
        return this.playerListTab;
    }

    private void initNewTab(Player player) {
        this.newTab.addButton(new NewTab.LaunchTabButton("Player List", () -> this.playerListTab));
        UUID uuid = player.getUniqueId();
        this.newTab.addButton(new NewTab.LaunchTabButton("Settings", () -> createSettingsTab(uuid)));
        if (player.hasPermission(ChatUI.ADMIN_PERMISSON)) {
            showNewTabAdminButtons();
        }
        if (DemoContent.ENABLE_DEMO) {
            this.newTab.addButton(new NewTab.LaunchTabButton("Demo Content", () -> DemoContent.TAB));
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
        if (Sponge.getPlatform().getImplementation().getId().equals("sponge")) {
            ConfigEditTab.ActionHandler handler = new ConfigEditTab.ActionHandler() {

                private void save() {
                    SpongeImpl.getGlobalConfig().save();
                }

                @Override
                public void onNodeAdded(ConfigurationNode node) {
                    save();
                }

                @Override
                public void onNodeChanged(ConfigurationNode node) {
                    save();
                }

                @Override
                public void onNodeRemoved(Object key) {
                    save();
                }
            };
            this.newTab.addButton(new NewTab.LaunchTabButton("Edit Config", () -> new ConfigEditTab(SpongeImpl.getGlobalConfig().getRootNode(),
                    Text.of("Sponge Config"), ConfigEditTab.Options.DEFAULTS, handler)));
        }
        this.newTab.addButton(new NewTab.LaunchTabButton("Permissions", () -> new PermissionsTab()));
    }

    public Player getPlayer() {
        return this.playerContext.player;
    }

    public Window getWindow() {
        return this.window;
    }

    public NewTab getNewTab() {
        return this.newTab;
    }

    public void update() {
        this.isUpdating = true;
        this.playerContext.player.sendMessage(this.window.draw(this.playerContext));
        this.isUpdating = false;
    }

    public MessagePipeline getIncomingPipeline() {
        return this.incomingPipeline;
    }

    public MessagePipeline getOutgoingPipeline() {
        return this.outgoingPipeline;
    }

    public boolean handleIncoming(Text message) {
        try {
            return this.incomingPipeline.process(message, this.playerContext.player);
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Just ignore the input
        }
    }

    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type) {
        if (this.isUpdating) {
            // Send it straight out
            return Optional.of(originalOutgoing);
        }
        // pump the text through the outgoing pipeline and redraw
        if (this.outgoingPipeline.process(originalOutgoing, sender)) {
            return Optional.of(this.window.draw(this.playerContext));
        }
        // Text ignored
        return Optional.empty();
    }

    boolean handleCommand(String[] args) {
        String cmd = args[0];
        if (cmd.equals("settab")) {
            this.window.setTab(Integer.parseInt(args[1]));
        } else if (cmd.equals("closetab")) {
            this.window.removeTab(Integer.parseInt(args[1]));
        } else if (cmd.equals("newtab")) {
            this.window.addTab(this.newTab, true);
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
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("player", this.playerContext)
                .add("window", this.window)
                .toString();
    }

}
