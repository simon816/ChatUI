package com.simon816.minecraft.tabchat;

import com.google.common.base.Objects;
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
            this.window.getActiveTab().onTextEntered(this, message);
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
            this.window.addTab(new NewTab(), true);
        } else {
            return false;
        }
        update();
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("player", this.playerContext)
                .add("window", this.window)
                .toString();
    }
}
