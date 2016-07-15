package com.simon816.minecraft.tabchat.tabs;

import com.google.common.collect.Lists;
import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.TabbedChat;
import com.simon816.minecraft.tabchat.tabs.canvas.CanvasTab;
import com.simon816.minecraft.tabchat.util.TextUtils;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.common.SpongeImpl;

import java.util.List;
import java.util.function.Supplier;

public class NewTab extends Tab {

    private static final Text TITLE = Text.of("New Tab");

    private static final List<Button> buttons = Lists.newArrayList();

    static {
        registerButton(new LaunchTabButton("Settings", () -> new SettingsTab()));
        registerButton(new LaunchTabButton("Canvas", () -> new CanvasTab()));
        registerButton(new LaunchTabButton("New File", () -> new TextFileTab()));
        registerButton(new LaunchTabButton("Edit Config", () -> new ConfigEditTab(SpongeImpl.getGlobalConfig().getRootNode())));
    }

    public static void registerButton(Button button) {
        buttons.add(button);
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }

    @Override
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        int maxButtonRows = ctx.height / 3;
        int columns = 1;

        while (maxButtonRows < Math.ceil(buttons.size() / (float) columns)) {
            columns++;
        }

        int remainingHeight = ctx.height;
        int width = ctx.width / columns;
        while (width % 9 != 0) {
            width -= 1;
        }
        for (int i = 0; i < buttons.size() && remainingHeight > 0; i += columns) {
            Text.Builder line1 = Text.builder();
            Text.Builder line2 = Text.builder();
            Text.Builder line3 = Text.builder();
            for (int j = 0; j < columns; j++) {
                if (buttons.size() <= i + j) {
                    break;
                }
                Button button = buttons.get(i + j);
                Text[] text = drawButton(button, width);
                line1.append(text[0]);
                line2.append(text[1]);
                line3.append(text[2]);
            }
            builder.append(line1.build(), Text.NEW_LINE, line2.build(), Text.NEW_LINE, line3.build(), Text.NEW_LINE);
            remainingHeight -= 3;
        }
        for (int i = 0; i < remainingHeight; i++) {
            builder.append(Text.NEW_LINE);
        }
        return builder.build();
    }

    private Text[] drawButton(Button button, int width) {
        int barWidth = TextUtils.getStringWidth(String.valueOf('│'), false) * 2;
        int bwidth = button.getWidth();
        Text buttonText = button.text;
        if (bwidth > width - barWidth - 3) {
            // Trim down
            String t = buttonText.toPlain();
            while (bwidth > width - barWidth - 3) {
                t = t.substring(0, t.length() - 1);
                bwidth = TextUtils.getStringWidth(t, false) + 6;
            }
            buttonText = ((LiteralText.Builder) buttonText.toBuilder()).content(t + "...").build();
        }
        StringBuilder spaces = new StringBuilder();
        spaces.append('│');
        // Not sure why -3 is needed but it works
        TextUtils.padSpaces(spaces, width - bwidth - barWidth-3);
        spaces.append('│');
        String left = spaces.substring(0, spaces.length() / 2);
        String right = spaces.substring(left.length());
        return new Text[] {
                TextUtils.startRepeatTerminate('┌', '─', '┐', width),
                Text.builder().append(Text.of(left), buttonText, Text.of(right)).build(),
                TextUtils.startRepeatTerminate('└', '─', '┘', width)};
    }

    public static abstract class Button {

        protected final Text text;
        private int textWidth = -1;

        public Button(String text) {
            this.text = Text.builder(text).onClick(TextActions.executeCallback(src -> {
                PlayerChatView view = TabbedChat.getView(src);
                if (view.getWindow().getActiveTab().getClass() != NewTab.class) {
                    return; // Expired link
                }
                onClick(view);
            })).build();
        }

        protected int getWidth() {
            if (this.textWidth == -1) {
                this.textWidth = TextUtils.getWidth(this.text);
            }
            return this.textWidth;
        }

        protected final void replaceWith(Tab replacement, PlayerChatView view) {
            int oldIndex = view.getWindow().getActiveIndex();
            view.getWindow().addTab(replacement, true);
            view.getWindow().removeTab(oldIndex);
            view.update();
        }

        protected abstract void onClick(PlayerChatView view);
    }

    public static class LaunchTabButton extends Button {

        private final Supplier<Tab> supplier;

        public LaunchTabButton(String text, Supplier<Tab> tabSupplier) {
            super(text);
            this.supplier = tabSupplier;
        }

        @Override
        protected void onClick(PlayerChatView view) {
            replaceWith(this.supplier.get(), view);
        }
    }

}
