package com.simon816.chatui.ui.table;

import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.util.FontData;
import com.simon816.chatui.util.TextUtils;
import com.simon816.chatui.util.Utils;
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

    protected static final int BORDER_MULTIPLE_A = FontData.VANILLA.getWidthInt('┼', false, false);
    protected static final int BORDER_SIDE_WIDTH_A = FontData.VANILLA.getWidthInt('│', false, false);
    protected static final int BORDER_MULTIPLE_U = FontData.VANILLA.getWidthInt('┼', false, true);
    protected static final int BORDER_SIDE_WIDTH_U = FontData.VANILLA.getWidthInt('│', false, true);

    @Override
    public TableViewport getViewport() {
        return DEFAULT_VIEWPORT;
    }

    @Override
    public int getPrefBorderWidth(int columnIndex) {
        return Math.max(BORDER_MULTIPLE_A, BORDER_MULTIPLE_U);
    }

    @Override
    public TableColumnRenderer createColumnRenderer(int columnIndex) {
        return new DefaultColumnRenderer();
    }

    @Override
    public int modifyMaxWidth(int index, int max, PlayerContext ctx) {
        final int mul = ctx.forceUnicode ? BORDER_MULTIPLE_U : BORDER_MULTIPLE_A;
        final int side = ctx.forceUnicode ? BORDER_SIDE_WIDTH_U : BORDER_SIDE_WIDTH_A;
        return Utils.ensureMultiple(max + side, mul) - side;
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
        final int mul = ctx.forceUnicode ? BORDER_MULTIPLE_U : BORDER_MULTIPLE_A;
        final int side = ctx.forceUnicode ? BORDER_SIDE_WIDTH_U : BORDER_SIDE_WIDTH_A;
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
            int width = colMaxWidths[i] + mul;
            int widest = Utils.ensureMultiple(width + side, mul) - side;
            if (i < colMaxWidths.length - 1) {
                ctx.utils().startAndRepeat(lineBuilder, i == 0 ? left : join, '─', widest);
            } else {
                width += mul;
                widest = Utils.ensureMultiple(width + side, mul) - side;
                ctx.utils().startRepeatTerminate(lineBuilder, i == 0 ? left : join, '─', right, widest);
            }
        }
        return lineBuilder.build();
    }

}
