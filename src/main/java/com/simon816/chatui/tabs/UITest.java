package com.simon816.chatui.tabs;

import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.Button;
import com.simon816.chatui.ui.HBoxUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.canvas.BlockRenderContext;
import com.simon816.chatui.ui.canvas.BrailleRenderContext;
import com.simon816.chatui.ui.canvas.CanvasUI;
import com.simon816.chatui.ui.canvas.CanvasUI.Context;
import com.simon816.chatui.ui.table.DefaultColumnRenderer;
import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableColumnRenderer;
import com.simon816.chatui.ui.table.TableModel;
import com.simon816.chatui.ui.table.TableUI;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class UITest extends Tab {

    private static class SimpleTable implements TableModel {

        private int rows;
        private int columns;

        public SimpleTable(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
        }

        @Override
        public int getRowCount() {
            return this.rows;
        }

        @Override
        public int getColumnCount() {
            return this.columns;
        }

        @Override
        public Object getCellValue(int row, int column) {
            if (this.columns == 3 && column == 1 && row == 1) {
                return "Middle Cell";
            }
            return new StringBuilder().append((char) (65 + column)).append(row + 1).toString();
        }

    }

    @Override
    public Text getTitle() {
        return Text.of("UI");
    }

    @Override
    public Text draw(PlayerContext ctx) {
        AnchorPaneUI pane = new AnchorPaneUI();
        HBoxUI box = new HBoxUI();
        TableModel table1 = new SimpleTable(6, 2);
        TableModel table2 = new SimpleTable(3, 3);
        CanvasUI canvas = new CanvasUI();
        BlockRenderContext cc = canvas.getContext(Context.BLOCKS);
        cc.drawRect(0, 0, 11, 11, TextColors.RED);
        cc.drawRect(1, 1, 10, 10, TextColors.GOLD);
        cc.drawRect(2, 2, 9, 9, TextColors.YELLOW);
        cc.drawRect(3, 3, 8, 8, TextColors.GREEN);
        cc.drawRect(4, 4, 7, 7, TextColors.BLUE);
        cc.drawRect(5, 5, 6, 6, TextColors.DARK_PURPLE);
        cc.drawRect(0, 6, 11, 11, TextColors.RESET);
        box.addChildren(
                new TableUI(table1, new DefaultTableRenderer() {

                    @Override
                    public TableColumnRenderer createColumnRenderer(int columnIndex) {
                        if (columnIndex == 0) {
                            return new DefaultColumnRenderer() {

                                @Override
                                public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
                                    if (row == 2) {
                                        CanvasUI canvas = new CanvasUI();
                                        BrailleRenderContext rctx = canvas.getContext(Context.BRAILLE);
                                        rctx.setVisualCorrections(true);
                                        rctx.drawCircle(8, 8, 8, true);
                                        LineFactory factory = new LineFactory();
                                        canvas.draw(new PlayerContext(ctx.getPlayer(), 100, 4, false), factory);
                                        return factory.getLines();
                                    }
                                    return super.renderCell(value, row, tableWidth, forceUnicode);
                                }
                            };
                        }
                        if (columnIndex == 1) {
                            return new DefaultColumnRenderer() {

                                @Override
                                public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
                                    if (row == 1) {
                                        LineFactory factory = new LineFactory();
                                        new Button("Click Me").draw(new PlayerContext(ctx.getPlayer(), 60, 3, false), factory);
                                        return factory.getLines();
                                    }
                                    return super.renderCell(value, row, tableWidth, forceUnicode);
                                }
                            };
                        }
                        return super.createColumnRenderer(columnIndex);
                    }
                }),
                canvas,
                new TableUI(table2, new DefaultTableRenderer() {

                    @Override
                    public TableColumnRenderer createColumnRenderer(int columnIndex) {
                        if (columnIndex == 1) {
                            return new DefaultColumnRenderer() {

                                @Override
                                public int getPrefWidth() {
                                    return 35;
                                };
                            };
                        }
                        return super.createColumnRenderer(columnIndex);
                    }
                }));
        Button button = new Button("Test Button");
        button.setClickHandler(view -> System.out.println("Clicked"));
        pane.addChildren(button, box);
        return pane.draw(ctx);
    }

}
