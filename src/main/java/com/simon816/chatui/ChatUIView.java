package com.simon816.chatui;

import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.TopWindow;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Optional;

// A wrapper class to enclose both ActivePlayerChatView and DisabledChatView
class ChatUIView implements PlayerChatView {

    private PlayerChatView actualView;

    public ChatUIView(PlayerChatView actualView) {
        this.actualView = actualView;
    }

    public PlayerChatView getActualView() {
        return this.actualView;
    }

    @Override
    public Player getPlayer() {
        return this.actualView.getPlayer();
    }

    @Override
    public TopWindow getWindow() {
        return this.actualView.getWindow();
    }

    @Override
    public void update() {
        this.actualView.update();
    }

    @Override
    public boolean handleIncoming(Text message) {
        return this.actualView.handleIncoming(message);
    }

    @Override
    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type) {
        return this.actualView.transformOutgoing(sender, originalOutgoing, type);
    }

    @Override
    public boolean handleCommand(String[] args) {
        return this.actualView.handleCommand(args);
    }

    @Override
    public void onRemove() {
        this.actualView.onRemove();
    }

    @Override
    public void initialize() {
        this.actualView.initialize();
    }

    public void setView(PlayerChatView newView) {
        this.actualView.onRemove();
        this.actualView = newView;
        newView.initialize();
    }

}
