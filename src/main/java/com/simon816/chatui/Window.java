package com.simon816.chatui;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.simon816.chatui.lib.ChatUILib;
import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.lib.TopWindow;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.tabs.TextBufferTab;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.util.TextBuffer;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;

public class Window implements TopWindow {

    private final List<Tab> tabs = Lists.newArrayList();
    private final AnchorPaneUI pane;
    private int activeIndex = -1;

    public Window() {
        this.pane = new AnchorPaneUI();
        this.pane.addWithConstraint(new TabBar(), AnchorPaneUI.ANCHOR_TOP);
        this.pane.getChildren().add(new TabHolder());
        this.pane.addWithConstraint(new StatusBar(), AnchorPaneUI.ANCHOR_BOTTOM);

    }

    public void addTab(Tab tab, boolean switchTab) {
        int idx = this.tabs.indexOf(tab);
        if (idx == -1) {
            this.tabs.add(tab);
            idx = this.tabs.size() - 1;
        }
        if (switchTab) {
            setTab(idx);
        }
    }

    public void removeTab(Tab tab) {
        removeTab(this.tabs.indexOf(tab));
    }

    public void removeTab(int index) {
        if (index < this.activeIndex) {
            this.activeIndex--;
        } else if (index == this.activeIndex) {
            setTab(index - 1);
        }
        this.tabs.remove(index).onClose();
    }

    public void setTab(int tabIndex) {
        if (tabIndex >= this.tabs.size()) {
            throw new IllegalArgumentException("Tab index doesn't exist");
        }
        if (tabIndex == this.activeIndex) {
            return;
        }
        if (this.activeIndex > -1) {
            this.tabs.get(this.activeIndex).onBlur();
        }
        this.activeIndex = tabIndex;
        this.tabs.get(this.activeIndex).onFocus();
    }

    public void setTab(Tab tab) {
        setTab(this.tabs.indexOf(tab));
    }

    public Tab getActiveTab() {
        return this.tabs.get(this.activeIndex);
    }

    public int getActiveIndex() {
        return this.activeIndex;
    }

    @Override
    public void onClose() {
        for (Tab tab : this.tabs) {
            tab.onClose();
        }
        this.tabs.clear();
        this.activeIndex = -1;
    }

    @Override
    public boolean onCommand(PlayerChatView view, String[] args) {
        String cmd = args[0];
        if (cmd.equals("settab")) {
            this.setTab(Integer.parseInt(args[1]));
        } else if (cmd.equals("closetab")) {
            this.removeTab(Integer.parseInt(args[1]));
        } else if (this.activeIndex != -1) {
            return this.tabs.get(this.activeIndex).onCommand(view, args);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onTextInput(PlayerChatView view, Text input) {
        if (this.activeIndex != -1) {
            this.tabs.get(this.activeIndex).onTextInput(view, input);
        }
    }

    @Override
    public Text draw(PlayerContext ctx) {
        return this.pane.draw(ctx);
    }

    List<Tab> getTabs() {
        return this.tabs;
    }

    static final LiteralText newTab = Text.builder("[+]")
            .color(TextColors.GREEN)
            .onClick(ChatUILib.command("newtab"))
            .onHover(TextActions.showText(Text.of("New Tab")))
            .build();

    private class TabBar implements UIComponent {

        private int tabListLines = 1;

        TabBar() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            int maxWidth = ctx.width;
            Text.Builder builder = Text.builder();
            int currentLineWidth = 0;
            TextBuffer buffer = new TextBuffer(ctx);
            buffer.append(TextUtils.charCache('╔'));
            this.tabListLines = 1;
            for (int i = 0; i < getTabs().size(); i++) {
                Tab tab = getTabs().get(i);
                buffer.append(createTabButton(i));
                if (tab.hasCloseButton()) {
                    buffer.append(createCloseButton(i));
                }
                buffer.append(TextUtils.charCache('═'));
                currentLineWidth = addTabElement(lineFactory, buffer, builder, currentLineWidth, maxWidth, ctx);
                buffer.clear();
            }
            buffer.append(newTab);
            currentLineWidth = addTabElement(lineFactory, buffer, builder, currentLineWidth, maxWidth, ctx);
            builder.append(ctx.utils().repeatAndTerminate('═', this.tabListLines == 1 ? '╗' : '╣', maxWidth - currentLineWidth));
            lineFactory.appendNewLine(builder.build(), ctx);
        }

        private int addTabElement(LineFactory lineFactory, TextBuffer buffer, Text.Builder builder, int currentLineWidth, int maxWidth,
                PlayerContext ctx) {
            if (currentLineWidth + buffer.getWidth() > maxWidth) {
                // Overspilled - finish this line and move to another one
                builder.append(ctx.utils().repeatAndTerminate('═', this.tabListLines == 1 ? '╗' : '╣', maxWidth - currentLineWidth));
                lineFactory.appendNewLine(builder.build(), ctx);
                char startChar = '╠';
                builder.removeAll();
                currentLineWidth = ctx.utils().getWidth(startChar, false);
                builder.append(TextUtils.charCache(startChar));
                this.tabListLines++;
            }
            currentLineWidth += buffer.getWidth();
            builder.append(buffer.getContents());
            return currentLineWidth;
        }

        private Text createTabButton(int tabIndex) {
            Text.Builder button = getTabs().get(tabIndex).getTitle().toBuilder();
            button.color(TextColors.GREEN);
            if (tabIndex == getActiveIndex()) {
                button.style(TextStyles.BOLD);
            } else {
                button.onClick(ChatUILib.command("settab " + tabIndex));
            }
            return button.build();
        }

        private Text createCloseButton(int tabIndex) {
            return Text.builder("[x]").color(TextColors.RED)
                    .onClick(ChatUILib.command("closetab " + tabIndex))
                    .onHover(TextActions.showText(Text.of("Close")))
                    .build();
        }

        @Override
        public int getPrefHeight(PlayerContext ctx) {
            return this.tabListLines;
        }

    }

    private class TabHolder implements UIComponent {

        TabHolder() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            if (getActiveIndex() != -1) {
                getActiveTab().draw(ctx, lineFactory);
            }
        }
    }

    private class StatusBar implements UIComponent {

        StatusBar() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            Text prefix = Text.of("╚Status: ");
            Text content = getStatusBarContent();
            int remWidth = ctx.width - ctx.utils().getWidth(prefix) - ctx.utils().getWidth(content);
            Text line = Text.builder().append(prefix, content, ctx.utils().repeatAndTerminate('═', '╝', remWidth)).build();
            lineFactory.appendNewLine(line, ctx);
        }

        private Text getStatusBarContent() {
            int unread = 0;
            for (Tab tab : getTabs()) {
                if (tab instanceof TextBufferTab) {
                    unread += ((TextBufferTab) tab).getUnread();
                }
            }
            return Text.builder(String.valueOf(unread)).color(TextColors.RED)
                    .append(Text.builder(" unread messages").color(TextColors.RESET).build()).build();
        }

        @Override
        public int getPrefHeight(PlayerContext ctx) {
            return 1;
        }

    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("active", getActiveTab())
                .add("tabs", this.tabs)
                .toString();
    }

}
