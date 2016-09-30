package com.simon816.chatui.tabs;

import com.google.common.collect.Lists;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.List;

public class TextFileTab extends BufferedTab {

    private final List<String> buffer = Lists.newArrayList();
    private int caretLine;
    private int caretCol;

    public TextFileTab(String[] lines) {
        this();
        Collections.addAll(this.buffer, lines);
    }

    public TextFileTab() {
        this.caretLine = 1;
        bufferInput();
    }

    @Override
    public LiteralText getTitle() {
        return Text.of("New File");
    }

    @Override
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        int remainingHeight = ctx.height;
        bufferLoop: for (int i = 0; i < this.buffer.size(); i++) {
            String side = ":: ";
            Text message = Text.of(this.buffer.get(i));
            List<Text> lines = TextUtils.splitLines(message, ctx.width - TextUtils.getStringWidth(side, false), ctx.player.getLocale());
            for (Text line : lines) {
                if (--remainingHeight < 0) {
                    break bufferLoop;
                }
                int ln = ctx.height - remainingHeight;
                String str = line.toPlain();
                Text.Builder b = Text.builder()
                        .onShiftClick(TextActions.insertText(str))
                        .color(TextColors.WHITE);
                char[] chars = str.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    int col = j;
                    Text.Builder charBuilder = Text.builder(chars[j])
                            .onClick(clickAction(ln, col));
                    if (ln == this.caretLine && j == this.caretCol) {
                        charBuilder.color(TextColors.RED);
                    }
                    b.append(charBuilder.build());
                }
                line = b.build();
                if (ln == this.caretLine) {
                    side = "@";
                }
                builder.append(Text.of(TextColors.GRAY, clickAction(ln, 0), side), line, Text.NEW_LINE);
            }
        }
        if (remainingHeight > 0) {
            while (remainingHeight-- > 0) {
                String side = ":: \n";
                int ln = ctx.height - remainingHeight;
                if (ln == this.caretLine) {
                    side = "@\n";
                }
                builder.append(Text.of(TextColors.GRAY, clickAction(ln, 0), side));
            }
        }
        return builder.build();
    }

    private ClickAction<?> clickAction(int ln, int col) {
        // Reduces the size of the serialized JSON by not using UUIDs for each
        // line/column
        return ChatUI.command("tf " + ln + " " + col);
    }

    public void onCommand(PlayerChatView view, int ln, int col) {
        this.caretLine = ln;
        this.caretCol = col;
    }

    @Override
    public void appendMessage(Text message) {
        String insertText = message.toPlain();
        while (this.buffer.size() < this.caretLine) {
            this.buffer.add("");
        }
        if (this.buffer.size() >= this.caretLine) {
            String existing = this.buffer.get(this.caretLine - 1);
            String newStr;
            if (existing.length() > this.caretCol) {
                newStr = existing.substring(0, this.caretCol + 1) + insertText + existing.substring(this.caretCol + 1);
                this.caretCol++;
            } else {
                newStr = existing + insertText;
            }
            this.buffer.set(this.caretLine - 1, newStr);
        } else {
            this.buffer.add(insertText);
        }
        this.caretCol += insertText.length() - 1;
    }

}
