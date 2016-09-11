package com.simon816.chatui.tabs;

import com.simon816.chatui.PlayerChatView;
import org.spongepowered.api.text.Text;

public abstract class BufferedTab extends Tab {

    private boolean isCapturing;

    protected void bufferInput() {
        this.isCapturing = true;
    }

    @Override
    public void onTextEntered(PlayerChatView view, Text input) {
        if (this.isCapturing) {
            appendMessage(input);
            view.update();
        }
    }

    public abstract void appendMessage(Text message);

}
