package com.simon816.chatui.lib;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Optional;

public interface PlayerChatView {

    public Player getPlayer();

    public TopWindow getWindow();

    public boolean showWindow(TopWindow window);

    public boolean removeShownWindow();

    public void update();

    public boolean handleIncoming(Text message);

    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type);

    public boolean handleCommand(String[] args);

    public void onRemove();

    public void initialize();

}
