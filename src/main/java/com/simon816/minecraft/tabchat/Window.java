package com.simon816.minecraft.tabchat;

import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.tabs.Tab;
import com.simon816.minecraft.tabchat.util.TextBuffer;
import com.simon816.minecraft.tabchat.util.TextUtils;
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

    public Tab getActiveTab() {
        return this.tabs.get(this.activeIndex);
    }

    @Override
    public Text draw(PlayerContext ctx) {
        if (ctx.height < 5) {
            throw new IllegalArgumentException("Height too small");
        }
        StringBuilder borderBuilder = new StringBuilder();
        double borderWidth = 0;
        while (borderWidth <= ctx.width) {
            borderWidth += TextUtils.getWidth('=', false);
            if (borderWidth <= ctx.width) {
                borderBuilder.append('=');
            }
        }
        String borderLine = borderBuilder.toString();
        Builder builder = Text.builder();
        builder.append(getTabListText(ctx.width));
        builder.append(Text.of(borderLine));
        builder.append(Text.NEW_LINE);
        builder.append(getActiveTab().draw(ctx.withHeight(ctx.height - 4)));
        builder.append(Text.of(borderLine + "\n"));
        builder.append(getStatusBarText());
        return builder.build();
    }

    private Text getStatusBarText() {
        return Text.of("Status: ", TextColors.RED, "0", TextColors.RESET, " unread PMs");
    }

    private static final LiteralText leftArrow = Text.builder("< ").onClick(TabbedChat.command("tableft")).build();
    private static final LiteralText rightArrow = Text.builder(" >").onClick(TabbedChat.command("tabright")).build();
    private static final LiteralText newTab = Text.builder(" [+]").color(TextColors.GREEN).onClick(TabbedChat.command("newtab"))
            .onHover(TextActions.showText(Text.of("New Tab"))).build();
    private int displayTabIndex;

    public void shiftLeft() {
        if (--this.displayTabIndex < 0) {
            this.displayTabIndex = 0;
        }
    }

    public void shiftRight() {
        if (++this.displayTabIndex >= this.tabs.size()) {
            this.displayTabIndex--;
        }
    }

    private Text getTabListText(int maxWidth) {
        Text.Builder builder = Text.builder();
        int width = TextUtils.getWidth(leftArrow) + TextUtils.getWidth(rightArrow);
        TextBuffer buffer = new TextBuffer();
        int count = 0;
        for (int i = this.displayTabIndex; i < this.tabs.size(); i++) {
            Tab tab = this.tabs.get(i);

            buffer.append(Text.of("| "));

            Text.Builder button = tab.getTitle().toBuilder();
            button.color(TextColors.GREEN);
            if (i == this.activeIndex) {
                button.style(TextStyles.BOLD);
            } else {
                button.onClick(TabbedChat.command("settab " + i));
            }

            buffer.append(button.build());

            if (tab.hasCloseButton()) {
                Text closeButton = Text.builder("[x]").color(TextColors.RED)
                        .onClick(TabbedChat.command("closetab " + i))
                        .onHover(TextActions.showText(Text.of("Close")))
                        .build();
                buffer.append(closeButton);
            }
            buffer.append(Text.of(" |"));

            width += buffer.getWidth();
            if (width <= maxWidth) {
                builder.append(buffer.getContents());
                count++;
                buffer.clear();
            } else {
                buffer.clear();
                break;
            }
        }
        width += TextUtils.getWidth(newTab);
        if (this.displayTabIndex == 0 && width <= maxWidth) {
            builder.append(newTab);
        } else {
            if (this.displayTabIndex > 0) {
                builder.insert(0, leftArrow);
            }
            if (this.displayTabIndex + count < this.tabs.size() || width > maxWidth) {
                builder.append(rightArrow);
            }
            if (width <= maxWidth) {
                builder.append(newTab);
            }
        }

        return builder.build();
    }

}
