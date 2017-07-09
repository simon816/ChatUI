package com.simon816.chatui.tabs.perm;

import com.google.common.collect.Lists;
import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.table.DefaultColumnRenderer;
import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableColumnRenderer;
import com.simon816.chatui.ui.table.TableModel;
import com.simon816.chatui.ui.table.TableScrollHelper;
import com.simon816.chatui.ui.table.TableUI;
import com.simon816.chatui.util.ExtraUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Predicate;

class EntryDisplayer extends AnchorPaneUI {

    final PermissionsTab tab;
    List<Entry<String, String>> entryList;
    TableScrollHelper scroll;
    boolean addMode;
    private BiFunction<String, String, Entry<String, String>> createEntry;
    private Predicate<String> removeEntry;
    Runnable goBack;

    public EntryDisplayer(PermissionsTab tab) {
        this.tab = tab;
        addChildren(createTable());
        addWithConstraint(createBottomBar(), ANCHOR_BOTTOM);
    }

    public void setData(Map<String, String> map, BiFunction<String, String, Entry<String, String>> entryCreator, Predicate<String> remover,
            Runnable goBack) {
        this.entryList = Lists.newArrayList();
        for (Entry<String, String> entry : map.entrySet()) {
            this.entryList.add(entry);
        }
        this.createEntry = entryCreator;
        this.removeEntry = remover;
        this.goBack = goBack;
    }

    private TableUI createTable() {
        TableModel model = new TableModel() {

            @Override
            public int getRowCount() {
                return EntryDisplayer.this.entryList.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getCellValue(int row, int column) {
                Entry<String, String> entry = EntryDisplayer.this.entryList.get(row);
                if (column == 0) {
                    return entry.getKey();
                } else if (column == 1) {
                    return entry.getValue();
                }
                return null;
            }
        };
        this.scroll = new TableScrollHelper(model);
        return new TableUI(model, new DefaultTableRenderer() {

            private final TableViewport viewport = EntryDisplayer.this.scroll.createViewport();

            @Override
            public TableViewport getViewport() {
                return this.viewport;
            }

            @Override
            public TableColumnRenderer createColumnRenderer(int columnIndex) {
                if (columnIndex == 2) {
                    return new DefaultColumnRenderer() {

                        @Override
                        public List<Text> renderCell(Object value, int row, int tableWidth, PlayerContext ctx) {
                            return Collections.singletonList(Text.of(TextColors.RED, ExtraUtils.clickAction(() -> {
                                remove(row);
                            }, EntryDisplayer.this.tab), "X"));
                        }
                    };
                }
                return new DefaultColumnRenderer() {

                    @Override
                    public List<Text> renderCell(Object value, int row, int tableWidth, PlayerContext ctx) {
                        TextColor color = TextColors.RESET;
                        if (EntryDisplayer.this.editCol == columnIndex && EntryDisplayer.this.editRow == row) {
                            color = TextColors.GRAY;
                        }
                        return ctx.utils().splitLines(Text.builder((String) value)
                                .onClick(ExtraUtils.clickAction(() -> {
                                    setEdit(columnIndex, row);
                                }, EntryDisplayer.this.tab))
                                .color(color)
                                .build(), (tableWidth / 2) - 20);
                    }

                };
            }
        });

    }

    int editRow = -1;
    int editCol = -1;

    void setEdit(int col, int row) {
        if (this.editRow == row && this.editCol == col) {
            row = col = -1;
        }
        this.editRow = row;
        this.editCol = col;
    }

    void remove(int index) {
        if (this.removeEntry.test(this.entryList.get(index).getKey())) {
            this.entryList.remove(index);
        }
    }

    private UIComponent createBottomBar() {
        return new UIComponent() {

            @Override
            public void draw(PlayerContext ctx, LineFactory lineFactory) {
                lineFactory.appendNewLine(Text.builder()
                        .append(
                                Text.builder("[Return]").color(TextColors.BLUE)
                                        .onClick(ExtraUtils.clickAction(() -> {
                                            EntryDisplayer.this.goBack.run();
                                        }, EntryDisplayer.this.tab)).build(),
                                Text.builder(EntryDisplayer.this.addMode ? " [Cancel]" : " [Add]")
                                        .color(EntryDisplayer.this.addMode ? TextColors.RED : TextColors.GREEN)
                                        .onClick(ExtraUtils.clickAction(() -> EntryDisplayer.this.addMode = !EntryDisplayer.this.addMode,
                                                EntryDisplayer.this.tab))
                                        .build(),
                                Text.builder(" [Scroll Up]").color(EntryDisplayer.this.scroll.canScrollUp() ? TextColors.WHITE : TextColors.GRAY)
                                        .onClick(ExtraUtils.clickAction(EntryDisplayer.this.scroll::scrollUp, EntryDisplayer.this.tab))
                                        .build(),
                                Text.builder(" [Scroll Down]").color(EntryDisplayer.this.scroll.canScrollDown() ? TextColors.WHITE : TextColors.GRAY)
                                        .onClick(ExtraUtils.clickAction(EntryDisplayer.this.scroll::scrollDown, EntryDisplayer.this.tab))
                                        .build())
                        .build(), ctx);
            }
        };
    }

    public void onTextEntered(PlayerChatView view, Text input) {
        if (!this.addMode && this.editRow == -1) {
            return;
        }
        String inStr = input.toPlain();
        if (this.editRow != -1) {
            Entry<String, String> old = this.entryList.get(this.editRow);
            String key = old.getKey();
            String value = old.getValue();
            if (inStr.equals("$delete")) {
                if (this.removeEntry.test(key)) {
                    this.entryList.remove(this.editRow);
                }
            } else {
                if (this.editCol == 0) {
                    if (this.removeEntry.test(key)) {
                        key = inStr;
                    }
                } else {
                    value = inStr;
                }
                if (!key.equals(old.getKey()) || !value.equals(old.getValue())) {
                    Entry<String, String> newEntry = this.createEntry.apply(key, value);
                    this.entryList.set(this.editRow, newEntry);
                }
            }
            this.editRow = this.editCol = -1;
        } else {
            this.addMode = false;
            String key = inStr;
            String value = "value";
            Entry<String, String> entry = this.createEntry.apply(key, value);
            if (entry != null) {
                this.entryList.add(entry);
            }
        }
        view.update();
    }

}
