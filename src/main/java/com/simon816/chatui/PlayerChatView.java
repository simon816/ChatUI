package com.simon816.chatui;

import com.simon816.chatui.tabs.NewTab;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Optional;

public interface PlayerChatView {

    public PlayerList getPlayerList();

    public Player getPlayer();

    public Window getWindow();

    public NewTab getNewTab();

    public void update();

    public boolean handleIncoming(Text message);

    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type);

    public boolean handleCommand(String[] args);

}
