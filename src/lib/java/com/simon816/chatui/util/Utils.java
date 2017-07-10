package com.simon816.chatui.util;

import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.internal.ClickCallback;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;

import java.util.function.Consumer;

public class Utils {

    public static CommandSource getRealSource(CommandSource source) {
        while (source instanceof ProxySource) {
            source = ((ProxySource) source).getOriginalSource();
        }
        return source;
    }

    public static ClickAction<?> execClick(Consumer<PlayerChatView> handler) {
        return TextActions.runCommand(ClickCallback.generateCommand(handler));
    }

    public static ClickAction<?> execClick(Runnable action) {
        return execClick(view -> {
            action.run();
            view.update();
        });
    }
}
