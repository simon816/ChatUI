package com.simon816.chatui.lib;

import com.google.common.collect.Lists;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.util.TextUtils;
import com.simon816.chatui.util.Utils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;
import java.util.function.Consumer;

public class TextEditorWindow implements TopWindow {

    private final AnchorPaneUI pane;

    private final List<String> lines = Lists.newArrayList();

    private int viewOffset = 0;
    private int activeLine = -1;

    public TextEditorWindow() {
        this.pane = new AnchorPaneUI();
        this.pane.getChildren().add(new TextArea());
        this.pane.addWithConstraint(new Toolbar(), AnchorPaneUI.ANCHOR_BOTTOM);
    }

    public List<String> getLines() {
        return this.lines;
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onTextInput(PlayerChatView view, Text input) {
        if (this.activeLine != -1) {
            this.lines.set(this.activeLine, input.toPlain());
            view.update();
        }
    }

    @Override
    public boolean onCommand(PlayerChatView view, String[] args) {
        String prefix = args[0];
        if (!prefix.equals("editor")) {
            return false;
        }
        String action = args[1];
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
        } else {
            return false;
        }
        return true;
    }

    private Consumer<PlayerChatView> setActiveLine(int line, boolean scroll) {
        return view -> {
            this.activeLine = line;
            while (this.activeLine >= this.lines.size()) {
                this.lines.add("");
                if (scroll) {
                    this.viewOffset++;
                }
            }
            view.update();
        };
    }

    @Override
    public Text draw(PlayerContext ctx) {
        return this.pane.draw(ctx);
    }

    void drawTextArea(PlayerContext ctx, LineFactory lineFactory) {
        int remaining = ctx.height;
        int largestLineNum = Math.min(this.lines.size(), this.viewOffset + ctx.height);
        int largestNumWidth = ctx.utils().getStringWidth(String.valueOf(largestLineNum), false);
        char sp = ' ';
        int spWidth = ctx.utils().getWidth(sp, false);

        for (int i = this.viewOffset; i < this.lines.size(); i++) {
            String line = this.lines.get(i);
            List<String> splitLines = ctx.utils().splitLines(line, ctx.width - largestNumWidth - spWidth);

            Text.Builder lineBuilder = Text.builder();

            ClickAction<?> lineClick;
            if (this.activeLine == i) {
                lineClick = TextActions.suggestCommand(line);
                lineBuilder.style(TextStyles.UNDERLINE);
            } else {
                lineClick = Utils.execClick(setActiveLine(i, false));

            }
            lineBuilder.onClick(lineClick);

            for (int j = 0; j < splitLines.size(); j++) {
                String outputLine = splitLines.get(j);

                StringBuilder sideLine = new StringBuilder();
                if (j == 0) {
                    String ourLine = String.valueOf(i + 1); // + 1 for 1-indexed
                    TextUtils.padSpaces(sideLine, largestNumWidth - ctx.utils().getStringWidth(ourLine, false));
                    sideLine.append(ourLine);
                } else {
                    TextUtils.padSpaces(sideLine, largestNumWidth);
                }
                sideLine.append(sp);
                lineBuilder.append(Text.builder(sideLine.toString()).color(TextColors.GRAY).build());
                lineBuilder.append(Text.of(outputLine));
                lineFactory.appendNewLine(lineBuilder.build(), ctx);
                lineBuilder.removeAll();
                remaining--;
                if (remaining == 0) {
                    break;
                }
            }
            if (remaining == 0) {
                break;
            }
        }
        if (remaining > 0) {
            Text newlineButton = Text.builder("*").color(TextColors.GRAY)
                    .onClick(Utils.execClick(setActiveLine(this.lines.size(), remaining < 2)))
                    .build();
            lineFactory.appendNewLine(newlineButton, ctx);
        }
    }

    void drawToolbar(PlayerContext ctx, LineFactory lineFactory) {
        Text.Builder toolbar = Text.builder();

        Text.Builder insertButton = Text.builder("[Insert Line]");
        if (this.activeLine != -1) {
            insertButton.onClick(ChatUILib.command("editor insert"));
        } else {
            insertButton.color(TextColors.GRAY);
        }

        Text.Builder deleteButton = Text.builder(" [Delete Line]");
        if (this.activeLine != -1) {
            deleteButton.onClick(ChatUILib.command("editor delete"));
        } else {
            deleteButton.color(TextColors.GRAY);
        }
        Text.Builder scrollUpButton = Text.builder(" [Scroll Up]");
        if (this.viewOffset != 0) {
            scrollUpButton.onClick(ChatUILib.command("editor scrUp"));
        } else {
            scrollUpButton.color(TextColors.GRAY);
        }
        Text.Builder scrollDownButton = Text.builder(" [Scroll Down]");
        if (this.viewOffset < this.lines.size()) {
            scrollDownButton.onClick(ChatUILib.command("editor scrDown"));
        } else {
            scrollDownButton.color(TextColors.GRAY);
        }
        toolbar.append(insertButton.build(), deleteButton.build(), scrollUpButton.build(), scrollDownButton.build());
        lineFactory.appendNewLine(toolbar.build(), ctx);
    }

    private class TextArea implements UIComponent {

        TextArea() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            drawTextArea(ctx, lineFactory);
        }

    }

    private class Toolbar implements UIComponent {

        Toolbar() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            drawToolbar(ctx, lineFactory);
        }
    }

}
