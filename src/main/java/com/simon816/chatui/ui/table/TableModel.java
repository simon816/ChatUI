package com.simon816.chatui.ui.table;

public interface TableModel {

    int getRowCount();

    int getColumnCount();

    Object getCellValue(int row, int column);

}
