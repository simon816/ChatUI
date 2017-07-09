package com.simon816.chatui.tabs;

import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.lib.TopWindow;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIPane;
import org.spongepowered.api.text.Text;

public class WindowTab extends Tab {

    private final TopWindow window;

    public WindowTab(TopWindow window) {
        super(Text.of(window.getClass().getSimpleName()), new TopWindowPane(window));
        this.window = window;
    }

    @Override
    public void onClose() {
        this.window.onClose();
    }

    @Override
    public void onTextInput(PlayerChatView view, Text input) {
        this.window.onTextInput(view, input);
    }

    @Override
    public boolean onCommand(PlayerChatView view, String[] args) {
        return this.window.onCommand(view, args);
    }

    private static class TopWindowPane extends UIPane {

        private TopWindow window;

        public TopWindowPane(TopWindow window) {
            this.window = window;
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            Text result = this.window.draw(ctx);
            lineFactory.addAll(ctx.utils().splitLines(result, ctx.width), ctx);
        }

    }

}
