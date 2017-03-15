package com.simon816.chatui.tabs;

import com.google.common.collect.Lists;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;
import java.util.function.Consumer;

public class TextEditorTab extends Tab {

    private final List<String> lines = Lists.newArrayList();

    private int viewOffset = 0;
    private int activeLine = -1;

    private final Text title;

    public TextEditorTab() {
        this(Text.of("Text Editor"));
    }

    public TextEditorTab(Text title) {
        this.title = title;
    }

    public List<String> getLines() {
        return this.lines;
    }

    @Override
    public void onTextEntered(PlayerChatView view, Text input) {
        if (this.activeLine != -1) {
            this.lines.set(this.activeLine, input.toPlain());
        }
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    @Override
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        int remaining = ctx.height - 1;
        int largestLineNum = Math.min(this.lines.size(), this.viewOffset + ctx.height);
        int largestNumWidth = TextUtils.getStringWidth(String.valueOf(largestLineNum), false, ctx.forceUnicode);
        char sp = ' ';
        int spWidth = TextUtils.getWidth(sp, false, false);

        for (int i = this.viewOffset; i < this.lines.size(); i++) {
            String line = this.lines.get(i);
            List<String> splitLines = TextUtils.splitLines(line, ctx.width - largestNumWidth - spWidth, ctx.forceUnicode);

            Text.Builder lineBuilder = Text.builder();

            ClickAction<?> lineClick;
            if (this.activeLine == i) {
                lineClick = TextActions.suggestCommand(line);
                lineBuilder.style(TextStyles.UNDERLINE);
            } else {
                lineClick = TextActions.executeCallback(this.setActiveLine(i));

            }
            lineBuilder.onClick(lineClick);

            for (int j = 0; j < splitLines.size(); j++) {
                String outputLine = splitLines.get(j);

                StringBuilder sideLine = new StringBuilder();
                if (j == 0) {
                    String ourLine = String.valueOf(i + 1); // + 1 for 1-indexed
                    TextUtils.padSpaces(sideLine, largestNumWidth - TextUtils.getStringWidth(ourLine, false, ctx.forceUnicode));
                    sideLine.append(ourLine);
                } else {
                    TextUtils.padSpaces(sideLine, largestNumWidth);
                }
                sideLine.append(sp);
                lineBuilder.append(Text.builder(sideLine.toString()).color(TextColors.GRAY).build());

                LiteralText lineText = Text.builder(outputLine)
                        .append(Text.NEW_LINE)
                        .build();
                lineBuilder.append(lineText);
                remaining--;
                if (remaining == 0) {
                    break;
                }
            }
            builder.append(lineBuilder.build());
            if (remaining == 0) {
                break;
            }
        }

        Text blankSide = Text.builder("*\n").color(TextColors.GRAY).onClick(TextActions.executeCallback(setActiveLine(this.lines.size()))).build();
        while (remaining-- > 0) {
            builder.append(blankSide);
            blankSide = Text.NEW_LINE;
        }

        Text.Builder toolbar = Text.builder();

        Text.Builder insertButton = Text.builder("[Insert Line]");
        if (this.activeLine != -1) {
            insertButton.onClick(ChatUI.command("editor insert"));
        } else {
            insertButton.color(TextColors.GRAY);
        }

        Text.Builder deleteButton = Text.builder(" [Delete Line]");
        if (this.activeLine != -1) {
            deleteButton.onClick(ChatUI.command("editor delete"));
        } else {
            deleteButton.color(TextColors.GRAY);
        }
        Text.Builder scrollUpButton = Text.builder(" [Scroll Up]");
        if (this.viewOffset != 0) {
            scrollUpButton.onClick(ChatUI.command("editor scrUp"));
        } else {
            scrollUpButton.color(TextColors.GRAY);
        }
        Text.Builder scrollDownButton = Text.builder(" [Scroll Down]");
        if (this.viewOffset < this.lines.size()) {
            scrollDownButton.onClick(ChatUI.command("editor scrDown"));
        } else {
            scrollDownButton.color(TextColors.GRAY);
        }
        toolbar.append(insertButton.build(), deleteButton.build(), scrollUpButton.build(), scrollDownButton.build());

        builder.append(toolbar.build(), Text.NEW_LINE);

        return builder.build();
    }

    public void onCommand(PlayerChatView view, String action) {
        if (action.equals("scrUp")) {
            this.viewOffset--;
        } else if (action.equals("scrDown")) {
            this.viewOffset++;
        } else if (action.equals("delete")) {
            this.lines.remove(this.activeLine--);
            if (this.activeLine == -1 && !this.lines.isEmpty()) {
                this.activeLine = 0;
            }
            if (this.activeLine != -1 && this.activeLine < this.viewOffset) {
                this.viewOffset = this.activeLine;
            }
        } else if (action.equals("insert")) {
            this.lines.add(this.activeLine++, "");
        }
    }

    private Consumer<CommandSource> setActiveLine(int line) {
        return src -> {
            this.activeLine = line;
            while (this.activeLine >= this.lines.size()) {
                this.lines.add("");
                this.viewOffset++;
            }
            ChatUI.getView(src).update();
        };
    }

}
