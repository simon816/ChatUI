package com.simon816.chatui.ui.table;

import com.simon816.chatui.lib.PlayerContext;
import org.spongepowered.api.text.Text;

import java.util.List;

public interface TableColumnRenderer {

    List<Text> renderCell(Object value, int row, int tableWidth, PlayerContext ctx);

    int getPrefWidth();

    default int getMinWidth() {
        return getPrefWidth();
    }

}
