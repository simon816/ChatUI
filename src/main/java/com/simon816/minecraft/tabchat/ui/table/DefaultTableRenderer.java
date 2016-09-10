package com.simon816.minecraft.tabchat.ui.table;

import com.simon816.minecraft.tabchat.PlayerContext;
import com.simon816.minecraft.tabchat.util.TextUtils;
import com.simon816.minecraft.tabchat.util.Utils;
import org.spongepowered.api.text.Text;

import java.util.List;

public class DefaultTableRenderer implements TableRenderer {

    private static final TableViewport DEFAULT_VIEWPORT = new TableViewport() {

        @Override
        public int getFirstRowIndex() {
            return 0;
        }

        @Override
        public int getFirstColumnIndex() {
            return 0;
        }
    };

    protected static final int BORDER_MULTIPLE = TextUtils.getWidth('┼', false);
    protected static final int BORDER_SIDE_WIDTH = TextUtils.getWidth('│', false);

    @Override
    public TableViewport getViewport() {
        return DEFAULT_VIEWPORT;
    }

    @Override
    public List<Text> renderCellValue(Object value, int row, int column, TableModel model, PlayerContext ctx) {
        int fractionWidth = ((ctx.width) / model.getColumnCount()) - BORDER_SIDE_WIDTH * 2;
        return TextUtils.splitLines(Text.of(value), fractionWidth);
    }

    @Override
    public int modifyMaxWidth(int index, int max) {
        return Utils.ensureMultiple(max + BORDER_SIDE_WIDTH, BORDER_MULTIPLE) - BORDER_SIDE_WIDTH;
    }

    @Override
    public Text applySideBorders(List<Text> line, int[] colMaxWidths) {
        Text.Builder builder = Text.builder();
        Text bar = TextUtils.charCache('│');
        for (int i = 0; i < colMaxWidths.length; i++) {
            Text part = null;
            int partWidth = 0;
            if (i < line.size()) {
                part = line.get(i);
            }
            if (part != null) {
                partWidth = TextUtils.getWidth(part);
            }
            StringBuilder spaces = new StringBuilder();
            TextUtils.padSpaces(spaces, colMaxWidths[i] - partWidth);
            builder.append(bar);
            if (part != null) {
                builder.append(part);
            }
            if (spaces.length() > 0) {
                builder.append(Text.of(spaces.toString()));
            }
            if (i == colMaxWidths.length - 1) {
                builder.append(bar);
            }
        }
        return builder.build();

    }

    @Override
    public Text createBorder(TableModel model, int rowIndex, int[] colMaxWidths) {
        char left = '├';
        char right = '┤';
        char join = '┼';
        if (rowIndex == -1) {
            left = '┌';
            right = '┐';
            join = '┬';
        } else if (rowIndex == model.getRowCount() - 1) {
            left = '└';
            right = '┘';
            join = '┴';
        }
        Text.Builder lineBuilder = Text.builder();
        for (int i = 0; i < colMaxWidths.length; i++) {
            int width = colMaxWidths[i] + BORDER_MULTIPLE;
            int widest = Utils.ensureMultiple(width + BORDER_SIDE_WIDTH, BORDER_MULTIPLE) - BORDER_SIDE_WIDTH;
            if (i < colMaxWidths.length - 1) {
                TextUtils.startAndRepeat(lineBuilder, i == 0 ? left : join, '─', widest);
            } else {
                width += BORDER_MULTIPLE;
                widest = Utils.ensureMultiple(width + BORDER_SIDE_WIDTH, BORDER_MULTIPLE) - BORDER_SIDE_WIDTH;
                TextUtils.startRepeatTerminate(lineBuilder, i == 0 ? left : join, '─', right, widest);
            }
        }
        return lineBuilder.build();
    }

}
