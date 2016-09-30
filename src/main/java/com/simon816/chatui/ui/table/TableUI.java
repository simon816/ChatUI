package com.simon816.chatui.ui.table;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.table.TableRenderer.TableViewport;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TableUI implements UIComponent {

    private final TableModel model;
    private final TableRenderer renderer;
    private final List<TableColumnRenderer> columnRenderers = Lists.newArrayList();

    public TableUI(TableModel model, TableRenderer renderer) {
        this.model = model;
        this.renderer = renderer;
        initTable();
    }

    private void initTable() {
        for (int i = 0; i < this.model.getColumnCount(); i++) {
            this.columnRenderers.add(this.renderer.createColumnRenderer(i));
        }
    }

    @Override
    public int getPrefWidth(PlayerContext ctx) {
        int width = 0;
        List<TableColumnRenderer> columnRenderers2 = this.columnRenderers;
        for (int i = 0; i < columnRenderers2.size(); i++) {
            width += columnRenderers2.get(i).getPrefWidth();
            width += this.renderer.getPrefBorderWidth(i);
        }
        return width;
    }

    @Override
    public int getMinWidth(PlayerContext ctx) {
        int width = 0;
        List<TableColumnRenderer> columnRenderers2 = this.columnRenderers;
        for (int i = 0; i < columnRenderers2.size(); i++) {
            width += columnRenderers2.get(i).getMinWidth();
            width += this.renderer.getMinBorderWidth(i);
        }
        return width;
    }

    @Override
    public int getMinHeight(PlayerContext ctx) {
        return 1;
    }

    @Override
    public int getPrefHeight(PlayerContext ctx) {
        int numRows = this.model.getRowCount() - this.renderer.getViewport().getFirstRowIndex();
        return (numRows * 2) + 1;
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        TableViewport viewport = this.renderer.getViewport();
        Map<Integer, Integer> columnMaxWidths = Maps.newLinkedHashMap();
        Map<Integer, List<List<Text>>> rowLines = Maps.newLinkedHashMap();
        int lineCountInitial = 0;
        for (int row = viewport.getFirstRowIndex(); row < this.model.getRowCount(); row++) {
            List<List<Text>> cellLines = Lists.newArrayList();
            for (int column = viewport.getFirstColumnIndex(); column < this.model.getColumnCount(); column++) {
                Object value = this.model.getCellValue(row, column);
                List<Text> lines = this.columnRenderers.get(column).renderCell(value, row, ctx.width);
                for (int i = 0; i < lines.size(); i++) {
                    Text line = lines.get(i);
                    int width = TextUtils.getWidth(line);
                    columnMaxWidths.put(column, Math.max(columnMaxWidths.getOrDefault(column, 0), width));
                    List<Text> correctLine = i < cellLines.size() ? cellLines.get(i) : null;
                    if (correctLine == null) {
                        cellLines.add(i, correctLine = Lists.newArrayList());
                    }
                    while (correctLine.size() < column) {
                        correctLine.add(null);
                    }
                    correctLine.add(line);
                }
                rowLines.put(row, cellLines);
            }
            lineCountInitial += cellLines.size() + 1; // +1 for border
            if (lineCountInitial > ctx.height) {
                List<List<Text>> overRowLines = rowLines.get(row);
                while (!overRowLines.isEmpty() && lineCountInitial > ctx.height) {
                    overRowLines.remove(overRowLines.size() - 1);
                    lineCountInitial--;
                }
                if (lineCountInitial > ctx.height) {
                    rowLines.remove(row);
                }
                break;
            }
        }
        Collection<Integer> colMaxes = columnMaxWidths.values();
        int[] colMaxWidths = new int[colMaxes.size()];
        int i = 0;
        for (Integer max : colMaxes) {
            colMaxWidths[i] = this.renderer.modifyMaxWidth(i++, max);
        }
        Text border = this.renderer.createBorder(this.model, viewport.getFirstRowIndex() - 1, colMaxWidths);
        lineFactory.appendNewLine(border);
        for (Entry<Integer, List<List<Text>>> row : rowLines.entrySet()) {
            int rowIndex = row.getKey();
            List<List<Text>> rowLineList = row.getValue();
            for (List<Text> colGroup : rowLineList) {
                Text lineSegment = this.renderer.applySideBorders(rowIndex, colGroup, colMaxWidths);
                lineFactory.appendNewLine(lineSegment);
            }
            if (lineFactory.linesRemaining(ctx) > 0) {
                border = this.renderer.createBorder(this.model, rowIndex, colMaxWidths);
                lineFactory.appendNewLine(border);
            }
        }
    }

}
