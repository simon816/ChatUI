package com.simon816.minecraft.tabchat.tabs;

import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.util.TextUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public abstract class TextBufferTab extends BufferedTab {

    private static final int MAX_BUFFER_SIZE = 100;

    private final List<Text> buffer = Lists.newArrayList();
    private final Player player;
    private int unread = 0;
    private boolean countUnread = true;

    public TextBufferTab(Player player) {
        this.player = player;
    }

    @Override
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
    public Text draw(final int height) {
        Text.Builder builder = Text.builder();
        int remainingHeight = height;
        bufferLoop: for (int i = 0; i < this.buffer.size(); i++) {
            Text message = this.buffer.get(i);
            List<Text> lines = TextUtils.splitLines(message, 320, this.player.isChatColorsEnabled());
            for (Text line : lines) {
                if (--remainingHeight < 0) {
                    break bufferLoop;
                }
                builder.insert(0, line, Text.NEW_LINE);
            }
        }
        if (remainingHeight > 0) {
            StringBuilder spacing = new StringBuilder();
            while (remainingHeight-- > 0) {
                spacing.append("\n");
            }
            builder.insert(0, Text.of(spacing));
        }
        return builder.build();
    }

    protected Text.Builder appendUnreadLabel(Text.Builder builder) {
        if (this.unread > 0) {
            builder.append(Text.of(" (", TextColors.RED, this.unread, TextColors.NONE, ")"));
        }
        return builder;
    }
}
