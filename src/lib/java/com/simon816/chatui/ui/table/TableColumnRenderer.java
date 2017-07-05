package com.simon816.chatui.ui.table;

import org.spongepowered.api.text.Text;

import java.util.List;

public interface TableColumnRenderer {

    List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode);

    int getPrefWidth();

    default int getMinWidth() {
        return getPrefWidth();
    }

}
