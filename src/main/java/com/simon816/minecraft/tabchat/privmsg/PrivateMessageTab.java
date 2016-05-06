package com.simon816.minecraft.tabchat.privmsg;

import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.tabs.TextBufferTab;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class PrivateMessageTab extends TextBufferTab {

    private final String otherPlayerName;

    public PrivateMessageTab(PlayerChatView view, Player otherPlayer) {
        this.otherPlayerName = otherPlayer.getName();
        enableInputCapturing(view);
    }

    @Override
    public Text getTitle() {
        return appendUnreadLabel(Text.builder("Private - " + this.otherPlayerName)).build();
    }
}
