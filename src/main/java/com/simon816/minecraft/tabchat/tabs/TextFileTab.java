package com.simon816.minecraft.tabchat.tabs;

import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.TabbedChat;
import com.simon816.minecraft.tabchat.util.TextUtils;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
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
                Text.Builder b = Text.builder();
                char[] chars = str.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    int col = j;
                    b.append(Text.builder(chars[j]).onClick(TextActions.executeCallback(src -> {
                        this.caretLine = ln;
                        this.caretCol = col;
                        TabbedChat.getView(ctx.player).update();
                    })).onShiftClick(TextActions.insertText(str))
                            .color(ln == this.caretLine && j == this.caretCol ? TextColors.RED : TextColors.WHITE).toText());
                }
                line = b.build();
                if (ln == this.caretLine) {
                    side = "@";
                }
                builder.append(Text.of(TextColors.GRAY, TextActions.executeCallback(src -> {
                    this.caretLine = ln;
                    this.caretCol = 0;
                    TabbedChat.getView(ctx.player).update();
                }), side), line, Text.NEW_LINE);
            }
        }
        if (remainingHeight > 0) {
            while (remainingHeight-- > 0) {
                String side = ":: \n";
                int ln = ctx.height - remainingHeight;
                if (ln == this.caretLine) {
                    side = "@\n";
                }
                builder.append(Text.of(TextColors.GRAY, TextActions.executeCallback(src -> {
                    this.caretLine = ln;
                    this.caretCol = 0;
                    TabbedChat.getView(ctx.player).update();
                }), side));
            }
        }
        return builder.build();
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
