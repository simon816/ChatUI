package com.simon816.chatui.tabs;

import com.google.common.collect.Lists;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public abstract class TextBufferTab extends Tab {

    private static final int MAX_BUFFER_SIZE = 100;

    protected final List<Text> buffer = Lists.newArrayList();
    private int unread = 0;
    private boolean countUnread = true;

    private int viewOffset;

    public void appendMessage(Text message) {
        this.buffer.add(0, message);
        if (this.buffer.size() > MAX_BUFFER_SIZE) {
            this.buffer.remove(this.buffer.size() - 1);
        }
        if (this.countUnread) {
            this.unread++;
        }
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
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        int remainingHeight = ctx.height - 1;
        bufferLoop: for (int i = this.viewOffset; i < this.buffer.size(); i++) {
            Text message = this.buffer.get(i);
            List<Text> lines = TextUtils.splitLines(message, ctx.width, ctx.getLocale(), ctx.forceUnicode);
            for (int j = 0; j < lines.size(); j++) {
                Text line = lines.get(j);
                if (--remainingHeight < 0) {
                    break bufferLoop;
                }
                // The client will break content onto new lines so we don't need
                // NEW_LINE for subsequent lines
                if (j == 0) {
                    builder.insert(0, Text.NEW_LINE);
                }
                builder.insert(j, line);
            }
        }
        if (remainingHeight > 0) {
            StringBuilder spacing = new StringBuilder();
            while (remainingHeight-- > 0) {
                spacing.append("\n");
            }
            builder.insert(0, Text.of(spacing));
        }
        Text.Builder toolbar = Text.builder();
        Text.Builder scrollUpButton = Text.builder("[Scroll Up]");
        if (this.viewOffset < this.buffer.size()) {
            scrollUpButton.onClick(ChatUI.execClick(src -> {
                this.viewOffset++;
                ChatUI.getView(src).update();
            }));
        } else {
            scrollUpButton.color(TextColors.GRAY);
        }
        Text.Builder scrollDownButton = Text.builder(" [Scroll Down]");
        if (this.viewOffset > 0) {
            scrollDownButton.onClick(ChatUI.execClick(src -> {
                this.viewOffset--;
                ChatUI.getView(src).update();
            }));
        } else {
            scrollDownButton.color(TextColors.GRAY);
        }
        toolbar.append(scrollUpButton.build(), scrollDownButton.build());
        builder.append(toolbar.build(), Text.NEW_LINE);
        return builder.build();
    }

    protected Text.Builder appendUnreadLabel(Text.Builder builder) {
        if (this.unread > 0) {
            builder.append(Text.of(" (", TextColors.RED, this.unread, TextColors.NONE, ")"));
        }
        return builder;
    }
}
