package com.simon816.chatui;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Optional;

public class ExternalServiceView implements PlayerChatView {

    private final PlayerContext context;

    private TopWindow window;
    private boolean isUpdating;

    private final List<Tuple<Text, ChatType>> chatBuffer = Lists.newArrayList();

    public ExternalServiceView(Player player) {
        this.context = new PlayerContext(player, Config.DEFAULT_BUFFER_WIDTH, Config.DEFAULT_BUFFER_HEIGHT, false);
    }

    @Override
    public Player getPlayer() {
        return this.context.getPlayer();
    }

    @Override
    public TopWindow getWindow() {
        return this.window;
    }

    public void setWindow(TopWindow window) {
        if (this.window != null) {
            this.window.onClose();
        }
        this.window = window;
        if (window != null) {
            update();
        } else {
            flushBuffer();
        }
    }

    private void flushBuffer() {
        Player player = getPlayer();
        for (Tuple<Text, ChatType> message : this.chatBuffer) {
            player.sendMessage(message.getSecond(), message.getFirst());
        }
        this.chatBuffer.clear();
    }

    @Override
    public void update() {
        if (this.window != null) {
            this.isUpdating = true;
            getPlayer().sendMessage(this.window.draw(this.context));
            this.isUpdating = false;
        }
    }

    @Override
    public boolean handleIncoming(Text message) {
        if (this.window == null) {
            return false;
        }
        this.window.onTextInput(this, message);
        return true;
    }

    @Override
    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type) {
        if (this.window == null || this.isUpdating) {
            return Optional.of(originalOutgoing);
        }
        this.chatBuffer.add(new Tuple<>(originalOutgoing, type));
        return Optional.empty();
    }

    @Override
    public boolean handleCommand(String[] args) {
        if (this.window == null) {
            return false;
        }
        boolean handled = this.window.onCommand(this, args);
        if (handled) {
            update();
        }
        return handled;
    }

    @Override
    public void onRemove() {
        this.window = null;
        this.chatBuffer.clear();
    }

}
