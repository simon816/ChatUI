package com.simon816.chatui.privmsg;

import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.privmsg.PrivateMessageFeature.PlayerPrivateView;
import com.simon816.chatui.tabs.TextBufferTab;
import org.spongepowered.api.text.Text;

public class PrivateMessageTab extends TextBufferTab {

    private final PlayerPrivateView ownView;
    final PlayerPrivateView otherPlayerView;

    public PrivateMessageTab(PlayerPrivateView ownView, PlayerPrivateView otherView) {
        this.ownView = ownView;
        this.otherPlayerView = otherView;
    }

    @Override
    public Text getTitle() {
        return appendUnreadLabel(Text.builder("PM - " + this.otherPlayerView.view.getPlayer().getName())).build();
    }

    @Override
    public void onClose() {
        super.onClose();
        this.ownView.removeTab(this.otherPlayerView.view.getPlayer().getUniqueId());
    }

    @Override
    public void onTextEntered(PlayerChatView view, Text input) {
        Text formatted = Text.builder("<" + view.getPlayer().getName() + "> ").append(input).build();
        appendMessage(formatted);
        view.update();
        this.otherPlayerView.createPrivateMessageTab(view.getPlayer(), false).appendMessage(formatted);
        this.otherPlayerView.view.update();
    }
}
