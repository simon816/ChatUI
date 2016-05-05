package com.simon816.minecraft.tabchat.tabs;

import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class SettingsTab extends Tab {

    private static final Text TITLE = Text.of("Settings");

    @Override
    public Text draw(int height) {
        LiteralText.Builder builder = Text.builder("\n");
        for (int i = 0; i < height - 1; i++) {
            if (i == 2) {
                builder.append(Text.of("Boolean: ", TextColors.GREEN, TextStyles.BOLD, "True", TextStyles.RESET, " ", TextColors.RED, "False\n"));
            } else if (i == 4) {
                builder.append(Text.of("Current integer: ", TextActions.suggestCommand("1"), "1", "\n"));
            } else {
                builder.append(Text.NEW_LINE);
            }
        }
        return builder.build();
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }

}
