package com.simon816.chatui.tabs.config;

import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableModel;
import com.simon816.chatui.ui.table.TableScrollHelper;
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
    public List<Text> renderCellValue(Object value, int row, int column, TableModel model, PlayerContext ctx) {
        int fractionWidth = calculateEqualWidth(model, ctx);
        ConfigEntry entry = this.control.getEntries().get(row);
        if (column == 0) {
            return TextUtils.splitLines(renderKey(value, entry), fractionWidth);
        } else {
            return TextUtils.splitLines(renderValue((ConfigEntry.ConfigValue) value, entry), fractionWidth);
        }
    }

    @Override
    protected int getCellAlignment(int row, int column) {
        if (column == 0) {
            return SwingConstants.RIGHT;
        }
        return SwingConstants.LEFT;
    }

    private Text.Builder getBuilder(ConfigEntry entry) {
        Text.Builder builder = Text.builder();
        boolean focus = this.control.hasFocus(entry);
        if (!focus) {
            builder.color(TextColors.DARK_GRAY);
        }
        ConfigEditTab tab = this.control.tab;
        if (this.control.inDeleteMode()) {
            builder.onClick(tab.clickAction(() -> this.control.deleteNode(entry.key)));
        } else if (focus) {
            builder.onClick(entry.value.createClickAction(entry, tab));
        }
        return builder;
    }

    private Text renderKey(Object value, ConfigEntry entry) {
        return getBuilder(entry).append(Text.of(value)).build();
    }

    private Text renderValue(ConfigEntry.ConfigValue value, ConfigEntry entry) {
        Text.Builder builder = getBuilder(entry);
        boolean focus = this.control.hasFocus(entry);
        Text valueText = entry.value.toText(focus && !this.control.inDeleteMode());
        if (this.control.inDeleteMode()) {
            valueText = valueText.toBuilder().color(TextColors.GRAY).build();
        }
        builder.append(valueText);
        return builder.build();
    }

}
