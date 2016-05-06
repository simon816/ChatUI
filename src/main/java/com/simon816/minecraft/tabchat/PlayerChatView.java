package com.simon816.minecraft.tabchat;

import com.simon816.minecraft.tabchat.tabs.GlobalTab;
import com.simon816.minecraft.tabchat.tabs.NewTab;
import com.simon816.minecraft.tabchat.tabs.TextBufferTab;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Optional;

public class PlayerChatView {

    public static final int PLAYER_BUFFER_HEIGHT = 20;
    public static final int PLAYER_BUFFER_WIDTH = 320;

    private final PlayerContext playerContext;
    private final Window window;
    private final TextBufferTab globalTab;
    boolean isUpdating;

    private final MessagePipeline incomingPipeline = new MessagePipeline();
    private final MessagePipeline outgoingPipeline = new MessagePipeline();

    public PlayerChatView(Player player) {
        this.playerContext = new PlayerContext(player, PLAYER_BUFFER_WIDTH, PLAYER_BUFFER_HEIGHT);
        this.window = new Window();
        this.window.addTab(this.globalTab = new GlobalTab(), true);

        this.outgoingPipeline.addHandler((message, sender) -> {
            this.globalTab.appendMessage(message);
            return true;
        });
        this.incomingPipeline.addHandler((message, sender) -> {
            // Only allow normal chat to get processed if on the global tab
            return this.window.getActiveTab() != this.globalTab;
        });
        TabbedChat.instance().loadFeatures(this);
    }

    public Player getPlayer() {
        return this.playerContext.player;
    }

    public Window getWindow() {
        return this.window;
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
        return this.incomingPipeline.process(message, this.playerContext.player);
    }

    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type) {
        if (this.isUpdating) {
            return Optional.of(originalOutgoing);
        }
        if (this.outgoingPipeline.process(originalOutgoing, sender)) {
            return Optional.of(this.window.draw(this.playerContext));
        }
        return Optional.empty();
    }

    boolean handleCommand(String[] args) {
        String cmd = args[0];
        if (cmd.equals("settab")) {
            this.window.setTab(Integer.parseInt(args[1]));
        } else if (cmd.equals("closetab")) {
            this.window.removeTab(Integer.parseInt(args[1]));
        } else if (cmd.equals("newtab")) {
            this.window.addTab(new NewTab(), true);
        } else if (cmd.equals("tableft")) {
            this.window.shiftLeft();
        } else if (cmd.equals("tabright")) {
            this.window.shiftRight();
        } else {
            return false;
        }
        update();
        return true;
    }

}
