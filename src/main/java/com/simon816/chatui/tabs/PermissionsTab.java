package com.simon816.chatui.tabs;

import com.simon816.chatui.PlayerContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PermissionsTab extends Tab {

    @Override
    public Text getTitle() {
        return Text.of("Permissions");
    }

    @Override
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder("Nothing here yet").color(TextColors.RED);
        for (int i = 0; i < ctx.height; i++) {
            builder.append(Text.NEW_LINE);
        }
        return builder.build();
    }

}
