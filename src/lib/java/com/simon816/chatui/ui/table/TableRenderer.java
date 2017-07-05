package com.simon816.chatui.ui.table;

import org.spongepowered.api.text.Text;

import java.util.List;

public interface TableRenderer {

    interface TableViewport {

        int getFirstRowIndex();

        int getFirstColumnIndex();

    }

    TableViewport getViewport();

    Text applySideBorders(int rowIndex, List<Text> line, int[] colMaxWidths, boolean forceUnicode);

    Text createBorder(TableModel model, int rowIndex, int[] colMaxWidths, boolean forceUnicode);

    default int modifyMaxWidth(int index, int max, boolean forceUnicode) {
        return max;
    }

    int getPrefBorderWidth(int columnIndex);

    default int getMinBorderWidth(int columnIndex) {
        return getPrefBorderWidth(columnIndex);
    }

    TableColumnRenderer createColumnRenderer(int columnIndex);

    int borderHeight();

}
