package com.simon816.chatui.util;

import com.google.common.base.Utf8;
import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.lib.internal.ClickCallback;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
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

    public static void sendMessageSplitLarge(PlayerContext ctx, Text text) {
        String json = TextSerializers.JSON.serialize(text);
        int size = Utf8.encodedLength(json);
        if (size > 32767) {
            List<Text> lines = ctx.utils().splitLines(text, ctx.width);
            ctx.getPlayer().sendMessages(lines);
        } else {
            ctx.getPlayer().sendMessage(text);
        }
    }
}
