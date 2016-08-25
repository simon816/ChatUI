package com.simon816.minecraft.tabchat.tabs;

import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.util.TextUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Collection;

public class PlayerListTab extends Tab {

    public static final PlayerListTab INSTANCE = new PlayerListTab();

    private PlayerListTab() {
    }

    @Override
    public Text draw(PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        Collection<Player> players = Sponge.getServer().getOnlinePlayers();
        int remainingHeight = ctx.height;
        int i = 0;
        for (Player player : players) {
            if (remainingHeight < 2) {
                break;
            }
            TextUtils.startRepeatTerminate(builder, '├', '─', '┤', ctx.width);
            builder.append(Text.of(player.getName()), Text.NEW_LINE);
            remainingHeight-=2;
            i++;
        }
        if (remainingHeight > 0) {
            StringBuilder spacing = new StringBuilder();
            while (remainingHeight-- > 0) {
                spacing.append("\n");
            }
            builder.append(Text.of(spacing));
        }
        return builder.build();
    }

    @Override
    public Text getTitle() {
        return Text.of("Player List");
    }

}
