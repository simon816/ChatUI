package com.simon816.minecraft.tabchat;

import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.tabs.Tab;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;

public class Window implements ITextDrawable {

    private static final String borderLine = "=====================================================";
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
    public Text draw(int height) {
        if (height < 5) {
            throw new IllegalArgumentException("Height too small");
        }
        Builder builder = Text.builder();
        builder.append(getTabListText());
        builder.append(Text.of(borderLine));
        builder.append(Text.NEW_LINE);
        builder.append(getActiveTab().draw(height - 4));
        builder.append(Text.of(borderLine + "\n"));
        builder.append(getStatusBarText());
        return builder.build();
    }

    private Text getStatusBarText() {
        return Text.of("Status: ", TextColors.RED, "0", TextColors.RESET, " unread PMs");
    }

    private Text getTabListText() {
        Text.Builder builder = Text.builder();
        for (int i = 0; i < this.tabs.size(); i++) {
            Tab tab = this.tabs.get(i);
            builder.append(Text.of("| "));
            Text.Builder button = tab.getTitle().toBuilder();
            button.color(TextColors.GREEN);
            if (i == this.activeIndex) {
                button.style(TextStyles.BOLD);
            } else {
                button.onClick(TabbedChat.command("settab " + i));
            }
            builder.append(button.build());
            if (tab.hasCloseButton()) {
                Text closeButton = Text.builder("[x]").color(TextColors.RED)
                        .onClick(TabbedChat.command("closetab " + i))
                        .onHover(TextActions.showText(Text.of("Close")))
                        .build();
                builder.append(closeButton);
            }
            builder.append(Text.of(" |"));
        }
        builder.append(Text.builder(" [+]").color(TextColors.GREEN)
                .onClick(TabbedChat.command("newtab"))
                .onHover(TextActions.showText(Text.of("New Tab"))).build());
        return builder.build();
    }
}
