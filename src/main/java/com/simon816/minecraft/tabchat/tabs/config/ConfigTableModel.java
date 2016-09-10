package com.simon816.minecraft.tabchat.tabs.config;

import com.simon816.minecraft.tabchat.ui.table.TableModel;

public class ConfigTableModel implements TableModel {

    private final ConfigTabControl control;

    public ConfigTableModel(ConfigTabControl control) {
        this.control = control;
    }

    @Override
    public int getRowCount() {
        return this.control.getEntries().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getCellValue(int row, int column) {
        ConfigEntry entry = this.control.getEntries().get(row);
        if (column == 0) {
            return entry.key;
        } else {
            return entry.value;
        }
    }

}
