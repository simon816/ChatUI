package com.simon816.chatui.lib;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DefaultChatView implements PlayerChatView {

    private final UUID playerUUID;

    private PlayerContext context;
    private TopWindow window;
    private boolean isUpdating;

    private final List<Tuple<Text, ChatType>> chatBuffer = Lists.newArrayList();

    public DefaultChatView(Player player) {
        this.playerUUID = player.getUniqueId();
    }

    @Override
    public void initialize() {
    }

    @Override
    public Player getPlayer() {
        return Sponge.getServer().getPlayer(this.playerUUID).get();
    }

    @Override
    public TopWindow getWindow() {
        return this.window;
    }

    public void setWindow(TopWindow window, PlayerContext context) {
        if (this.window != null) {
            this.window.onClose();
        }
        this.window = window;
        if (window != null) {
            this.context = checkNotNull(context, "context");
            update();
        } else {
            this.context = null;
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
        this.context = null;
        this.chatBuffer.clear();
    }

}
