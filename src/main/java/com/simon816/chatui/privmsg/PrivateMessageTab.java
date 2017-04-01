package com.simon816.chatui.privmsg;

import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.tabs.TextBufferTab;
import org.spongepowered.api.text.Text;

public class PrivateMessageTab extends TextBufferTab {

    private final PlayerPrivateView ownView;
    private final PlayerPrivateView otherPlayerView;

    public PrivateMessageTab(PlayerPrivateView ownView, PlayerPrivateView otherView) {
        this.ownView = ownView;
        this.otherPlayerView = otherView;
    }

    @Override
    public Text getTitle() {
        return appendUnreadLabel(Text.builder("PM - " + this.otherPlayerView.getPlayerName())).build();
    }

    @Override
    public void onClose() {
        super.onClose();
        this.ownView.removeConversation(this.otherPlayerView.getPlayerId(), false);
    }

    @Override
    public void onTextInput(PlayerChatView view, Text input) {
        Text formatted = Text.builder("<" + view.getPlayer().getName() + "> ").append(input).build();
        appendMessage(formatted);
        view.update();
        this.otherPlayerView.createPrivateMessageTab(this.ownView, false).appendMessage(formatted);
        this.otherPlayerView.update();
    }

    PlayerPrivateView getOther() {
        return this.otherPlayerView;
    }
}
