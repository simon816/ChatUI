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
    private final Player player;
    private final Window window;
    private final TextBufferTab globalTab;
    boolean isUpdating;

    private final MessagePipeline incomingPipeline = new MessagePipeline();
    private final MessagePipeline outgoingPipeline = new MessagePipeline();

    public PlayerChatView(Player player) {
        this.player = player;
        this.window = new Window();
        this.window.addTab(this.globalTab = new GlobalTab(player), true);

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
        return this.player;
    }

    public Window getWindow() {
        return this.window;
    }

    public void update() {
        this.isUpdating = true;
        this.player.sendMessage(this.window.draw(PLAYER_BUFFER_HEIGHT));
        this.isUpdating = false;
    }

    public MessagePipeline getIncomingPipeline() {
        return this.incomingPipeline;
    }

    public MessagePipeline getOutgoingPipeline() {
        return this.outgoingPipeline;
    }

    public boolean handleIncoming(Text message) {
        return this.incomingPipeline.process(message, this.player);
    }

    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type) {
        if (this.isUpdating) {
            return Optional.of(originalOutgoing);
        }
        if (this.outgoingPipeline.process(originalOutgoing, sender)) {
            return Optional.of(this.window.draw(PLAYER_BUFFER_HEIGHT));
        }
        return Optional.empty();
    }

    boolean handleCommand(String[] args) {
        if (args[0].equals("settab")) {
            this.window.setTab(Integer.parseInt(args[1]));
        } else if (args[0].endsWith("closetab")) {
            this.window.removeTab(Integer.parseInt(args[1]));
        } else if (args[0].endsWith("newtab")) {
            this.window.addTab(new NewTab(), true);
        } else {
            return false;
        }
        update();
        return true;
    }

}
