package com.simon816.minecraft.tabchat.privmsg;

import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.tabs.TextBufferTab;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

public class PrivateMessageTab extends TextBufferTab {

    private final Player other;

    public PrivateMessageTab(PlayerChatView view, Player otherPlayer) {
        super(view.getPlayer());
        this.other = otherPlayer;
        enableInputCapturing(view);
    }


    public Player getOther() {
        return this.other;
    }

    @Override
    public Text getTitle() {
        LiteralText.Builder b = Text.builder("Private - " + this.other.getName());
        return appendUnreadLabel(b).build();
    }
}
