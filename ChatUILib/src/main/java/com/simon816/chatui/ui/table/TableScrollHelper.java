package com.simon816.chatui.ui.table;

import com.simon816.chatui.ui.table.TableRenderer.TableViewport;

public class TableScrollHelper {

    private final TableModel model;

    private int scrollOffset;

    public TableScrollHelper(TableModel model) {
        this.model = model;
    }

    public boolean scrollUp() {
        if (!canScrollUp()) {
            return false;
        }
        this.scrollOffset--;
        return true;
    }

    public boolean canScrollUp() {
        return this.scrollOffset > 0;
    }

    public boolean scrollDown() {
        if (!canScrollDown()) {
            return false;
        }
        this.scrollOffset++;
        return true;
    }

    public boolean canScrollDown() {
        return this.scrollOffset < this.model.getRowCount() - 1;
    }

    public int getScrollOffset() {
        return this.scrollOffset;
    }

    public boolean scrollToOffset(int offset) {
        if (offset < 0 || offset >= this.model.getRowCount()) {
            return false;
        }
        this.scrollOffset = offset;
        return true;
    }

    public void reset() {
        this.scrollOffset = 0;
    }

    public TableModel getModel() {
        return this.model;
    }

    public TableViewport createViewport() {
        return new TableViewport() {

            @Override
            public int getFirstRowIndex() {
                return TableScrollHelper.this.getScrollOffset();
            }

            @Override
            public int getFirstColumnIndex() {
                return 0;
            }
        };
    }

}
