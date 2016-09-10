package com.simon816.minecraft.tabchat.ui.table;

public interface TableModel {

    int getRowCount();

    int getColumnCount();

    Object getCellValue(int row, int column);

}
