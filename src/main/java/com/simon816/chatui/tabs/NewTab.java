package com.simon816.chatui.tabs;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.ui.Button;
import com.simon816.chatui.ui.HBoxUI;
import com.simon816.chatui.ui.VBoxUI;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NewTab extends Tab {

    private static final Text TITLE = Text.of("New Tab");

    private final List<Button> buttons = Lists.newArrayList();

    public void addButton(String label, ButtonAction action) {
        Button button = new Button(label);
        button.truncateOverflow(true);
        button.setClickHandler(action);
        this.buttons.add(button);
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }

    @Override
    public Text draw(PlayerContext ctx) {
        int buttonHeight = 3;
        checkArgument(ctx.height >= buttonHeight, "Height must be at least %s", buttonHeight);
        int maxButtonRows = ctx.height / buttonHeight;
        int columns = 1;
        while (maxButtonRows < Math.ceil(this.buttons.size() / (float) columns)) {
            columns++;
        }
        HBoxUI colBox = new HBoxUI();
        int index = 0;
        for (int i = 0; i < columns; i++) {
            VBoxUI rowBox = new VBoxUI();
            for (int j = 0; j < maxButtonRows; j++) {
                if (index >= this.buttons.size()) {
                    break;
                }
                rowBox.getChildren().add(this.buttons.get(index++));
            }
            colBox.getChildren().add(rowBox);
        }
        return colBox.draw(ctx);
    }

    public static abstract class ButtonAction implements Consumer<PlayerChatView> {

        @Override
        public final void accept(PlayerChatView view) {
            if (!(view.getWindow().getActiveTab() instanceof NewTab)) {
                return; // Expired link
            }
            onClick(view);
        }

        protected final void replaceWith(Tab replacement, PlayerChatView view) {
            int oldIndex = view.getWindow().getActiveIndex();
            view.getWindow().addTab(replacement, true);
            view.getWindow().removeTab(oldIndex);
            view.update();
        }

        protected abstract void onClick(PlayerChatView view);
    }

    public static class LaunchTabAction extends ButtonAction {

        private final Function<PlayerChatView, Tab> tabOpenFunc;

        public LaunchTabAction(Supplier<Tab> tabOpenFunc) {
            this(view -> tabOpenFunc.get());
        }

        public LaunchTabAction(Function<PlayerChatView, Tab> tabOpenFunc) {
            this.tabOpenFunc = tabOpenFunc;
        }

        @Override
        protected void onClick(PlayerChatView view) {
            replaceWith(this.tabOpenFunc.apply(view), view);
        }
    }

}
