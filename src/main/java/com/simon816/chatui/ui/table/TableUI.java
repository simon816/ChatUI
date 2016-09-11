package com.simon816.chatui.ui.table;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simon816.chatui.PlayerContext;
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

    public TableUI(TableModel model, TableRenderer renderer) {
        this.model = model;
        this.renderer = renderer;
    }

    @Override
    public int draw(Text.Builder builder, PlayerContext ctx) {
        TableViewport viewport = this.renderer.getViewport();
        Map<Integer, Integer> columnMaxWidths = Maps.newLinkedHashMap();
        Map<Integer, List<List<Text>>> rowLines = Maps.newLinkedHashMap();
        int lineCountInitial = 0;
        for (int row = viewport.getFirstRowIndex(); row < this.model.getRowCount(); row++) {
            List<List<Text>> cellLines = Lists.newArrayList();
            for (int column = viewport.getFirstColumnIndex(); column < this.model.getColumnCount(); column++) {
                Object value = this.model.getCellValue(row, column);
                List<Text> lines = this.renderer.renderCellValue(value, row, column, this.model, ctx);
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
        builder.append(border, Text.NEW_LINE);
        int lineCount = 1;
        for (Entry<Integer, List<List<Text>>> row : rowLines.entrySet()) {
            int rowIndex = row.getKey();
            List<List<Text>> rowLineList = row.getValue();
            Text.Builder completeLineBuilder = Text.builder();
            for (List<Text> colGroup : rowLineList) {
                Text lineSegment = this.renderer.applySideBorders(rowIndex, colGroup, colMaxWidths);
                completeLineBuilder.append(lineSegment, Text.NEW_LINE);
                lineCount++;
            }
            Text completeLine = completeLineBuilder.build();
            if (ctx.height - lineCount > 0) {
                border = this.renderer.createBorder(this.model, rowIndex, colMaxWidths);
                builder.append(completeLine, border, Text.NEW_LINE);
                lineCount++;
            } else {
                builder.append(completeLine);
            }
        }
        while (lineCount++ < ctx.height) {
            builder.append(Text.NEW_LINE);
        }
        return lineCount;
    }

}
