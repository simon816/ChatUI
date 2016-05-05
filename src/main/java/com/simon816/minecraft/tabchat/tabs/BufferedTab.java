package com.simon816.minecraft.tabchat.tabs;

import com.simon816.minecraft.tabchat.MessageHandler;
import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.TabbedChat;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public abstract class BufferedTab extends Tab {

    private MessageHandler handler;
    private PlayerChatView view;

    protected void enableInputCapturing(PlayerChatView view) {
        (this.view = view).getIncomingPipeline().addHandler(this.handler = (message, sender) -> {
            PlayerChatView senderView = TabbedChat.getView((Player) sender);
            if (senderView.getWindow().getActiveTab() == this) {
                appendMessage(message);
                senderView.update();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onClose() {
        if (this.view != null && this.handler != null) {
            this.view.getIncomingPipeline().removeHandler(this.handler);
        }
    }

    public abstract void appendMessage(Text message);

}
