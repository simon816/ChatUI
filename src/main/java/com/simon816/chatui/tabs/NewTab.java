package com.simon816.chatui.tabs;

import com.simon816.chatui.ActivePlayerChatView;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.ui.Button;
import com.simon816.chatui.ui.FlowPaneUI;
import org.spongepowered.api.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NewTab extends Tab {

    private static final Text TITLE = Text.of("New Tab");

    public NewTab() {
        super(TITLE, new FlowPaneUI(FlowPaneUI.WRAP_VERTICALLY));
    }

    public void addButton(String label, ButtonAction action) {
        Button button = new Button(label);
        button.truncateOverflow(true);
        button.setClickHandler(action);
        getRoot().getChildren().add(button);
    }

    public static abstract class ButtonAction implements Consumer<PlayerChatView> {

        @Override
        public final void accept(PlayerChatView view) {
            if (!(view instanceof ActivePlayerChatView) || !(((ActivePlayerChatView) view).getWindow().getActiveTab() instanceof NewTab)) {
                return; // Expired link
            }
            onClick((ActivePlayerChatView) view);
        }

        protected final void replaceWith(Tab replacement, ActivePlayerChatView view) {
            int oldIndex = view.getWindow().getActiveIndex();
            view.getWindow().addTab(replacement, true);
            view.getWindow().removeTab(oldIndex);
            view.update();
        }

        protected abstract void onClick(ActivePlayerChatView view);
    }

    public static class LaunchTabAction extends ButtonAction {

        private final Function<ActivePlayerChatView, Tab> tabOpenFunc;

        public LaunchTabAction(Supplier<Tab> tabOpenFunc) {
            this(view -> tabOpenFunc.get());
        }

        public LaunchTabAction(Function<ActivePlayerChatView, Tab> tabOpenFunc) {
            this.tabOpenFunc = tabOpenFunc;
        }

        @Override
        protected void onClick(ActivePlayerChatView view) {
            replaceWith(this.tabOpenFunc.apply(view), view);
        }
    }

}
