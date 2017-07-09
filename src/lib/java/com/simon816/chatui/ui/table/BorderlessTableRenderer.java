package com.simon816.chatui.ui.table;

import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;

import java.util.List;

public class BorderlessTableRenderer implements TableRenderer {

    @Override
    public TableViewport getViewport() {
        return DefaultTableRenderer.DEFAULT_VIEWPORT;
    }

    @Override
    public Text applySideBorders(int rowIndex, List<Text> line, int[] colMaxWidths, PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        for (int col = 0; col < colMaxWidths.length; col++) {
            Text part = null;
            int partWidth = 0;
            if (line.size() > col) {
                part = line.get(col);
            }
            if (part != null) {
                partWidth = ctx.utils().getWidth(part);
            }
            StringBuilder spaces = new StringBuilder();
            TextUtils.padSpaces(spaces, colMaxWidths[col] - partWidth);
            spaces.append(' '); // A space is the border
            if (part != null) {
                builder.append(part);
            }
            builder.append(Text.of(spaces.toString()));
        }
        return builder.build();
    }

    @Override
    public Text createBorder(TableModel model, int rowIndex, int[] colMaxWidths, PlayerContext ctx) {
        return null;
    }

    @Override
    public int borderHeight() {
        return 0;
    }

    @Override
    public int getPrefBorderWidth(int columnIndex) {
        return 0;
    }

    @Override
    public TableColumnRenderer createColumnRenderer(int columnIndex) {
        return new DefaultColumnRenderer();
    }

}
