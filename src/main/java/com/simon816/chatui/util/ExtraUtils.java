package com.simon816.chatui.util;

import com.simon816.chatui.ChatUI;
import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.tabs.Tab;
import org.spongepowered.api.text.action.ClickAction;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ExtraUtils {

    public static ClickAction<?> clickAction(Runnable action, Tab tab) {
        return clickAction(view -> {
            action.run();
            return true;
        }, tab);
    }

    public static ClickAction<?> clickAction(Consumer<PlayerChatView> consumer, Tab tab) {
        return clickAction(view -> {
            consumer.accept(view);
            return true;
        }, tab);
    }

    public static ClickAction<?> clickAction(BooleanSupplier action, Tab tab) {
        return clickAction((Predicate<PlayerChatView>) view -> action.getAsBoolean(), tab);
    }

    public static ClickAction<?> clickAction(Predicate<PlayerChatView> action, Tab tab) {
        return Utils.execClick(clickHandler(action, tab));
    }

    public static Consumer<PlayerChatView> clickHandler(Predicate<PlayerChatView> action, Tab tab) {
        return view -> {
            PlayerChatView unwrapped = ChatUI.unwrapView(view);
            if (!ChatUI.isTabActive(unwrapped, tab)) {
                return;
            }
            if (action.test(unwrapped)) {
                view.update();
            }
        };
    }

}
