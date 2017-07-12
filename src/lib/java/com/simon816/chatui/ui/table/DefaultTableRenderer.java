package com.simon816.chatui.ui.table;

import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;

import java.util.List;

import javax.swing.SwingConstants;

public class DefaultTableRenderer implements TableRenderer {

    public static final TableViewport DEFAULT_VIEWPORT = new TableViewport() {

        @Override
        public int getFirstRowIndex() {
            return 0;
        }

        @Override
        public int getFirstColumnIndex() {
            return 0;
        }
    };

    @Override
    public TableViewport getViewport() {
        return DEFAULT_VIEWPORT;
    }

    @Override
    public int getPrefBorderWidth(int columnIndex, int numColumns, PlayerContext ctx) {
        return ctx.utils().getWidth('─', false);
    }

    @Override
    public TableColumnRenderer createColumnRenderer(int columnIndex) {
        return new DefaultColumnRenderer();
    }

    @Override
    public int modifyMaxWidth(int index, int max, int numColumns, PlayerContext ctx) {
        int barWidth = ctx.utils().getWidth('│', false);
        int edgeWidth = ctx.utils().getWidth('┼', false);
        int dashWidth = ctx.utils().getWidth('─', false);
        int extraPad;
        // line up so content starts after edge piece
        if (!ctx.forceUnicode) {
            extraPad = edgeWidth - barWidth;
        } else {
            extraPad = (int) Math.ceil(edgeWidth / 2D) - barWidth;
        }
        // Make it a multiple of the dash width
        int rem = max % dashWidth;
        extraPad += dashWidth - rem;
        // Align bar to center of joiner
        if (ctx.forceUnicode) {
            extraPad += (int) Math.ceil(edgeWidth / 2D) - barWidth;
        }
        extraPad %= dashWidth;
        return max + extraPad;
    }

    protected int getCellAlignment(int row, int column) {
        return SwingConstants.LEFT;
    }

    @Override
    public Text applySideBorders(int rowIndex, List<Text> line, int[] colMaxWidths, PlayerContext ctx) {
        Text.Builder builder = Text.builder();
        Text bar = TextUtils.charCache('│');
        for (int i = 0; i < colMaxWidths.length; i++) {
            Text part = null;
            int partWidth = 0;
            if (i < line.size()) {
                part = line.get(i);
            }
            if (part != null) {
                partWidth = ctx.utils().getWidth(part);
            }
            StringBuilder spaces = new StringBuilder();
            TextUtils.padSpaces(spaces, colMaxWidths[i] - partWidth);
            builder.append(bar);
            int alignment = getCellAlignment(rowIndex, i);
            if (alignment == SwingConstants.RIGHT || alignment == SwingConstants.CENTER) {
                if (spaces.length() > 0) {
                    if (alignment == SwingConstants.CENTER) {
                        String half = spaces.substring(0, (int) (spaces.length() + 0.5) / 2);
                        spaces.delete(0, half.length());
                        builder.append(Text.of(half));
                    } else {
                        builder.append(Text.of(spaces.toString()));
                    }
                }
            }
            if (part != null) {
                builder.append(part);
            }
            if (alignment == SwingConstants.LEFT || alignment == SwingConstants.CENTER) {
                if (spaces.length() > 0) {
                    builder.append(Text.of(spaces.toString()));
                }
            }
            if (i == colMaxWidths.length - 1) {
                builder.append(bar);
            }
        }
        return builder.build();

    }

    @Override
    public int borderHeight() {
        return 1;
    }

    @Override
    public Text createBorder(TableModel model, int rowIndex, int[] colMaxWidths, PlayerContext ctx) {
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
            char edge = i == 0 ? left : join;
            int edgeWidth = ctx.utils().getWidth(edge, false);
            int width = colMaxWidths[i] + edgeWidth;
            if (i < colMaxWidths.length - 1) {
                ctx.utils().startAndRepeat(lineBuilder, edge, '─', width);
            } else {
                width += edgeWidth;
                ctx.utils().startRepeatTerminate(lineBuilder, edge, '─', right, width);
            }
        }
        return lineBuilder.build();
    }

}
