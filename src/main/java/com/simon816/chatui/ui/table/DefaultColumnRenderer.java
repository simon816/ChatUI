package com.simon816.chatui.ui.table;

import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;

import java.util.List;

public class DefaultColumnRenderer implements TableColumnRenderer {

    @Override
    public List<Text> renderCell(Object value, int row, int tableWidth) {
        return TextUtils.splitLines(Text.of(value), getPrefWidth());
    }

    @Override
    public int getPrefWidth() {
        return 75;
    }

}
