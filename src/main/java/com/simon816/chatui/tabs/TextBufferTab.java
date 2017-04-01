package com.simon816.chatui.tabs;

import com.google.common.collect.Lists;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.util.TextBuffer;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public abstract class TextBufferTab extends Tab {

    private static class LineBufferComponent implements UIComponent {

        private static final int MAX_BUFFER_SIZE = 100;

        private final List<Text> buffer = Lists.newArrayList();

        private int viewOffset;

        LineBufferComponent() {
        }

        public boolean canScrollUp() {
            return this.viewOffset < this.buffer.size();
        }

        public boolean canScrollDown() {
            return this.viewOffset > 0;
        }

        public void scrollUp() {
            if (++this.viewOffset > this.buffer.size()) {
                this.viewOffset = this.buffer.size();
            }
        }

        public void scrollDown() {
            if (--this.viewOffset < 0) {
                this.viewOffset = 0;
            }
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            int remainingHeight = ctx.height;
            bufferLoop: for (int i = this.viewOffset; i < this.buffer.size(); i++) {
                Text message = this.buffer.get(i);
                List<Text> lines = TextUtils.splitLines(message, ctx.width, ctx.getLocale(), ctx.forceUnicode);
                for (int j = 0; j < lines.size(); j++) {
                    Text line = lines.get(j);
                    if (--remainingHeight <= 0) {
                        break bufferLoop;
                    }
                    lineFactory.insertNewLine(j, line, ctx.forceUnicode);
                }
            }
        }

        public void appendMessage(Text message) {
            this.buffer.add(0, message);
            if (this.buffer.size() > MAX_BUFFER_SIZE) {
                this.buffer.remove(this.buffer.size() - 1);
            }
        }

        public void clear() {
            this.buffer.clear();
        }

    }

    private static class Toolbar implements UIComponent {

        private final LineBufferComponent buffer;

        Toolbar(LineBufferComponent buffer) {
            this.buffer = buffer;
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            TextBuffer toolbar = new TextBuffer(ctx.forceUnicode);
            Text.Builder scrollUpButton = Text.builder("[Scroll Up]");
            if (this.buffer.canScrollUp()) {
                scrollUpButton.onClick(ChatUI.execClick(src -> {
                    this.buffer.scrollUp();
                    ChatUI.getView(src).update();
                }));
            } else {
                scrollUpButton.color(TextColors.GRAY);
            }
            Text.Builder scrollDownButton = Text.builder(" [Scroll Down]");
            if (this.buffer.canScrollDown()) {
                scrollDownButton.onClick(ChatUI.execClick(src -> {
                    this.buffer.scrollDown();
                    ChatUI.getView(src).update();
                }));
            } else {
                scrollDownButton.color(TextColors.GRAY);
            }
            toolbar.append(scrollUpButton.build());
            toolbar.append(scrollDownButton.build());
            StringBuilder spaces = new StringBuilder();
            TextUtils.padSpaces(spaces, ctx.width - toolbar.getWidth());
            lineFactory.appendNewLine(Text.builder(spaces.toString()).append(toolbar.getContents()).build(), ctx.forceUnicode);
        }
    }

    private final LineBufferComponent buffer;

    private int unread = 0;
    private boolean countUnread = true;

    public TextBufferTab() {
        super(Text.of(), new AnchorPaneUI());
        AnchorPaneUI pane = (AnchorPaneUI) getRoot();
        pane.addWithConstraint(this.buffer = new LineBufferComponent(), AnchorPaneUI.ANCHOR_BOTTOM);
        pane.addWithConstraint(new Toolbar(this.buffer), AnchorPaneUI.ANCHOR_BOTTOM);
    }

    public void appendMessage(Text message) {
        this.buffer.appendMessage(message);
        if (this.countUnread) {
            this.unread++;
        }
    }

    public int getUnread() {
        return this.unread;
    }

    @Override
    public void onFocus() {
        this.unread = 0;
        this.countUnread = false;
    }

    @Override
    public void onBlur() {
        this.countUnread = true;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.buffer.clear();
        this.unread = 0;
    }

    @Override
    public abstract Text getTitle();

    protected Text.Builder appendUnreadLabel(Text.Builder builder) {
        if (this.unread > 0) {
            builder.append(Text.of(" (", TextColors.RED, this.unread, TextColors.NONE, ")"));
        }
        return builder;
    }
}
