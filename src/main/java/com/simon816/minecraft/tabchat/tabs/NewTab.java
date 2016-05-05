package com.simon816.minecraft.tabchat.tabs;

import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.TabbedChat;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

public class NewTab extends Tab {

    private static final Text TITLE = Text.of("New Tab");

    @Override
    public Text getTitle() {
        return TITLE;
    }

    @Override
    public Text draw(int height) {
        Text.Builder builder = Text.builder();
        builder.append(Text.of("What would you like to do?\n\n"));
        builder.append(Text.builder("* Open Settings\n").onClick(TextActions.executeCallback(src -> {
            PlayerChatView view = TabbedChat.getView((Player) src);
            view.getWindow().removeTab(this);
            view.getWindow().addTab(new SettingsTab(), true);
            view.update();
        })).build());
        builder.append(Text.builder("* Canvas test\n").onClick(TextActions.executeCallback(src -> {
            PlayerChatView view = TabbedChat.getView((Player) src);
            view.getWindow().removeTab(this);
            view.getWindow().addTab(new CanvasTab(), true);
            view.update();
        })).build());
        //builder.append(Text.builder("* View Player List\n").build());
        builder.append(Text.builder("* Create text file\n")
                .onClick(TextActions.executeCallback(src -> {
                    PlayerChatView view = TabbedChat.getView((Player) src);
                    view.getWindow().removeTab(this);
                    view.getWindow().addTab(new TextFileTab(view), true);
                    view.update();
                }))
                .build());
        for (int i = 0; i < height - 5; i++) {
            builder.append(Text.NEW_LINE);
        }
        return builder.build();
    }
}
