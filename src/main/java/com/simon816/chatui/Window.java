package com.simon816.chatui;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.util.TextBuffer;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;

public class Window implements ITextDrawable {

    private final List<Tab> tabs = Lists.newArrayList();
    private int activeIndex = -1;

    public void addTab(Tab tab, boolean switchTab) {
        this.tabs.add(tab);
        if (switchTab) {
            setTab(this.tabs.size() - 1);
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
    public Text draw(PlayerContext ctx) {
        if (ctx.height < 5) {
            throw new IllegalArgumentException("Height too small");
        }
        Builder builder = Text.builder();
        builder.append(getTabListText(ctx.width, ctx.forceUnicode));
        builder.append(Text.NEW_LINE);
        builder.append(getActiveTab().draw(ctx.withHeight(ctx.height - this.tabListLines - 1)));
        builder.append(getStatusBarText(ctx.width, ctx.forceUnicode));
        return builder.build();
    }

    private Text getStatusBarText(int width, boolean forceUnicode) {
        Text prefix = Text.of("╚Status: ");
        Text content = getStatusBarContent(forceUnicode);
        int remWidth = width - TextUtils.getWidth(prefix, forceUnicode) - TextUtils.getWidth(content, forceUnicode);
        return Text.builder().append(prefix, content, TextUtils.repeatAndTerminate('═', '╝', remWidth, forceUnicode)).build();
    }

    private Text getStatusBarContent(boolean forceUnicode) {
        return Text.EMPTY;
    }

    private static final LiteralText newTab = Text.builder("[+]")
            .color(TextColors.GREEN)
            .onClick(ChatUI.command("newtab"))
            .onHover(TextActions.showText(Text.of("New Tab")))
            .build();

    private int tabListLines;

    private Text getTabListText(int maxWidth, boolean forceUnicode) {
        Text.Builder builder = Text.builder();
        int currentLineWidth = 0;
        TextBuffer buffer = new TextBuffer(forceUnicode);
        buffer.append(TextUtils.charCache('╔'));
        this.tabListLines = 1;
        for (int i = 0; i < this.tabs.size(); i++) {
            Tab tab = this.tabs.get(i);
            buffer.append(createTabButton(i));
            if (tab.hasCloseButton()) {
                buffer.append(createCloseButton(i));
            }
            buffer.append(TextUtils.charCache('═'));
            currentLineWidth = addTabElement(buffer, builder, currentLineWidth, maxWidth, forceUnicode);
            buffer.clear();
        }
        buffer.append(newTab);
        currentLineWidth = addTabElement(buffer, builder, currentLineWidth, maxWidth, forceUnicode);
        builder.append(TextUtils.repeatAndTerminate('═', this.tabListLines == 1 ? '╗' : '╣', maxWidth - currentLineWidth, forceUnicode));
        return builder.build();
    }

    private int addTabElement(TextBuffer buffer, Text.Builder builder, int currentLineWidth, int maxWidth, boolean forceUnicode) {
        if (currentLineWidth + buffer.getWidth() > maxWidth) {
            // Overspilled - finish this line and move to another one
            builder.append(TextUtils.repeatAndTerminate('═', this.tabListLines == 1 ? '╗' : '╣', maxWidth - currentLineWidth, forceUnicode));
            Text newLineStart = Text.of("\n╠");
            currentLineWidth = TextUtils.getWidth(newLineStart, forceUnicode);
            builder.append(newLineStart);
            this.tabListLines++;
        }
        currentLineWidth += buffer.getWidth();
        builder.append(buffer.getContents());
        return currentLineWidth;
    }

    private Text createTabButton(int tabIndex) {
        Text.Builder button = this.tabs.get(tabIndex).getTitle().toBuilder();
        button.color(TextColors.GREEN);
        if (tabIndex == this.activeIndex) {
            button.style(TextStyles.BOLD);
        } else {
            button.onClick(ChatUI.command("settab " + tabIndex));
        }
        return button.build();
    }

    private Text createCloseButton(int tabIndex) {
        return Text.builder("[x]").color(TextColors.RED)
                .onClick(ChatUI.command("closetab " + tabIndex))
                .onHover(TextActions.showText(Text.of("Close")))
                .build();
    }

    void closeAll() {
        for (Tab tab : this.tabs) {
            tab.onClose();
        }
        this.tabs.clear();
        this.activeIndex = -1;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("active", getActiveTab())
                .add("tabs", this.tabs)
                .toString();
    }

}
