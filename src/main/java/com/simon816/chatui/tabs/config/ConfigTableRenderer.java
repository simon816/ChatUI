package com.simon816.chatui.tabs.config;

import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableColumnRenderer;
import com.simon816.chatui.ui.table.TableScrollHelper;
import com.simon816.chatui.util.ExtraUtils;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

import javax.swing.SwingConstants;

class ConfigTableRenderer extends DefaultTableRenderer {

    final ConfigTabControl control;
    private final TableViewport viewport;

    public ConfigTableRenderer(ConfigTabControl control, TableScrollHelper scrollHelper) {
        this.control = control;
        this.viewport = scrollHelper.createViewport();
    }

    @Override
    public TableViewport getViewport() {
        return this.viewport;
    }

    @Override
    public TableColumnRenderer createColumnRenderer(int columnIndex) {
        if (columnIndex == 0) {
            return new KeyColumnRenderer();
        } else {
            return new ValueColumnRenderer();
        }
    }

    @Override
    protected int getCellAlignment(int row, int column) {
        if (column == 0) {
            return SwingConstants.RIGHT;
        }
        return SwingConstants.LEFT;
    }

    Text.Builder getBuilder(ConfigEntry entry) {
        Text.Builder builder = Text.builder();
        boolean focus = this.control.hasFocus(entry);
        if (!focus) {
            builder.color(TextColors.DARK_GRAY);
        }
        ConfigEditTab tab = this.control.tab;
        if (this.control.inDeleteMode()) {
            builder.onClick(ExtraUtils.clickAction(() -> this.control.deleteNode(entry.key), tab));
        } else if (focus) {
            builder.onClick(entry.value.createClickAction(entry, tab));
        }
        return builder;
    }

    protected int calculateEqualWidth(int tableWidth, int columnCount, boolean forceUnicode) {
        return (tableWidth / columnCount) - (forceUnicode ? BORDER_MULTIPLE_U : BORDER_MULTIPLE_A)
                - (forceUnicode ? BORDER_SIDE_WIDTH_U : BORDER_SIDE_WIDTH_A);
    }

    private class KeyColumnRenderer implements TableColumnRenderer {

        KeyColumnRenderer() {
        }

        @Override
        public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
            int fractionWidth = calculateEqualWidth(tableWidth, 2, forceUnicode);
            ConfigEntry entry = ConfigTableRenderer.this.control.getEntries().get(row);

            Text keyText = getBuilder(entry).append(Text.of(value)).build();

            return TextUtils.splitLines(keyText, fractionWidth, forceUnicode);
        }

        @Override
        public int getPrefWidth() {
            return 75;
        }

    }

    private class ValueColumnRenderer implements TableColumnRenderer {

        ValueColumnRenderer() {
        }

        @Override
        public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
            int fractionWidth = calculateEqualWidth(tableWidth, 2, forceUnicode);
            ConfigEntry entry = ConfigTableRenderer.this.control.getEntries().get(row);

            Text.Builder builder = getBuilder(entry);
            boolean focus = ConfigTableRenderer.this.control.hasFocus(entry);
            Text valueText = entry.value.toText(focus && !ConfigTableRenderer.this.control.inDeleteMode());
            if (ConfigTableRenderer.this.control.inDeleteMode()) {
                valueText = valueText.toBuilder().color(TextColors.GRAY).build();
            }
            builder.append(valueText);

            return TextUtils.splitLines(builder.build(), fractionWidth, forceUnicode);
        }

        @Override
        public int getPrefWidth() {
            return 75;
        }

    }

}
