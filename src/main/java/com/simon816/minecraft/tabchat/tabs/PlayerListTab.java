package com.simon816.minecraft.tabchat.tabs;

import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.ui.Frame;
import com.simon816.minecraft.tabchat.ui.table.DefaultTableRenderer;
import com.simon816.minecraft.tabchat.ui.table.TableModel;
import com.simon816.minecraft.tabchat.ui.table.TableUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.List;

public class PlayerListTab extends Tab {

    public static final PlayerListTab INSTANCE = new PlayerListTab();
    private final Frame frame;

    private PlayerListTab() {
        this.frame = new Frame();
        this.frame.addComponent(new TableUI(new TableModel() {

            @Override
            public int getRowCount() {
                return Sponge.getServer().getOnlinePlayers().size();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getCellValue(int row, int column) {
                return ((List<Player>) Sponge.getServer().getOnlinePlayers()).get(row).getDisplayNameData().displayName().get();
            }
        }, new DefaultTableRenderer()));
    }

    @Override
    public Text draw(PlayerContext ctx) {
        return this.frame.draw(ctx);
    }

    @Override
    public Text getTitle() {
        return Text.of("Player List");
    }

}
