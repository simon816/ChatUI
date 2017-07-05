package com.simon816.chatui.group;

import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.tabs.TextBufferTab;
import org.spongepowered.api.text.Text;

class ChatBufferTab extends TextBufferTab {

    private final ChatGroup group;

    public ChatBufferTab(ChatGroup group) {
        this.group = group;
        for (Text message : group.getBacklog()) {
            this.appendMessage(message);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        this.group.onTabClosed(this);
    }

    @Override
    public Text getTitle() {
        return appendUnreadLabel(Text.builder(this.group.getName())).build();
    }

    @Override
    public void onTextInput(PlayerChatView view, Text input) {
        Text formatted = Text.builder("<" + view.getPlayer().getName() + "> ").append(input).build();
        this.group.onMessage(formatted);
    }
}
